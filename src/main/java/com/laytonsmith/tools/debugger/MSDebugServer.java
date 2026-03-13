package com.laytonsmith.tools.debugger;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.environments.DebugContext;
import com.laytonsmith.core.environments.DebugListener;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.PausedState;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.environments.Breakpoint;
import org.eclipse.lsp4j.debug.Capabilities;
import org.eclipse.lsp4j.debug.ConfigurationDoneArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.ContinueResponse;
import org.eclipse.lsp4j.debug.ContinuedEventArguments;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.ExceptionBreakpointsFilter;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.OutputEventArguments;
import org.eclipse.lsp4j.debug.Scope;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsResponse;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsResponse;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StackFrame;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.StoppedEventArgumentsReason;
import org.eclipse.lsp4j.debug.TerminatedEventArguments;
import org.eclipse.lsp4j.debug.Thread;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.Variable;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.ThreadEventArgumentsReason;

/**
 * A Debug Adapter Protocol (DAP) server for MethodScript. This server can be started
 * alongside any MethodScript execution mode (cmdline, eval, embedded) to enable
 * debugging via VS Code or any DAP-compatible client.
 *
 * <p>The server opens a TCP listener and waits for a debugger to connect. It supports
 * two usage patterns:</p>
 * <ul>
 *   <li><b>Launch mode</b> (cmdline/eval): The server compiles and executes a script
 *       file, with optional suspend-before-execution.</li>
 *   <li><b>Attach mode</b> (embedded or pre-running): The caller provides an Environment
 *       with a DebugContext already wired in. The server bridges DAP requests to that
 *       existing debug context.</li>
 * </ul>
 */
public class MSDebugServer implements IDebugProtocolServer {

	/** Default port for the DAP TCP listener (MSDB on a phone keypad). */
	public static final int DEFAULT_PORT = 6732;

	private static final int LOCALS_REF = 1;

	private IDebugProtocolClient client;
	private DebugContext debugCtx;
	private final ConcurrentHashMap<Integer, PausedState> pausedStates = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, Script.DebugSnapshot> asyncSnapshots = new ConcurrentHashMap<>();
	/** The paused state most recently requested via stackTrace, used by scopes/variables. */
	private volatile PausedState lastInspectedState;
	private java.lang.Thread executionThread;
	private Environment env;
	private boolean configurationDone = false;
	private String programPath;
	private ServerSocket serverSocket;
	private final CountDownLatch suspendLatch = new CountDownLatch(1);
	private final CountDownLatch completionLatch = new CountDownLatch(1);
	private boolean suspendOnStart = false;
	private boolean managedExecution = false;
	private PrintStream originalOut;
	private PrintStream originalErr;

	/**
	 * Starts a DAP TCP listener on the given port. This method starts accepting
	 * connections on a background thread and returns immediately. When a client
	 * connects, the DAP JSON-RPC protocol handler starts automatically.
	 *
	 * @param port The TCP port to listen on.
	 * @param environment The Environment to debug. If it does not already contain a
	 *     DebugContext, one will be created and added.
	 * @param suspend If true, script execution will not begin until the debugger sends
	 *     configurationDone (analogous to Java's suspend=y).
	 * @return The Environment, which may have been cloned to include the DebugContext.
	 * @throws IOException If the server socket cannot be opened.
	 */
	public Environment startListening(int port, Environment environment, boolean suspend) throws IOException {
		return startListening(port, environment, suspend, DebugContext.ThreadingMode.SYNCHRONOUS);
	}

	/**
	 * Starts a DAP TCP listener on the given port with an explicit threading mode.
	 *
	 * @param port The TCP port to listen on.
	 * @param environment The Environment to debug.
	 * @param suspend If true, waits for configurationDone before execution.
	 * @param threadingMode The threading mode for the main execution thread.
	 * @return The Environment, which may have been cloned to include the DebugContext.
	 * @throws IOException If the server socket cannot be opened.
	 */
	public Environment startListening(int port, Environment environment, boolean suspend,
			DebugContext.ThreadingMode threadingMode) throws IOException {
		this.env = environment;
		this.suspendOnStart = suspend;
		this.managedExecution = true;

		if(env.hasEnv(DebugContext.class)) {
			debugCtx = env.getEnv(DebugContext.class);
		} else {
			debugCtx = new DebugContext(new DAPDebugListener(),
					threadingMode, java.lang.Thread.currentThread());
			try {
				env = env.cloneAndAdd(debugCtx);
			} catch(Exception e) {
				throw new IOException("Failed to add debug context to environment", e);
			}
		}

		serverSocket = new ServerSocket(port);
		System.err.println("MethodScript debugger listening on port " + port);
		if(suspend) {
			System.err.println("Waiting for debugger to connect before execution...");
		}

		java.lang.Thread acceptThread = new java.lang.Thread(() -> {
			try {
				Socket socket = serverSocket.accept();
				socket.setKeepAlive(true);
				Launcher<IDebugProtocolClient> launcher
						= DSPLauncher.createServerLauncher(this, socket.getInputStream(), socket.getOutputStream());
				client = launcher.getRemoteProxy();
				launcher.startListening();
			} catch(IOException e) {
				if(!serverSocket.isClosed()) {
					System.err.println("Debug server accept failed: " + e.getMessage());
				}
			}
		}, "ms-debug-accept");
		acceptThread.setDaemon(true);
		acceptThread.start();

		return env;
	}

	/**
	 * If suspend mode is active, blocks until the debugger has connected and sent
	 * configurationDone. Call this before starting script execution to implement
	 * suspend-on-start behavior.
	 */
	public void awaitConfiguration() throws InterruptedException {
		if(suspendOnStart) {
			suspendLatch.await();
		}
	}

	/**
	 * Blocks until the debug session is fully complete. This is signalled when the
	 * debugger disconnects or the script runs to completion without hitting any more
	 * breakpoints. Call this after script execution returns to prevent premature
	 * shutdown while the debugger is still paused on a breakpoint.
	 */
	public void awaitCompletion() throws InterruptedException {
		completionLatch.await();
	}

	/**
	 * Runs the given compiled script on a background thread. This method returns
	 * immediately. The debug server manages the full execution lifecycle: if a
	 * breakpoint is hit, execution pauses and the thread exits; when the debugger
	 * sends continue/step, {@link #resumeOnThread()} picks up from the snapshot.
	 * When execution completes (no more breakpoints), the terminated event is sent
	 * to the client and the completion latch is released.
	 *
	 * <p>Call {@link #awaitCompletion()} after this to block until the debug
	 * session fully ends.</p>
	 *
	 * @param tree The compiled parse tree
	 * @param env The environment (must already contain DebugContext)
	 * @param vars Script argument variables (may be null)
	 */
	public void runScript(ParseTree tree, Environment env, List<com.laytonsmith.core.constructs.Variable> vars) {
		this.env = env;
		wireUpDaemonManager(env);
		installOutputRedirect();
		spawnExecutionThread(() -> MethodScriptCompiler.execute(tree, env, null, null, vars));
	}

	/**
	 * Spawns a new execution thread that runs the given task and handles the full
	 * debug lifecycle. If the task returns {@link Script#isDebuggerPaused(Mixed)},
	 * the thread waits for background threads (DaemonManager) to keep the process
	 * alive. If it completes normally, the thread signals the main thread exit,
	 * waits for daemons, and then signals session completion.
	 *
	 * @param task A callable that performs the script execution and returns the result
	 */
	private void spawnExecutionThread(java.util.concurrent.Callable<Mixed> task) {
		executionThread = new java.lang.Thread(() -> {
			try {
				Mixed result = task.call();
				if(!Script.isDebuggerPaused(result)) {
					mainThreadFinished();
				}
				awaitDaemonManager(env);
				if(!Script.isDebuggerPaused(result)) {
					executionCompleted();
				}
			} catch(Exception e) {
				sendOutput("error", "Script error: " + e.getMessage());
				executionCompleted();
			}
		}, "ms-debug-execution");
		debugCtx.setMainThread(executionThread);
		executionThread.start();
	}

	/**
	 * Called when the main script thread finishes executing user code but before
	 * waiting for background threads. Unregisters the main thread from the
	 * DebugContext and sends a thread-exited event so VS Code removes it from
	 * the Call Stack panel.
	 */
	private void mainThreadFinished() {
		int mainDapId = debugCtx.getDapThreadId(java.lang.Thread.currentThread());
		debugCtx.unregisterThread(java.lang.Thread.currentThread());
		if(client != null && mainDapId >= 0) {
			ThreadEventArguments tea = new ThreadEventArguments();
			tea.setThreadId(mainDapId);
			tea.setReason(ThreadEventArgumentsReason.EXITED);
			client.thread(tea);
		}
	}

	/**
	 * Called when script execution completes normally (not paused). Sends the
	 * terminated event to the client and releases the completion latch.
	 */
	private void executionCompleted() {
		restoreOutputRedirect();
		if(client != null) {
			client.terminated(new TerminatedEventArguments());
		}
		completionLatch.countDown();
	}

	/**
	 * Returns the port the server is currently listening on, or -1 if the
	 * server is not yet listening or has been shut down.
	 */
	public int getPort() {
		return serverSocket != null && !serverSocket.isClosed()
				? serverSocket.getLocalPort() : -1;
	}

	/**
	 * Shuts down the debug server, closing the TCP listener.
	 */
	public void shutdown() {
		try {
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch(IOException e) {
			// Ignore close errors
		}
	}

	@Override
	public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
		Capabilities caps = new Capabilities();
		caps.setSupportsConfigurationDoneRequest(true);
		caps.setSupportsEvaluateForHovers(true);
		caps.setSupportsConditionalBreakpoints(true);
		caps.setSupportsHitConditionalBreakpoints(true);

		ExceptionBreakpointsFilter allFilter = new ExceptionBreakpointsFilter();
		allFilter.setFilter("all");
		allFilter.setLabel("All Exceptions");
		allFilter.setDescription("Break on all thrown exceptions");

		ExceptionBreakpointsFilter uncaughtFilter = new ExceptionBreakpointsFilter();
		uncaughtFilter.setFilter("uncaught");
		uncaughtFilter.setLabel("Uncaught Exceptions");
		uncaughtFilter.setDescription("Break on exceptions not caught by a try block");
		uncaughtFilter.setDefault_(true);

		caps.setExceptionBreakpointFilters(
				new ExceptionBreakpointsFilter[]{allFilter, uncaughtFilter});
		return CompletableFuture.completedFuture(caps);
	}

	@Override
	public CompletableFuture<Void> launch(Map<String, Object> args) {
		programPath = (String) args.get("program");
		if(programPath == null) {
			sendOutput("error", "No 'program' specified in launch configuration");
			return CompletableFuture.completedFuture(null);
		}

		File scriptFile = new File(programPath);
		if(!scriptFile.exists()) {
			sendOutput("error", "Script file not found: " + programPath);
			return CompletableFuture.completedFuture(null);
		}

		if(env == null) {
			try {
				env = Static.GenerateStandaloneEnvironment(false,
						EnumSet.of(RuntimeMode.CMDLINE));
				debugCtx = new DebugContext(new DAPDebugListener(),
						DebugContext.ThreadingMode.SYNCHRONOUS, java.lang.Thread.currentThread());
				env = env.cloneAndAdd(debugCtx);
			} catch(Exception e) {
				sendOutput("error", "Failed to create environment: " + e.getMessage());
				return CompletableFuture.completedFuture(null);
			}
		}

		client.initialized();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
		configurationDone = true;
		suspendLatch.countDown();
		if(programPath != null && !managedExecution) {
			startExecution();
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> disconnect(DisconnectArguments args) {
		if(debugCtx != null) {
			debugCtx.disconnect();
			if(!pausedStates.isEmpty()) {
				for(int threadId : pausedStates.keySet()) {
					resumeExecution(threadId);
				}
			}
		}
		suspendLatch.countDown();
		completionLatch.countDown();
		shutdown();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
		SetBreakpointsResponse response = new SetBreakpointsResponse();
		Source source = args.getSource();
		if(source == null || source.getPath() == null || debugCtx == null) {
			response.setBreakpoints(new org.eclipse.lsp4j.debug.Breakpoint[0]);
			return CompletableFuture.completedFuture(response);
		}

		File file = new File(source.getPath());
		SourceBreakpoint[] sourceBreakpoints = args.getBreakpoints();
		List<Breakpoint> debugBps = new ArrayList<>();
		List<org.eclipse.lsp4j.debug.Breakpoint> verified = new ArrayList<>();

		if(sourceBreakpoints != null) {
			for(SourceBreakpoint sbp : sourceBreakpoints) {
				String condition = sbp.getCondition();
				String hitCondition = sbp.getHitCondition();
				int hitThreshold = 0;
				if(hitCondition != null && !hitCondition.isEmpty()) {
					try {
						hitThreshold = Integer.parseInt(hitCondition.trim());
					} catch(NumberFormatException e) {
						// Ignore invalid hit condition
					}
				}

				org.eclipse.lsp4j.debug.Breakpoint bp = new org.eclipse.lsp4j.debug.Breakpoint();
				bp.setLine(sbp.getLine());
				bp.setSource(source);
				try {
					debugBps.add(new Breakpoint(file, sbp.getLine(), condition, hitThreshold));
					bp.setVerified(true);
				} catch(IllegalArgumentException e) {
					bp.setVerified(false);
					bp.setMessage(e.getMessage());
				}
				verified.add(bp);
			}
		}

		debugCtx.setBreakpointsForFile(file, debugBps);
		response.setBreakpoints(verified.toArray(new org.eclipse.lsp4j.debug.Breakpoint[0]));
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<SetExceptionBreakpointsResponse> setExceptionBreakpoints(
			SetExceptionBreakpointsArguments args) {
		DebugContext.ExceptionBreakMode mode = DebugContext.ExceptionBreakMode.NONE;
		String[] filters = args.getFilters();
		if(filters != null) {
			for(String filter : filters) {
				if("all".equals(filter)) {
					mode = DebugContext.ExceptionBreakMode.ALL;
					break;
				} else if("uncaught".equals(filter)) {
					mode = DebugContext.ExceptionBreakMode.UNCAUGHT;
				}
			}
		}
		if(debugCtx != null) {
			debugCtx.setExceptionBreakMode(mode);
		}
		return CompletableFuture.completedFuture(new SetExceptionBreakpointsResponse());
	}

	@Override
	public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
		int threadId = args.getThreadId();
		if(pausedStates.containsKey(threadId)) {
			debugCtx.setStepMode(threadId,
					DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
			resumeExecution(threadId);
		}
		ContinueResponse response = new ContinueResponse();
		response.setAllThreadsContinued(false);
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<Void> next(NextArguments args) {
		int threadId = args.getThreadId();
		PausedState state = pausedStates.get(threadId);
		if(state != null) {
			debugCtx.setStepMode(threadId, DebugContext.StepMode.OVER,
					state.getUserCallDepth(), state.getPauseTarget());
			resumeExecution(threadId);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> stepIn(StepInArguments args) {
		int threadId = args.getThreadId();
		PausedState state = pausedStates.get(threadId);
		if(state != null) {
			debugCtx.setStepMode(threadId, DebugContext.StepMode.INTO,
					state.getUserCallDepth(), state.getPauseTarget());
			resumeExecution(threadId);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> stepOut(StepOutArguments args) {
		int threadId = args.getThreadId();
		PausedState state = pausedStates.get(threadId);
		if(state != null) {
			debugCtx.setStepMode(threadId, DebugContext.StepMode.OUT,
					state.getUserCallDepth(), state.getPauseTarget());
			resumeExecution(threadId);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<ThreadsResponse> threads() {
		ThreadsResponse response = new ThreadsResponse();
		if(debugCtx != null) {
			Map<Integer, String> registered = debugCtx.getRegisteredThreads();
			Thread[] threads = new Thread[registered.size()];
			int i = 0;
			for(Map.Entry<Integer, String> entry : registered.entrySet()) {
				Thread t = new Thread();
				t.setId(entry.getKey());
				t.setName(entry.getValue());
				threads[i++] = t;
			}
			response.setThreads(threads);
		} else {
			response.setThreads(new Thread[0]);
		}
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
		StackTraceResponse response = new StackTraceResponse();
		PausedState pausedState = pausedStates.get(args.getThreadId());
		if(pausedState == null) {
			response.setStackFrames(new StackFrame[0]);
			response.setTotalFrames(0);
			return CompletableFuture.completedFuture(response);
		}
		lastInspectedState = pausedState;

		List<StackTraceFrame> callStack = pausedState.getCallStack();
		List<StackFrame> frames = new ArrayList<>();
		Target pauseTarget = pausedState.getPauseTarget();

		if(callStack.isEmpty()) {
			// At top level — only a <main> frame
			StackFrame mainFrame = new StackFrame();
			mainFrame.setId(0);
			mainFrame.setName("<main>");
			mainFrame.setLine(pauseTarget.line());
			mainFrame.setColumn(pauseTarget.col());
			setSource(mainFrame, pauseTarget);
			frames.add(mainFrame);
		} else {
			// Inside proc/closure calls. callStack is innermost-first.
			// The first entry is the current frame — use pauseTarget for its location.
			// Each subsequent frame shows the call site of the frame above it.
			for(int i = 0; i < callStack.size(); i++) {
				StackTraceFrame stf = callStack.get(i);
				StackFrame frame = new StackFrame();
				frame.setId(i);
				frame.setName(stf.getProcedureName());
				Target t;
				if(i == 0) {
					t = pauseTarget;
				} else {
					// Show where this frame called the child (i.e. the child's call site)
					t = callStack.get(i - 1).getCallSite();
				}
				frame.setLine(t.line());
				frame.setColumn(t.col());
				setSource(frame, t);
				frames.add(frame);
			}
			// Add the outermost <main> frame at the bottom, showing where
			// the outermost proc was called from
			StackFrame mainFrame = new StackFrame();
			mainFrame.setId(callStack.size());
			mainFrame.setName("<main>");
			Target outerCallSite = callStack.get(callStack.size() - 1).getCallSite();
			mainFrame.setLine(outerCallSite.line());
			mainFrame.setColumn(outerCallSite.col());
			setSource(mainFrame, outerCallSite);
			frames.add(mainFrame);
		}

		response.setStackFrames(frames.toArray(new StackFrame[0]));
		response.setTotalFrames(frames.size());
		return CompletableFuture.completedFuture(response);
	}

	private void setSource(StackFrame frame, Target t) {
		if(t.file() != null) {
			Source src = new Source();
			src.setPath(t.file().getAbsolutePath());
			src.setName(t.file().getName());
			frame.setSource(src);
		}
	}

	@Override
	public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
		ScopesResponse response = new ScopesResponse();
		Scope locals = new Scope();
		locals.setName("Locals");
		locals.setVariablesReference(LOCALS_REF);
		locals.setExpensive(false);
		response.setScopes(new Scope[]{locals});
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
		VariablesResponse response = new VariablesResponse();
		PausedState inspected = lastInspectedState;
		if(inspected == null || args.getVariablesReference() != LOCALS_REF) {
			response.setVariables(new Variable[0]);
			return CompletableFuture.completedFuture(response);
		}

		Map<String, Mixed> vars = inspected.getVariables();
		List<Variable> result = new ArrayList<>();
		Environment stateEnv = inspected.getEnvironment();
		for(Map.Entry<String, Mixed> entry : vars.entrySet()) {
			Variable v = new Variable();
			v.setName(entry.getKey());
			v.setValue(entry.getValue().val());
			v.setType(entry.getValue().typeof(stateEnv).val());
			v.setVariablesReference(0);
			result.add(v);
		}

		response.setVariables(result.toArray(new Variable[0]));
		return CompletableFuture.completedFuture(response);
	}

	@Override
	public CompletableFuture<EvaluateResponse> evaluate(EvaluateArguments args) {
		EvaluateResponse response = new EvaluateResponse();
		PausedState inspected = lastInspectedState;
		if(inspected == null) {
			response.setResult("Not paused");
			response.setVariablesReference(0);
			return CompletableFuture.completedFuture(response);
		}

		String expression = args.getExpression();
		DAPTeeOutputStream.setSuppressOutput(true);
		try {
			Environment evalEnv = inspected.getEnvironment();
			GlobalEnv gEnv = evalEnv.getEnv(GlobalEnv.class);
			gEnv.SetFlag(GlobalEnv.FLAG_NO_CHECK_UNDEFINED, true);
			try {
				Set<Class<? extends Environment.EnvironmentImpl>> envClasses
						= evalEnv.getEnvClasses();
				Mixed result = MethodScriptCompiler.execute(expression, null, true,
						evalEnv, envClasses, null, null, null);
				while(result instanceof IVariable iv) {
					result = gEnv.GetVarList()
							.get(iv.getVariableName(), iv.getTarget(), evalEnv).ival();
				}
				response.setResult(result.val());
				response.setType(result.typeof(evalEnv).val());
			} finally {
				gEnv.ClearFlag(GlobalEnv.FLAG_NO_CHECK_UNDEFINED);
			}
		} catch(Exception e) {
			response.setResult(e.getMessage());
		} finally {
			DAPTeeOutputStream.setSuppressOutput(false);
		}
		response.setVariablesReference(0);
		return CompletableFuture.completedFuture(response);
	}

	private void startExecution() {
		if(!configurationDone || programPath == null) {
			return;
		}
		wireUpDaemonManager(env);

		spawnExecutionThread(() -> {
			File scriptFile = new File(programPath);
			String script = FileUtil.read(scriptFile);
			Set<Class<? extends Environment.EnvironmentImpl>> envClasses
					= Environment.getDefaultEnvClasses();
			StaticAnalysis analysis = new StaticAnalysis(true);
			analysis.setLocalEnable(false);
			ParseTree tree = MethodScriptCompiler.compile(
					MethodScriptCompiler.lex(script, env, scriptFile, true),
					env, envClasses, analysis);
			return MethodScriptCompiler.execute(tree, env, null, null);
		});
	}

	/**
	 * Resumes execution of the specified thread after a debug pause. In synchronous
	 * mode, releases the thread's latch so it continues in place. In asynchronous mode
	 * (main thread only), spawns a new thread to resume from the saved snapshot.
	 *
	 * @param threadId The DAP thread ID to resume
	 */
	private void resumeExecution(int threadId) {
		pausedStates.remove(threadId);
		if(debugCtx.getThreadingMode() == DebugContext.ThreadingMode.SYNCHRONOUS
				|| threadId != DebugContext.MAIN_THREAD_DAP_ID) {
			// Sync mode or background thread: thread is blocked on the latch — release it
			asyncSnapshots.remove(threadId);
			debugCtx.resume(threadId);
		} else {
			// Async mode, main thread: spawn a new thread to resume from snapshot
			Script.DebugSnapshot snapshot = asyncSnapshots.remove(threadId);
			spawnExecutionThread(() -> Script.resumeEval(snapshot));
		}
	}

	/**
	 * Registers the DebugContext with the environment's DaemonManager so that
	 * background threads (e.g. x_new_thread) are automatically tracked and
	 * reported as DAP thread events.
	 */
	private void wireUpDaemonManager(Environment env) {
		if(env.hasEnv(StaticRuntimeEnv.class)) {
			com.laytonsmith.PureUtilities.DaemonManager dm
					= env.getEnv(StaticRuntimeEnv.class).GetDaemonManager();
			debugCtx.registerWithDaemonManager(dm);
		}
	}

	/**
	 * Waits for all background threads tracked by the DaemonManager to finish.
	 * This ensures the debug session stays alive while background threads
	 * (e.g. x_new_thread) are still running.
	 */
	private void awaitDaemonManager(Environment env) {
		if(env.hasEnv(StaticRuntimeEnv.class)) {
			try {
				env.getEnv(StaticRuntimeEnv.class).GetDaemonManager().waitForThreads();
			} catch(InterruptedException e) {
				// Interrupted — let the session end
			}
		}
	}

	private void installOutputRedirect() {
		originalOut = System.out;
		originalErr = System.err;
		System.setOut(new PrintStream(new DAPTeeOutputStream(originalOut, "stdout"), true, StandardCharsets.UTF_8));
		System.setErr(new PrintStream(new DAPTeeOutputStream(originalErr, "stderr"), true, StandardCharsets.UTF_8));
	}

	private void restoreOutputRedirect() {
		if(originalOut != null) {
			System.setOut(originalOut);
			originalOut = null;
		}
		if(originalErr != null) {
			System.setErr(originalErr);
			originalErr = null;
		}
	}

	/**
	 * An OutputStream that tees output to both the original stream and the DAP client
	 * as OutputEvents. Bytes are buffered until a newline is seen, then sent as a
	 * single output event to avoid splitting multi-byte characters or sending
	 * partial lines.
	 *
	 * <p>Output can be suppressed on a per-thread basis using {@link #setSuppressOutput(boolean)}.
	 * When suppressed, output is silently discarded (not sent to the original stream or the
	 * DAP client). This is used during watch expression evaluation to prevent side effects.</p>
	 */
	private class DAPTeeOutputStream extends OutputStream {
		private static final ThreadLocal<Boolean> SUPPRESS_OUTPUT = ThreadLocal.withInitial(() -> false);

		private final OutputStream original;
		private final String category;
		private final StringBuilder lineBuffer = new StringBuilder();

		DAPTeeOutputStream(OutputStream original, String category) {
			this.original = original;
			this.category = category;
		}

		static void setSuppressOutput(boolean suppress) {
			SUPPRESS_OUTPUT.set(suppress);
		}

		@Override
		public void write(int b) throws IOException {
			if(SUPPRESS_OUTPUT.get()) {
				return;
			}
			original.write(b);
			char c = (char) b;
			lineBuffer.append(c);
			if(c == '\n') {
				flushBuffer();
			}
		}

		@Override
		public void write(byte[] buf, int off, int len) throws IOException {
			if(SUPPRESS_OUTPUT.get()) {
				return;
			}
			original.write(buf, off, len);
			String s = new String(buf, off, len, StandardCharsets.UTF_8);
			lineBuffer.append(s);
			if(s.contains("\n")) {
				flushBuffer();
			}
		}

		@Override
		public void flush() throws IOException {
			if(SUPPRESS_OUTPUT.get()) {
				return;
			}
			original.flush();
			if(lineBuffer.length() > 0) {
				flushBuffer();
			}
		}

		private void flushBuffer() {
			if(client != null && lineBuffer.length() > 0) {
				OutputEventArguments output = new OutputEventArguments();
				output.setCategory(category);
				output.setOutput(lineBuffer.toString());
				client.output(output);
			}
			lineBuffer.setLength(0);
		}
	}

	private void sendOutput(String category, String text) {
		if(client != null) {
			OutputEventArguments output = new OutputEventArguments();
			output.setCategory(category);
			output.setOutput(text + "\n");
			client.output(output);
		}
	}

	/**
	 * Bridges the DebugListener interface to DAP client events.
	 */
	private final class DAPDebugListener implements DebugListener {

		@Override
		public void onPaused(PausedState state) {
			int dapId = debugCtx.getCurrentDapThreadId();
			pausedStates.put(dapId, state);
			if(state instanceof Script.DebugSnapshot snapshot) {
				asyncSnapshots.put(dapId, snapshot);
			}
			StoppedEventArguments stopped = new StoppedEventArguments();
			stopped.setThreadId(dapId);
			if(state.isExceptionPause()) {
				stopped.setReason(StoppedEventArgumentsReason.EXCEPTION);
				stopped.setText(state.getPauseException().getMessage());
			} else if(debugCtx.getStepMode() != DebugContext.StepMode.NONE) {
				stopped.setReason(StoppedEventArgumentsReason.STEP);
			} else {
				stopped.setReason(StoppedEventArgumentsReason.BREAKPOINT);
			}
			client.stopped(stopped);
		}

		@Override
		public void onResumed() {
			int dapId = debugCtx.getCurrentDapThreadId();
			ContinuedEventArguments continued = new ContinuedEventArguments();
			continued.setThreadId(dapId);
			continued.setAllThreadsContinued(false);
			client.continued(continued);
		}

		@Override
		public void onCompleted() {
			// Completion is handled by executionCompleted(), called from
			// runScript/resumeOnThread when they detect eval finished.
		}

		@Override
		public void onThreadStarted(int dapThreadId, String name) {
			if(client != null) {
				ThreadEventArguments args = new ThreadEventArguments();
				args.setThreadId(dapThreadId);
				args.setReason(ThreadEventArgumentsReason.STARTED);
				client.thread(args);
			}
		}

		@Override
		public void onThreadExited(int dapThreadId) {
			pausedStates.remove(dapThreadId);
			asyncSnapshots.remove(dapThreadId);
			if(client != null) {
				ThreadEventArguments args = new ThreadEventArguments();
				args.setThreadId(dapThreadId);
				args.setReason(ThreadEventArgumentsReason.EXITED);
				client.thread(args);
			}
		}
	}
}
