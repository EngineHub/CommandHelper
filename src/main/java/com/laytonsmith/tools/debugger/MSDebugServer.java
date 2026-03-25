package com.laytonsmith.tools.debugger;

import com.laytonsmith.PureUtilities.CommandExecutor;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
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
import org.eclipse.lsp4j.debug.ExitedEventArguments;
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
import org.eclipse.lsp4j.debug.StepInTarget;
import org.eclipse.lsp4j.debug.StepInTargetsArguments;
import org.eclipse.lsp4j.debug.StepInTargetsResponse;
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
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
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

	/** Default bind address for the DAP TCP listener. */
	public static final String DEFAULT_BIND_ADDRESS = "127.0.0.1";

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
	private Socket clientSocket;
	private final CountDownLatch suspendLatch = new CountDownLatch(1);
	private final CountDownLatch completionLatch = new CountDownLatch(1);
	private boolean suspendOnStart = false;
	private boolean managedExecution = false;
	private volatile boolean scriptCompleted = false;
	private PrintStream originalOut;
	private PrintStream originalErr;
	private DebugSecurity securityMode = DebugSecurity.KEYPAIR;
	private DebugAuthenticator authenticator;
	private SSLContext sslContext;

	/**
	 * Starts a DAP TCP listener on the given port with an explicit threading mode.
	 *
	 * @param port The TCP port to listen on.
	 * @param bindAddress The address to bind to (e.g. "127.0.0.1" or "0.0.0.0").
	 * @param environment The Environment to debug.
	 * @param suspend If true, waits for configurationDone before execution.
	 * @param threadingMode The threading mode for the main execution thread.
	 * @param security The security mode for incoming connections.
	 * @param authorizedKeysFile The file containing authorized public keys (required
	 *     when security is KEYPAIR, ignored when NONE).
	 * @return The Environment, which may have been cloned to include the DebugContext.
	 * @throws IOException If the server socket cannot be opened or the authorized keys
	 *     file cannot be read.
	 */
	public Environment startListening(int port, String bindAddress, Environment environment,
			boolean suspend, DebugContext.ThreadingMode threadingMode,
			DebugSecurity security, File authorizedKeysFile) throws IOException {
		this.env = environment;
		this.suspendOnStart = suspend;
		this.managedExecution = true;
		this.securityMode = security;

		if(security == DebugSecurity.KEYPAIR) {
			if(authorizedKeysFile == null) {
				throw new IOException("KEYPAIR security requires an authorized keys file");
			}
			this.authenticator = new DebugAuthenticator(authorizedKeysFile);
			try {
				this.sslContext = createEphemeralSSLContext();
			} catch(Exception e) {
				throw new IOException("Failed to create TLS context", e);
			}
		}

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

		if(security == DebugSecurity.NONE && !"127.0.0.1".equals(bindAddress)
				&& !"localhost".equals(bindAddress)) {
			throw new IOException("NONE security mode can only bind to localhost (127.0.0.1). "
					+ "Use KEYPAIR security for remote connections.");
		}

		serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);
		serverSocket.bind(new java.net.InetSocketAddress(
				InetAddress.getByName(bindAddress), port), 1);
		StreamUtils.GetSystemErr().println("MethodScript debugger listening on " + bindAddress + ":"
				+ serverSocket.getLocalPort() + " (security: " + security + ")");
		if(suspend) {
			StreamUtils.GetSystemErr().println("Waiting for debugger to connect before execution...");
		}

		java.lang.Thread acceptThread = new java.lang.Thread(() -> {
			while(!serverSocket.isClosed()) {
				try {
					Socket socket = serverSocket.accept();
					socket.setKeepAlive(true);
					clientSocket = socket;
					InputStream in = socket.getInputStream();
					OutputStream out = socket.getOutputStream();

					if(securityMode == DebugSecurity.KEYPAIR && authenticator != null) {
						if(!authenticator.authenticate(in, out)) {
							StreamUtils.GetSystemErr().println("Debug client authentication failed, "
									+ "closing connection.");
							socket.close();
							StreamUtils.GetSystemErr().println("Waiting for another connection...");
							continue;
						}
						StreamUtils.GetSystemErr().println("Debug client authenticated successfully.");

						SSLSocketFactory sslFactory = sslContext.getSocketFactory();
						SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(
								socket,
								socket.getInetAddress().getHostAddress(),
								socket.getPort(),
								true);
						sslSocket.setUseClientMode(false);
						sslSocket.startHandshake();
						StreamUtils.GetSystemErr().println("TLS handshake completed.");
						clientSocket = sslSocket;
						in = sslSocket.getInputStream();
						out = sslSocket.getOutputStream();
					}

					Launcher<IDebugProtocolClient> launcher
							= DSPLauncher.createServerLauncher(this, in, out);
					client = launcher.getRemoteProxy();
					launcher.startListening().get();

					// DAP session ended (client disconnected or crashed).
					client = null;
					if(serverSocket.isClosed()) {
						break;
					}
					if(scriptCompleted) {
						// Script is done - no point accepting more connections.
						completionLatch.countDown();
						break;
					}
					if(suspendOnStart && debugCtx != null) {
						debugCtx.awaitReconnect();
						StreamUtils.GetSystemErr().println("Debug client disconnected. Execution "
								+ "suspended. Waiting for another connection...");
					} else {
						StreamUtils.GetSystemErr().println("Debug client disconnected. "
								+ "Waiting for another connection...");
					}
				} catch(IOException e) {
					if(!serverSocket.isClosed()) {
						StreamUtils.GetSystemErr().println("Debug server accept failed: "
								+ e.getMessage());
					}
				} catch(Exception e) {
					if(!serverSocket.isClosed()) {
						StreamUtils.GetSystemErr().println("Debug session error: " + e.getMessage());
					}
				}
			}
		}, "ms-debug-accept");
		acceptThread.setDaemon(true);
		acceptThread.start();

		Runtime.getRuntime().addShutdownHook(new java.lang.Thread(() -> {
			shutdown();
		}, "ms-debug-shutdown"));

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
	 * Returns true if a debugger has connected and sent the configurationDone request.
	 */
	public boolean isConfigurationDone() {
		return configurationDone;
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
		if(managedExecution) {
			// Embedded mode: keep the host's main thread as the debug main
			// thread. Register the execution thread as a background thread
			// with its own DAP ID so it can safely block on breakpoints.
			// Transfer the main thread's debug state (step mode, etc.) to
			// the execution thread.
			int dapId = debugCtx.registerThread(executionThread, "execution",
					debugCtx.getMainThread());
			if(client != null) {
				ThreadEventArguments tea = new ThreadEventArguments();
				tea.setThreadId(dapId);
				tea.setReason(ThreadEventArgumentsReason.STARTED);
				client.thread(tea);
			}
		} else {
			debugCtx.setMainThread(executionThread);
		}
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
	 * exited and terminated events to the client. The client is expected to
	 * respond with a disconnect request, which releases the completion latch.
	 */
	private void executionCompleted() {
		if(!managedExecution) {
			// CLI mode: the script is the entire program, so signal termination.
			scriptCompleted = true;
			restoreOutputRedirect();
			if(client != null) {
				ExitedEventArguments exitArgs = new ExitedEventArguments();
				exitArgs.setExitCode(0);
				client.exited(exitArgs);
				client.terminated(new TerminatedEventArguments());
			} else {
				completionLatch.countDown();
			}
		}
		// Embedded mode (managedExecution=true): script completion is normal.
		// The debug session stays alive for the next script execution.
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
	 * Returns true if a DAP client is currently connected.
	 */
	public boolean isClientConnected() {
		return client != null;
	}

	/**
	 * Shuts down the debug server, closing the TCP listener and unblocking
	 * any threads waiting for a debugger reconnection.
	 */
	public void shutdown() {
		if(debugCtx != null) {
			debugCtx.disconnect();
		}
		// Notify the client of termination before closing sockets, so
		// the DAP framework doesn't throw SocketExceptions mid-response.
		if(client != null) {
			try {
				client.terminated(new TerminatedEventArguments());
			} catch(Exception e) {
				// Client may already be gone
			}
			client = null;
		}
		completionLatch.countDown();
		try {
			if(clientSocket != null && !clientSocket.isClosed()) {
				clientSocket.close();
			}
		} catch(IOException e) {
			// Ignore close errors
		}
		try {
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch(IOException e) {
			// Ignore close errors
		}
	}

	/**
	 * Creates an ephemeral SSLContext with a self-signed certificate for
	 * encrypting DAP traffic after KEYPAIR authentication.
	 */
	private static SSLContext createEphemeralSSLContext() throws Exception {
		File tempKeystore = File.createTempFile("ms-debug-", ".p12");
		tempKeystore.delete(); // keytool requires the file not exist
		tempKeystore.deleteOnExit();
		String password = Long.toHexString(new SecureRandom().nextLong());
		String keytool = System.getProperty("java.home") + File.separator
				+ "bin" + File.separator + "keytool";

		java.io.ByteArrayOutputStream errStream = new java.io.ByteArrayOutputStream();
		int exitCode = new CommandExecutor(keytool,
				"-genkeypair",
				"-alias", "debug",
				"-keyalg", "RSA",
				"-keysize", "2048",
				"-validity", "1",
				"-dname", "CN=MethodScript Debug Server",
				"-keystore", tempKeystore.getAbsolutePath(),
				"-storepass", password,
				"-keypass", password,
				"-storetype", "PKCS12")
				.setSystemErr(errStream)
				.start().waitFor();
		if(exitCode != 0) {
			throw new IOException("keytool failed (exit " + exitCode + "): "
					+ errStream.toString("UTF-8").trim());
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		try(InputStream fis = Files.newInputStream(tempKeystore.toPath())) {
			keyStore.load(fis, password.toCharArray());
		} finally {
			tempKeystore.delete();
		}

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(
				KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, password.toCharArray());

		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(kmf.getKeyManagers(), null, new SecureRandom());
		return ctx;
	}

	@Override
	public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
		Capabilities caps = new Capabilities();
		caps.setSupportsConfigurationDoneRequest(true);
		caps.setSupportsEvaluateForHovers(true);
		caps.setSupportsConditionalBreakpoints(true);
		caps.setSupportsHitConditionalBreakpoints(true);
		caps.setSupportsLogPoints(true);
		caps.setSupportsStepInTargetsRequest(true);

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
	public CompletableFuture<Void> attach(Map<String, Object> args) {
		// In attach mode, the environment and debug context were already
		// set up by startListening(). Just signal that we're ready.
		client.initialized();
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
		configurationDone = true;
		suspendLatch.countDown();
		if(debugCtx != null) {
			debugCtx.reconnect();
		}
		if(programPath != null && !managedExecution) {
			startExecution();
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> disconnect(DisconnectArguments args) {
		if(debugCtx != null) {
			if(suspendOnStart && !scriptCompleted) {
				// Suspend mode: pause execution until a new debugger connects.
				// awaitReconnect() releases paused threads so they can block
				// in shouldPause() on the reconnect monitor.
				debugCtx.awaitReconnect();
			} else {
				debugCtx.disconnect();
			}
			if(!pausedStates.isEmpty()) {
				for(int threadId : pausedStates.keySet()) {
					resumeExecution(threadId);
				}
			}
		}
		suspendLatch.countDown();
		if(!suspendOnStart || scriptCompleted) {
			completionLatch.countDown();
		}
		// Don't call shutdown() - the accept loop will handle reconnection.
		// The Interpreter's finally block calls shutdown() when the main
		// thread is done.
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
				String logMessage = sbp.getLogMessage();
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
					debugBps.add(new Breakpoint(file, sbp.getLine(), condition, hitThreshold, logMessage));
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
					DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
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
					state.getUserCallDepth(), state.getPauseTarget(), -1);
			resumeExecution(threadId);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<Void> stepIn(StepInArguments args) {
		int threadId = args.getThreadId();
		PausedState state = pausedStates.get(threadId);
		if(state != null) {
			Integer targetId = args.getTargetId();
			int targetCol = (targetId != null && targetId >= 0) ? targetId : -1;
			debugCtx.setStepMode(threadId, DebugContext.StepMode.INTO,
					state.getUserCallDepth(), state.getPauseTarget(), targetCol);
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
					state.getUserCallDepth(), state.getPauseTarget(), -1);
			resumeExecution(threadId);
		}
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public CompletableFuture<StepInTargetsResponse> stepInTargets(StepInTargetsArguments args) {
		StepInTargetsResponse response = new StepInTargetsResponse();
		// Find the paused state — stepInTargets provides frameId, but all frames share
		// the same paused state, so we use lastInspectedState (set by the most recent
		// stackTrace request, which VS Code always sends before stepInTargets).
		PausedState state = lastInspectedState;
		if(state != null) {
			List<PausedState.StepInTargetInfo> infos = state.getStepInTargets();
			StepInTarget[] targets = new StepInTarget[infos.size()];
			for(int i = 0; i < infos.size(); i++) {
				PausedState.StepInTargetInfo info = infos.get(i);
				StepInTarget sit = new StepInTarget();
				sit.setId(info.column());
				sit.setLabel(info.name());
				targets[i] = sit;
			}
			response.setTargets(targets);
		} else {
			response.setTargets(new StepInTarget[0]);
		}
		return CompletableFuture.completedFuture(response);
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

		@Override
		public void onLogPoint(String message) {
			sendOutput("console", message + "\n");
		}
	}
}
