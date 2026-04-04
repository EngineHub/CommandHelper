package com.laytonsmith.core;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Breakpoint;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.DebugContext;
import com.laytonsmith.core.environments.DebugListener;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.PausedState;
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.StaticTest;
import com.laytonsmith.tools.debugger.DebugSecurity;
import com.laytonsmith.tools.debugger.MSDebugServer;
import org.eclipse.lsp4j.debug.InitializeRequestArguments;
import org.eclipse.lsp4j.debug.NextArguments;
import org.eclipse.lsp4j.debug.ContinueArguments;
import org.eclipse.lsp4j.debug.SetBreakpointsArguments;
import org.eclipse.lsp4j.debug.Source;
import org.eclipse.lsp4j.debug.SourceBreakpoint;
import org.eclipse.lsp4j.debug.StoppedEventArguments;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.eclipse.lsp4j.debug.launch.DSPLauncher;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.lsp4j.debug.EvaluateArguments;
import org.eclipse.lsp4j.debug.EvaluateResponse;
import org.eclipse.lsp4j.debug.ScopesArguments;
import org.eclipse.lsp4j.debug.ScopesResponse;
import org.eclipse.lsp4j.debug.StackTraceArguments;
import org.eclipse.lsp4j.debug.StackTraceResponse;
import org.eclipse.lsp4j.debug.StepInArguments;
import org.eclipse.lsp4j.debug.StepOutArguments;
import org.eclipse.lsp4j.debug.ThreadEventArguments;
import org.eclipse.lsp4j.debug.ThreadsResponse;
import org.eclipse.lsp4j.debug.VariablesArguments;
import org.eclipse.lsp4j.debug.VariablesResponse;
import org.eclipse.lsp4j.debug.DisconnectArguments;
import org.eclipse.lsp4j.debug.StepInTargetsArguments;
import org.eclipse.lsp4j.debug.StepInTargetsResponse;
import org.eclipse.lsp4j.debug.SetExceptionBreakpointsArguments;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Tests for the debug infrastructure: breakpoints, step modes, variable inspection,
 * call stack inspection, and disconnect.
 */
public class DebugInfrastructureTest {

	private static Environment baseEnv;
	private static Set<Class<? extends Environment.EnvironmentImpl>> envs;
	private static final File TEST_FILE = new File("test.ms");

	@BeforeClass
	public static void setUpClass() throws Exception {
		Implementation.setServerType(Implementation.Type.TEST);
		StaticTest.InstallFakeServerFrontend();
		baseEnv = Static.GenerateStandaloneEnvironment();
		baseEnv = baseEnv.cloneAndAdd(new CommandHelperEnvironment());
		envs = Environment.getDefaultEnvClasses();
		envs.add(CommandHelperEnvironment.class);
	}

	@AfterClass
	public static void tearDownClass() {
		Implementation.forceServerType(null);
	}

	@Before
	public void setUp() {
		baseEnv.getEnv(GlobalEnv.class).GetVarList().clear();
	}

	/**
	 * Compiles and executes a script with the given DebugContext attached.
	 * Returns null if the debugger paused execution.
	 */
	private Mixed executeWithDebugger(String script, DebugContext debugCtx) throws Exception {
		Environment testEnv = baseEnv.cloneAndAdd(debugCtx);
		testEnv.getEnv(GlobalEnv.class).GetVarList().clear();
		StaticAnalysis analysis = new StaticAnalysis(true);
		analysis.setLocalEnable(false);
		TokenStream tokens = MethodScriptCompiler.lex(script, testEnv, TEST_FILE, true);
		ParseTree tree = MethodScriptCompiler.compile(tokens, testEnv, envs, analysis);
		return MethodScriptCompiler.execute(tree, testEnv, null, null);
	}

	/**
	 * Test DebugListener that records all events for later assertion.
	 */
	private static class TestDebugListener implements DebugListener {
		final List<PausedState> pauses = new ArrayList<>();
		final List<String> logMessages = new ArrayList<>();
		boolean completed = false;
		int resumeCount = 0;

		@Override
		public void onPaused(PausedState state) {
			pauses.add(state);
		}

		@Override
		public void onResumed() {
			resumeCount++;
		}

		@Override
		public void onCompleted() {
			completed = true;
		}

		@Override
		public void onLogPoint(String message) {
			logMessages.add(message);
		}

		Script.DebugSnapshot snapshot(int index) {
			return (Script.DebugSnapshot) pauses.get(index);
		}
	}

	@Test
	public void testBreakpointHitAndContinue() throws Exception {
		String script = "@x = 1\n@y = @x + 1\n@z = @y + 1";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		Mixed result = executeWithDebugger(script, debugCtx);

		// Should have paused
		assertTrue(Script.isDebuggerPaused(result));
		assertEquals(1, listener.pauses.size());
		assertEquals(2, listener.pauses.get(0).getPauseTarget().line());

		// Resume with continue (NONE = run freely)
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(0));

		assertTrue(listener.completed);
		assertEquals(1, listener.pauses.size());
	}

	@Test
	public void testVariableInspection() throws Exception {
		String script = "@x = 42\n@y = 'hello'\n@z = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);

		assertEquals(1, listener.pauses.size());
		Map<String, Mixed> vars = listener.pauses.get(0).getVariables();
		// @x and @y should be set; @z has not executed yet
		assertEquals("42", vars.get("@x").val());
		assertEquals("hello", vars.get("@y").val());

		// Clean up: continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(0));
		assertTrue(listener.completed);
	}

	@Test
	public void testMultipleBreakpoints() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 1));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);

		// First breakpoint
		assertEquals(1, listener.pauses.size());
		assertEquals(1, listener.pauses.get(0).getPauseTarget().line());

		// Continue to next breakpoint
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(0));

		assertEquals(2, listener.pauses.size());
		assertEquals(3, listener.pauses.get(1).getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));
		assertTrue(listener.completed);
	}

	@Test
	public void testDisconnect() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 1));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);

		// Hit first breakpoint
		assertEquals(1, listener.pauses.size());

		// Disconnect and resume - should run to completion without more pauses
		debugCtx.disconnect();
		Script.resumeEval(listener.snapshot(0));

		assertTrue(listener.completed);
		assertEquals(1, listener.pauses.size());
	}

	@Test
	public void testStepIntoProc() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()\n@done = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 5));

		executeWithDebugger(script, debugCtx);

		// Should pause at line 5 (_foo() call)
		assertEquals(1, listener.pauses.size());
		PausedState pause1 = listener.pauses.get(0);
		assertEquals(5, pause1.getPauseTarget().line());

		// Step into
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID, DebugContext.StepMode.INTO,
				pause1.getUserCallDepth(), pause1.getPauseTarget(), -1);
		Script.resumeEval(listener.snapshot(0));

		// Should pause inside the proc body (line 2)
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertEquals(2, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));
		assertTrue(listener.completed);
	}

	@Test
	public void testStepOverProc() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()\n@done = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 5));

		executeWithDebugger(script, debugCtx);

		PausedState pause1 = listener.pauses.get(0);
		assertEquals(5, pause1.getPauseTarget().line());

		// Step over
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID, DebugContext.StepMode.OVER,
				pause1.getUserCallDepth(), pause1.getPauseTarget(), -1);
		Script.resumeEval(listener.snapshot(0));

		// Should pause at line 6, having skipped into the proc
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertEquals(6, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));
		assertTrue(listener.completed);
	}

	@Test
	public void testStepOut() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()\n@done = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		executeWithDebugger(script, debugCtx);

		// Should pause inside proc at line 2
		assertEquals(1, listener.pauses.size());
		PausedState pause1 = listener.pauses.get(0);
		assertEquals(2, pause1.getPauseTarget().line());
		int depthInside = pause1.getUserCallDepth();
		assertTrue("Should be inside proc (depth > 0)", depthInside > 0);

		// Step out
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID, DebugContext.StepMode.OUT,
				depthInside, pause1.getPauseTarget(), -1);
		Script.resumeEval(listener.snapshot(0));

		// Should pause after returning from the proc
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertTrue("Should be at lower depth after step out",
				pause2.getUserCallDepth() < depthInside);

		// Continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));
		assertTrue(listener.completed);
	}

	@Test
	public void testCallStackInspection() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		executeWithDebugger(script, debugCtx);

		assertEquals(1, listener.pauses.size());
		PausedState pause = listener.pauses.get(0);
		List<StackTraceFrame> callStack = pause.getCallStack();
		assertFalse("Call stack should not be empty inside proc", callStack.isEmpty());
		// Procedure names in the stack trace include the "proc " prefix
		assertTrue("Expected proc name to contain _foo",
				callStack.get(0).getProcedureName().contains("_foo"));

		// Clean up
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(0));
	}

	@Test
	public void testResumeCount() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 1));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);
		assertEquals(0, listener.resumeCount);

		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(0));
		assertEquals(1, listener.resumeCount);

		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));
		assertEquals(2, listener.resumeCount);
		assertTrue(listener.completed);
	}

	@Test
	public void testSyncModeKeepsSameThread() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";

		// Track which thread hits the breakpoint callback
		AtomicLong pausedThreadId = new AtomicLong(-1);
		AtomicReference<Thread> pausedThread = new AtomicReference<>();
		CountDownLatch pauseReached = new CountDownLatch(1);
		CountDownLatch resumeReached = new CountDownLatch(1);

		TestDebugListener listener = new TestDebugListener() {
			@Override
			public void onPaused(PausedState state) {
				super.onPaused(state);
				pausedThreadId.set(Thread.currentThread().getId());
				pausedThread.set(Thread.currentThread());
				pauseReached.countDown();
			}

			@Override
			public void onResumed() {
				super.onResumed();
				resumeReached.countDown();
			}
		};

		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.SYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		// Run the script on a dedicated thread (sync mode blocks, so we can't
		// run it on the test thread).
		AtomicLong executionThreadId = new AtomicLong(-1);
		AtomicReference<Mixed> resultRef = new AtomicReference<>();
		AtomicReference<Exception> errorRef = new AtomicReference<>();
		Thread execThread = new Thread(() -> {
			executionThreadId.set(Thread.currentThread().getId());
			try {
				resultRef.set(executeWithDebugger(script, debugCtx));
			} catch(Exception e) {
				errorRef.set(e);
			}
		});
		debugCtx.setMainThread(execThread);
		execThread.start();

		// Wait for the breakpoint to be hit
		assertTrue("Breakpoint was not hit within timeout",
				pauseReached.await(10, TimeUnit.SECONDS));

		// The onPaused callback should have been called ON the execution thread
		// (sync mode blocks in place, so the callback runs on the interpreter thread)
		assertEquals("onPaused should run on the interpreter thread",
				executionThreadId.get(), pausedThreadId.get());

		// Resume the paused thread (not the test thread)
		debugCtx.getThreadState(pausedThread.get())
				.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		debugCtx.getThreadState(pausedThread.get()).resume();
		assertTrue("Resume was not processed within timeout",
				resumeReached.await(10, TimeUnit.SECONDS));

		execThread.join(10_000);
		assertFalse("Execution thread should have finished", execThread.isAlive());
		if(errorRef.get() != null) {
			throw errorRef.get();
		}

		// In sync mode the script runs to completion and returns a real result, not DEBUGGER_PAUSED
		assertFalse("Sync mode should not return DEBUGGER_PAUSED",
				Script.isDebuggerPaused(resultRef.get()));
		assertTrue(listener.completed);
		assertEquals(1, listener.resumeCount);
	}

	@Test
	public void testBackgroundThreadUsesSyncMode() throws Exception {
		// In ASYNCHRONOUS mode, the main thread uses async behavior, but threads
		// spawned by x_new_thread should use sync mode (block in place) since
		// they are not the main execution thread.
		String script = "x_new_thread('bg', closure() {\n\t@a = 1\n\t@b = 2\n})";

		AtomicLong pausedThreadId = new AtomicLong(-1);
		AtomicReference<Thread> pausedThread = new AtomicReference<>();
		CountDownLatch pauseReached = new CountDownLatch(1);
		CountDownLatch resumeReached = new CountDownLatch(1);

		TestDebugListener listener = new TestDebugListener() {
			@Override
			public void onPaused(PausedState state) {
				super.onPaused(state);
				pausedThreadId.set(Thread.currentThread().getId());
				pausedThread.set(Thread.currentThread());
				pauseReached.countDown();
			}

			@Override
			public void onResumed() {
				super.onResumed();
				resumeReached.countDown();
			}
		};

		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		// Breakpoint inside the closure (line 2 = @a = 1, runs on background thread)
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		// Main thread runs x_new_thread and returns without hitting breakpoints
		Mixed result = executeWithDebugger(script, debugCtx);
		assertFalse("Main thread should not be paused",
				Script.isDebuggerPaused(result));

		// Wait for the background thread to hit the breakpoint
		assertTrue("Background thread breakpoint was not hit within timeout",
				pauseReached.await(10, TimeUnit.SECONDS));

		// onPaused was called ON the background thread (sync mode blocks in place),
		// not on the main/test thread
		assertNotEquals("onPaused should run on the background thread",
				Thread.currentThread().getId(), pausedThreadId.get());

		// Resume the background thread (must target the actual paused thread, not the test thread)
		debugCtx.getThreadState(pausedThread.get())
				.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		debugCtx.getThreadState(pausedThread.get()).resume();
		assertTrue("Resume was not processed within timeout",
				resumeReached.await(10, TimeUnit.SECONDS));

		assertEquals(1, listener.resumeCount);
	}

	// ---- DAP integration test helpers ----

	/**
	 * Helper that sets up a DAP server, connects a mock client, compiles a script,
	 * sets breakpoints, and returns everything needed to drive a DAP test scenario.
	 */
	private static class DAPTestHarness implements AutoCloseable {
		final MSDebugServer server;
		final IDebugProtocolServer proxy;
		final Socket clientSocket;
		final AtomicInteger stopCount = new AtomicInteger(0);
		final List<CountDownLatch> stopLatches = new ArrayList<>();
		final CountDownLatch terminated = new CountDownLatch(1);
		final AtomicReference<StoppedEventArguments> lastStopArgs = new AtomicReference<>();
		final List<ThreadEventArguments> threadEvents
				= Collections.synchronizedList(new ArrayList<>());
		final CountDownLatch threadExited = new CountDownLatch(1);
		private boolean closed = false;
		private boolean managed = false;

		DAPTestHarness(String script, int[] breakpointLines,
				DebugContext.ThreadingMode mode) throws Exception {
			this(script, toSourceBreakpoints(breakpointLines), mode, false);
		}

		DAPTestHarness(String script, int[] breakpointLines,
				DebugContext.ThreadingMode mode, boolean managedExecution) throws Exception {
			this(script, toSourceBreakpoints(breakpointLines), mode, managedExecution);
		}

		DAPTestHarness(String script, SourceBreakpoint[] breakpoints,
				DebugContext.ThreadingMode mode, boolean managedExecution) throws Exception {
			this.managed = managedExecution;
			// Create enough latches for expected stops
			for(int i = 0; i < 10; i++) {
				stopLatches.add(new CountDownLatch(1));
			}

			IDebugProtocolClient mockClient = new IDebugProtocolClient() {
				@Override
				public void stopped(StoppedEventArguments args) {
					lastStopArgs.set(args);
					int idx = stopCount.getAndIncrement();
					if(idx < stopLatches.size()) {
						stopLatches.get(idx).countDown();
					}
				}

				@Override
				public void terminated(org.eclipse.lsp4j.debug.TerminatedEventArguments args) {
					terminated.countDown();
				}

				@Override
				public void thread(ThreadEventArguments args) {
					threadEvents.add(args);
					if("exited".equals(args.getReason())) {
						threadExited.countDown();
					}
				}
			};

			server = new MSDebugServer();
			if(managedExecution) {
				server.setManagedExecution(true);
			}
			Environment env = Static.GenerateStandaloneEnvironment();
			env = env.cloneAndAdd(new CommandHelperEnvironment());
			env = server.startListening(0, MSDebugServer.DEFAULT_BIND_ADDRESS,
					env, false, mode, DebugSecurity.NONE, null);
			int port = server.getPort();
			assertTrue("Server should be listening", port > 0);

			clientSocket = new Socket("localhost", port);
			Launcher<IDebugProtocolServer> clientLauncher = DSPLauncher.createClientLauncher(
					mockClient, clientSocket.getInputStream(), clientSocket.getOutputStream());
			proxy = clientLauncher.getRemoteProxy();
			clientLauncher.startListening();

			proxy.initialize(new InitializeRequestArguments()).get(5, TimeUnit.SECONDS);

			// Set breakpoints
			sendBreakpoints(breakpoints);

			// Compile and run
			StaticAnalysis analysis = new StaticAnalysis(true);
			analysis.setLocalEnable(false);
			TokenStream tokens = MethodScriptCompiler.lex(script, env, TEST_FILE, true);
			ParseTree tree = MethodScriptCompiler.compile(tokens, env, envs, analysis);
			server.runScript(tree, env, null);
		}

		static SourceBreakpoint[] toSourceBreakpoints(int[] lines) {
			SourceBreakpoint[] sbps = new SourceBreakpoint[lines.length];
			for(int i = 0; i < lines.length; i++) {
				sbps[i] = new SourceBreakpoint();
				sbps[i].setLine(lines[i]);
			}
			return sbps;
		}

		void sendBreakpoints(SourceBreakpoint[] sbps) throws Exception {
			SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
			Source source = new Source();
			source.setPath(TEST_FILE.getPath());
			bpArgs.setSource(source);
			bpArgs.setBreakpoints(sbps);
			proxy.setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);
		}

		void setBreakpoints(int[] lines) throws Exception {
			sendBreakpoints(toSourceBreakpoints(lines));
		}

		void setBreakpointsWithConditions(int[] lines, String[] conditions) throws Exception {
			SourceBreakpoint[] sbps = toSourceBreakpoints(lines);
			for(int i = 0; i < sbps.length; i++) {
				if(conditions != null && i < conditions.length && conditions[i] != null) {
					sbps[i].setCondition(conditions[i]);
				}
			}
			sendBreakpoints(sbps);
		}

		void setBreakpointsWithHitCounts(int[] lines, String[] hitCounts) throws Exception {
			SourceBreakpoint[] sbps = toSourceBreakpoints(lines);
			for(int i = 0; i < sbps.length; i++) {
				if(hitCounts != null && i < hitCounts.length && hitCounts[i] != null) {
					sbps[i].setHitCondition(hitCounts[i]);
				}
			}
			sendBreakpoints(sbps);
		}

		boolean awaitStop(int index, long seconds) throws InterruptedException {
			return stopLatches.get(index).await(seconds, TimeUnit.SECONDS);
		}

		int lastStoppedThreadId() {
			StoppedEventArguments args = lastStopArgs.get();
			return args != null ? args.getThreadId() : 1;
		}

		void stepOver() throws Exception {
			NextArguments args = new NextArguments();
			args.setThreadId(lastStoppedThreadId());
			proxy.next(args).get(5, TimeUnit.SECONDS);
		}

		void continue_() throws Exception {
			ContinueArguments args = new ContinueArguments();
			args.setThreadId(lastStoppedThreadId());
			proxy.continue_(args).get(5, TimeUnit.SECONDS);
		}

		void stepIn() throws Exception {
			StepInArguments args = new StepInArguments();
			args.setThreadId(lastStoppedThreadId());
			proxy.stepIn(args).get(5, TimeUnit.SECONDS);
		}

		void stepOut() throws Exception {
			StepOutArguments args = new StepOutArguments();
			args.setThreadId(lastStoppedThreadId());
			proxy.stepOut(args).get(5, TimeUnit.SECONDS);
		}

		ThreadsResponse threads() throws Exception {
			return proxy.threads().get(5, TimeUnit.SECONDS);
		}

		StackTraceResponse stackTrace() throws Exception {
			StackTraceArguments args = new StackTraceArguments();
			args.setThreadId(lastStoppedThreadId());
			return proxy.stackTrace(args).get(5, TimeUnit.SECONDS);
		}

		ScopesResponse scopes() throws Exception {
			StackTraceResponse st = stackTrace();
			ScopesArguments args = new ScopesArguments();
			args.setFrameId(st.getStackFrames()[0].getId());
			return proxy.scopes(args).get(5, TimeUnit.SECONDS);
		}

		VariablesResponse variables(int variablesReference) throws Exception {
			VariablesArguments args = new VariablesArguments();
			args.setVariablesReference(variablesReference);
			return proxy.variables(args).get(5, TimeUnit.SECONDS);
		}

		EvaluateResponse evaluate(String expression) throws Exception {
			EvaluateArguments args = new EvaluateArguments();
			args.setExpression(expression);
			args.setContext("repl");
			StackTraceResponse st = stackTrace();
			args.setFrameId(st.getStackFrames()[0].getId());
			return proxy.evaluate(args).get(5, TimeUnit.SECONDS);
		}

		void disconnect() throws Exception {
			DisconnectArguments args = new DisconnectArguments();
			proxy.disconnect(args).get(5, TimeUnit.SECONDS);
		}

		StepInTargetsResponse stepInTargets() throws Exception {
			StackTraceResponse st = stackTrace();
			StepInTargetsArguments args = new StepInTargetsArguments();
			args.setFrameId(st.getStackFrames()[0].getId());
			return proxy.stepInTargets(args).get(5, TimeUnit.SECONDS);
		}

		void setExceptionBreakpoints(String... filters) throws Exception {
			SetExceptionBreakpointsArguments args = new SetExceptionBreakpointsArguments();
			args.setFilters(filters);
			proxy.setExceptionBreakpoints(args).get(5, TimeUnit.SECONDS);
		}

		@Override
		public void close() throws Exception {
			if(!closed) {
				closed = true;
				clientSocket.close();
				server.shutdown();
			}
		}
	}

	// ---- DAP integration tests (each runs in both sync and async modes) ----

	/**
	 * Step-over a simple 3-line script: hit breakpoint at line 1, step over to
	 * line 2, then continue to completion.
	 */
	@Test
	public void testDAPStepOverSync() throws Exception {
		dapStepOverSimple(DebugContext.ThreadingMode.SYNCHRONOUS);
	}

	@Test
	public void testDAPStepOverAsync() throws Exception {
		dapStepOverSimple(DebugContext.ThreadingMode.ASYNCHRONOUS);
	}

	private void dapStepOverSimple(DebugContext.ThreadingMode mode) throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1}, mode)) {
			assertTrue("Breakpoint at line 1 was not hit",
					h.awaitStop(0, 5));
			h.stepOver();
			assertTrue("Step-over should pause at next line",
					h.awaitStop(1, 5));
			h.continue_();
			assertTrue("Script should complete",
					h.terminated.await(5, TimeUnit.SECONDS));
		}
	}

	/**
	 * Step-over a line that spawns a new thread (x_new_thread). The debugger
	 * should pause at the next line after x_new_thread, not run to completion.
	 */
	@Test
	public void testDAPStepOverNewThreadSync() throws Exception {
		dapStepOverNewThread(DebugContext.ThreadingMode.SYNCHRONOUS);
	}

	@Test
	public void testDAPStepOverNewThreadAsync() throws Exception {
		dapStepOverNewThread(DebugContext.ThreadingMode.ASYNCHRONOUS);
	}

	private void dapStepOverNewThread(DebugContext.ThreadingMode mode) throws Exception {
		String script = "sys_out('before')\n"
				+ "x_new_thread('bg', closure() {\n"
				+ "  while(true) {\n"
				+ "    sleep(0.1)\n"
				+ "  }\n"
				+ "})\n"
				+ "sys_out('after')";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1}, mode)) {
			assertTrue("Breakpoint at line 1 was not hit",
					h.awaitStop(0, 5));
			h.stepOver();
			assertTrue("Step-over should pause at x_new_thread line",
					h.awaitStop(1, 5));
			h.stepOver();
			assertTrue("Step-over x_new_thread should pause at next line",
					h.awaitStop(2, 5));
		}
	}

	@Test
	public void testLogPoint() throws Exception {
		String script = "@x = 42\n@y = @x + 1\n@z = @y + 1";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2, null, 0, "x is {@x}"));

		executeWithDebugger(script, debugCtx);

		// Log point should not cause a pause
		assertEquals(0, listener.pauses.size());
		// But should have logged the message
		assertEquals(1, listener.logMessages.size());
		assertEquals("x is 42", listener.logMessages.get(0));
		assertTrue(listener.completed);
	}

	@Test
	public void testStepIntoTargetSkipsEarlierCall() throws Exception {
		String script = "proc _bar() {\n"
				+ "\treturn(10)\n"
				+ "}\n"
				+ "proc _foo() {\n"
				+ "\treturn(20)\n"
				+ "}\n"
				+ "@result = _bar() + _foo()";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 7));

		executeWithDebugger(script, debugCtx);

		// Should pause at line 7
		assertEquals(1, listener.pauses.size());
		PausedState pause1 = listener.pauses.get(0);
		assertEquals(7, pause1.getPauseTarget().line());

		// Check step-in targets include both procs
		List<PausedState.StepInTargetInfo> targets = pause1.getStepInTargets();
		PausedState.StepInTargetInfo fooTarget = null;
		for(PausedState.StepInTargetInfo t : targets) {
			if("_foo".equals(t.name())) {
				fooTarget = t;
			}
		}
		assertNotNull("Should find _foo as step-in target", fooTarget);

		// Step into _foo specifically — should skip _bar entirely
		debugCtx.getThreadState().setStepMode(DebugContext.StepMode.INTO,
				pause1.getUserCallDepth(), pause1.getPauseTarget(), fooTarget.column());
		Script.resumeEval(listener.snapshot(0));

		// Should pause inside _foo (line 5), NOT _bar (line 2)
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertEquals(5, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));
		assertTrue(listener.completed);
	}

	@Test
	public void testStepIntoTargetThenBreakpointRefires() throws Exception {
		String script = "proc _bar() {\n"
				+ "\treturn(10)\n"
				+ "}\n"
				+ "proc _foo() {\n"
				+ "\treturn(20)\n"
				+ "}\n"
				+ "@result = _bar() + _foo()";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener, DebugContext.ThreadingMode.ASYNCHRONOUS,
				Thread.currentThread());
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 7));

		executeWithDebugger(script, debugCtx);

		// Pause 1: line 7 breakpoint
		assertEquals(1, listener.pauses.size());
		PausedState pause1 = listener.pauses.get(0);

		// Find _bar target and step into it
		List<PausedState.StepInTargetInfo> targets = pause1.getStepInTargets();
		PausedState.StepInTargetInfo barTarget = null;
		for(PausedState.StepInTargetInfo t : targets) {
			if("_bar".equals(t.name())) {
				barTarget = t;
			}
		}
		assertNotNull("Should find _bar as step-in target", barTarget);

		debugCtx.getThreadState().setStepMode(DebugContext.StepMode.INTO,
				pause1.getUserCallDepth(), pause1.getPauseTarget(), barTarget.column());
		Script.resumeEval(listener.snapshot(0));

		// Pause 2: inside _bar (line 2)
		assertEquals(2, listener.pauses.size());
		assertEquals(2, listener.pauses.get(1).getPauseTarget().line());

		// Continue from _bar — breakpoint on line 7 should re-fire
		// (targeted step-into is complete, normal breakpoint behavior resumes)
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(1));

		// Pause 3: breakpoint at line 7 fires again (back from _bar, _foo not yet called)
		assertEquals(3, listener.pauses.size());
		assertEquals(7, listener.pauses.get(2).getPauseTarget().line());

		// Now step into _foo
		PausedState pause3 = listener.pauses.get(2);
		targets = pause3.getStepInTargets();
		PausedState.StepInTargetInfo fooTarget = null;
		for(PausedState.StepInTargetInfo t : targets) {
			if("_foo".equals(t.name())) {
				fooTarget = t;
			}
		}
		assertNotNull("Should find _foo as step-in target", fooTarget);

		debugCtx.getThreadState().setStepMode(DebugContext.StepMode.INTO,
				pause3.getUserCallDepth(), pause3.getPauseTarget(), fooTarget.column());
		Script.resumeEval(listener.snapshot(2));

		// Pause 4: inside _foo (line 5)
		assertEquals(4, listener.pauses.size());
		assertEquals(5, listener.pauses.get(3).getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.MAIN_THREAD_DAP_ID,
				DebugContext.StepMode.NONE, 0, Target.UNKNOWN, -1);
		Script.resumeEval(listener.snapshot(3));
		assertTrue(listener.completed);
	}

	// ---- Managed execution mode tests ----

	@Test
	public void testManagedModeStepOverSync() throws Exception {
		managedModeStepOver(DebugContext.ThreadingMode.SYNCHRONOUS);
	}

	@Test
	public void testManagedModeStepOverAsync() throws Exception {
		managedModeStepOver(DebugContext.ThreadingMode.ASYNCHRONOUS);
	}

	private void managedModeStepOver(DebugContext.ThreadingMode mode) throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1}, mode, true)) {
			assertTrue("Breakpoint at line 1 was not hit",
					h.awaitStop(0, 5));
			// In managed mode, the stopped thread should NOT be 1 (main)
			int stoppedId = h.lastStoppedThreadId();
			assertTrue("Execution thread should have a non-main DAP ID in managed mode",
					stoppedId > 1);
			h.stepOver();
			assertTrue("Step-over should pause at next line",
					h.awaitStop(1, 5));
			h.continue_();
			assertTrue("Script should complete",
					h.threadExited.await(5, TimeUnit.SECONDS));
		}
	}

	@Test
	public void testManagedModeThreadEvents() throws Exception {
		String script = "@x = 1\n@y = 2";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1},
				DebugContext.ThreadingMode.SYNCHRONOUS, true)) {
			assertTrue(h.awaitStop(0, 5));
			// In managed mode, a thread-started event should have been sent
			// for the execution thread
			boolean foundStarted = false;
			for(ThreadEventArguments tea : h.threadEvents) {
				if("started".equals(tea.getReason())) {
					foundStarted = true;
					assertTrue("Execution thread DAP ID should be > 1",
							tea.getThreadId() > 1);
				}
			}
			assertTrue("Should have received thread-started event for execution thread",
					foundStarted);
			h.continue_();
			h.threadExited.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testManagedModeDisconnectKeepsServerAlive() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1},
				DebugContext.ThreadingMode.SYNCHRONOUS, true)) {
			assertTrue(h.awaitStop(0, 5));
			// Disconnect while paused - in managed mode, this should resume
			// execution and NOT terminate the server
			h.disconnect();
			// The script should complete (execution was resumed by disconnect)
			assertTrue("Script should complete after managed disconnect",
					h.threadExited.await(5, TimeUnit.SECONDS));
		}
	}

	// ---- Threads, stack trace, scopes, and variables via DAP ----

	@Test
	public void testDAPThreadsList() throws Exception {
		String script = "@x = 1\n@y = 2";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			ThreadsResponse resp = h.threads();
			assertNotNull(resp.getThreads());
			assertTrue("Should have at least one thread",
					resp.getThreads().length >= 1);
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPStackTrace() throws Exception {
		String script = "proc _inner() {\n"
				+ "\t@a = 1\n"
				+ "}\n"
				+ "proc _outer() {\n"
				+ "\t_inner()\n"
				+ "}\n"
				+ "_outer()";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			StackTraceResponse resp = h.stackTrace();
			org.eclipse.lsp4j.debug.StackFrame[] frames = resp.getStackFrames();
			assertTrue("Should have at least 2 frames",
					frames.length >= 2);
			assertEquals("proc _inner", frames[0].getName());
			assertEquals("proc _outer", frames[1].getName());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPScopesAndVariables() throws Exception {
		String script = "@x = 42\n@y = 'hello'\n@z = 3";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{3},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			ScopesResponse scopeResp = h.scopes();
			assertNotNull(scopeResp.getScopes());
			assertTrue("Should have at least one scope",
					scopeResp.getScopes().length >= 1);
			int localsRef = scopeResp.getScopes()[0].getVariablesReference();
			assertTrue("Locals scope should have a variables reference",
					localsRef > 0);
			VariablesResponse varResp = h.variables(localsRef);
			assertNotNull(varResp.getVariables());
			// Find @x and @y
			boolean foundX = false;
			boolean foundY = false;
			for(org.eclipse.lsp4j.debug.Variable v : varResp.getVariables()) {
				if("@x".equals(v.getName())) {
					assertEquals("42", v.getValue());
					foundX = true;
				}
				if("@y".equals(v.getName())) {
					assertEquals("hello", v.getValue());
					foundY = true;
				}
			}
			assertTrue("Should find @x in variables", foundX);
			assertTrue("Should find @y in variables", foundY);
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	// ---- CArray expansion via variables ----

	@Test
	public void testDAPVariablesIndexedArray() throws Exception {
		String script = "@arr = array(10, 20, 30)\n@y = 1";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			ScopesResponse scopeResp = h.scopes();
			int localsRef = scopeResp.getScopes()[0].getVariablesReference();
			VariablesResponse varResp = h.variables(localsRef);
			// Find @arr - it should be expandable
			org.eclipse.lsp4j.debug.Variable arrVar = null;
			for(org.eclipse.lsp4j.debug.Variable v : varResp.getVariables()) {
				if("@arr".equals(v.getName())) {
					arrVar = v;
					break;
				}
			}
			assertNotNull("Should find @arr", arrVar);
			assertTrue("Array should be expandable",
					arrVar.getVariablesReference() > 0);
			// Expand the array
			VariablesResponse children = h.variables(arrVar.getVariablesReference());
			assertEquals("Indexed array should have 3 elements",
					3, children.getVariables().length);
			assertEquals("10", children.getVariables()[0].getValue());
			assertEquals("20", children.getVariables()[1].getValue());
			assertEquals("30", children.getVariables()[2].getValue());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPVariablesAssociativeArray() throws Exception {
		String script = "@map = array(a: 'x', b: 'y')\n@z = 1";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			ScopesResponse scopeResp = h.scopes();
			int localsRef = scopeResp.getScopes()[0].getVariablesReference();
			VariablesResponse varResp = h.variables(localsRef);
			org.eclipse.lsp4j.debug.Variable mapVar = null;
			for(org.eclipse.lsp4j.debug.Variable v : varResp.getVariables()) {
				if("@map".equals(v.getName())) {
					mapVar = v;
					break;
				}
			}
			assertNotNull("Should find @map", mapVar);
			assertTrue("Assoc array should be expandable",
					mapVar.getVariablesReference() > 0);
			VariablesResponse children = h.variables(mapVar.getVariablesReference());
			assertEquals(2, children.getVariables().length);
			// Check keys
			boolean foundA = false;
			boolean foundB = false;
			for(org.eclipse.lsp4j.debug.Variable v : children.getVariables()) {
				if("a".equals(v.getName())) {
					assertEquals("x", v.getValue());
					foundA = true;
				}
				if("b".equals(v.getName())) {
					assertEquals("y", v.getValue());
					foundB = true;
				}
			}
			assertTrue("Should find key 'a'", foundA);
			assertTrue("Should find key 'b'", foundB);
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPVariablesNestedArray() throws Exception {
		String script = "@nested = array(array(1, 2), array(3, 4))\n@z = 1";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			ScopesResponse scopeResp = h.scopes();
			int localsRef = scopeResp.getScopes()[0].getVariablesReference();
			VariablesResponse varResp = h.variables(localsRef);
			org.eclipse.lsp4j.debug.Variable nestedVar = null;
			for(org.eclipse.lsp4j.debug.Variable v : varResp.getVariables()) {
				if("@nested".equals(v.getName())) {
					nestedVar = v;
					break;
				}
			}
			assertNotNull("Should find @nested", nestedVar);
			assertTrue("Nested array should be expandable",
					nestedVar.getVariablesReference() > 0);
			// Expand outer array
			VariablesResponse outerChildren = h.variables(nestedVar.getVariablesReference());
			assertEquals(2, outerChildren.getVariables().length);
			// First element should also be expandable
			assertTrue("Inner array should be expandable",
					outerChildren.getVariables()[0].getVariablesReference() > 0);
			// Expand inner array
			VariablesResponse innerChildren = h.variables(
					outerChildren.getVariables()[0].getVariablesReference());
			assertEquals(2, innerChildren.getVariables().length);
			assertEquals("1", innerChildren.getVariables()[0].getValue());
			assertEquals("2", innerChildren.getVariables()[1].getValue());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	// ---- Evaluate via DAP ----

	@Test
	public void testDAPEvaluateSimple() throws Exception {
		String script = "@x = 42\n@y = 2";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			EvaluateResponse resp = h.evaluate("1 + 2");
			assertEquals("3", resp.getResult());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPEvaluateVariable() throws Exception {
		String script = "@x = 42\n@y = 2";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			EvaluateResponse resp = h.evaluate("@x");
			assertEquals("42", resp.getResult());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPEvaluateError() throws Exception {
		String script = "@x = 42\n@y = 2";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			// Invalid expression should return error message, not throw
			EvaluateResponse resp = h.evaluate("this is not valid");
			assertNotNull(resp.getResult());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	// ---- Exception breakpoints via DAP ----

	@Test
	public void testDAPExceptionBreakpointsAll() throws Exception {
		String script = "@x = 1\ntry {\n\tthrow(Exception, 'test')\n} catch(Exception @e) {\n\t@y = 1\n}";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			// Pause at line 1 first, then set exception breakpoints before
			// the throw executes, avoiding the race condition.
			assertTrue(h.awaitStop(0, 5));
			h.setExceptionBreakpoints("all");
			h.setBreakpoints(new int[]{});
			h.continue_();
			// The throw should cause a stop
			assertTrue("Should break on caught exception",
					h.awaitStop(1, 5));
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPExceptionBreakpointsUncaught() throws Exception {
		String script = "@x = 1\ntry {\n\tthrow(Exception, 'test')\n} catch(Exception @e) {\n\t@y = 1\n}";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			// Pause at line 1 first, then set exception breakpoints before
			// the throw executes, avoiding the race condition.
			assertTrue(h.awaitStop(0, 5));
			h.setExceptionBreakpoints("uncaught");
			h.setBreakpoints(new int[]{});
			h.continue_();
			// Caught exception should NOT trigger a break with "uncaught" filter
			// Script should complete normally
			assertTrue("Script should complete without breaking",
					h.terminated.await(5, TimeUnit.SECONDS));
		}
	}

	// ---- Conditional breakpoints via DAP ----

	@Test
	public void testDAPConditionalBreakpoint() throws Exception {
		String script = "@x = 0\nforeach(range(5), @i,\n\t@x = @x + 1\n)";
		SourceBreakpoint sbp = new SourceBreakpoint();
		sbp.setLine(3);
		sbp.setCondition("@x == 3");
		try(DAPTestHarness h = new DAPTestHarness(script, new SourceBreakpoint[]{sbp},
				DebugContext.ThreadingMode.SYNCHRONOUS, false)) {
			assertTrue("Should hit conditional breakpoint",
					h.awaitStop(0, 5));
			h.stackTrace();
			EvaluateResponse response = h.evaluate("@x");
			assertEquals("3", response.getResult());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	// ---- Hit count breakpoints via DAP ----

	@Test
	public void testDAPHitCountBreakpoint() throws Exception {
		String script = "@x = 0\nforeach(range(5), @i,\n\t@x = @x + 1\n)";
		SourceBreakpoint sbp = new SourceBreakpoint();
		sbp.setLine(3);
		sbp.setHitCondition("4");
		try(DAPTestHarness h = new DAPTestHarness(script, new SourceBreakpoint[]{sbp},
				DebugContext.ThreadingMode.SYNCHRONOUS, false)) {
			assertTrue("Should hit breakpoint on 4th iteration",
					h.awaitStop(0, 5));
			h.stackTrace();
			EvaluateResponse response = h.evaluate("@x");
			assertEquals("3", response.getResult());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	// ---- Step in / step out via DAP ----

	@Test
	public void testDAPStepIn() throws Exception {
		String script = "proc _greet() {\n"
				+ "\t@msg = 'hi'\n"
				+ "}\n"
				+ "_greet()";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{4},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			h.stepIn();
			assertTrue("Step-in should pause inside proc",
					h.awaitStop(1, 5));
			// Verify we're inside the proc by checking stack trace
			StackTraceResponse stResp = h.stackTrace();
			org.eclipse.lsp4j.debug.StackFrame[] frames = stResp.getStackFrames();
			assertTrue("Should have multiple frames when inside proc",
					frames.length >= 2);
			assertEquals("proc _greet", frames[0].getName());
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPStepOut() throws Exception {
		String script = "proc _inner() {\n"
				+ "\t@a = 1\n"
				+ "\t@b = 2\n"
				+ "}\n"
				+ "@before = 0\n"
				+ "_inner()\n"
				+ "@after = 0";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{2},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			// We're inside _inner at line 2. Step out should return to caller.
			h.stepOut();
			assertTrue("Step-out should pause after returning from proc",
					h.awaitStop(1, 5));
			// Verify we're back at the top level
			StackTraceResponse stResp = h.stackTrace();
			org.eclipse.lsp4j.debug.StackFrame[] frames = stResp.getStackFrames();
			assertEquals("Should be back at top level (1 frame)",
					1, frames.length);
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	@Test
	public void testDAPStepInTargets() throws Exception {
		String script = "proc _bar() {\n"
				+ "\treturn(10)\n"
				+ "}\n"
				+ "proc _foo() {\n"
				+ "\treturn(20)\n"
				+ "}\n"
				+ "@result = _bar() + _foo()";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{7},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			StepInTargetsResponse resp = h.stepInTargets();
			assertNotNull(resp.getTargets());
			assertTrue("Should have at least 2 step-in targets",
					resp.getTargets().length >= 2);
			h.continue_();
			h.terminated.await(5, TimeUnit.SECONDS);
		}
	}

	// ---- Disconnect resumes execution ----

	@Test
	public void testDAPDisconnectResumesExecution() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		try(DAPTestHarness h = new DAPTestHarness(script, new int[]{1},
				DebugContext.ThreadingMode.SYNCHRONOUS)) {
			assertTrue(h.awaitStop(0, 5));
			h.disconnect();
			// Script should resume and complete after disconnect
			assertTrue("Script should complete after disconnect",
					h.terminated.await(5, TimeUnit.SECONDS));
		}
	}
}
