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

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;
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
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.snapshot(0));

		assertEquals(2, listener.pauses.size());
		assertEquals(3, listener.pauses.get(1).getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		debugCtx.setStepMode(DebugContext.StepMode.INTO,
				pause1.getUserCallDepth(), pause1.getPauseTarget());
		Script.resumeEval(listener.snapshot(0));

		// Should pause inside the proc body (line 2)
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertEquals(2, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		debugCtx.setStepMode(DebugContext.StepMode.OVER,
				pause1.getUserCallDepth(), pause1.getPauseTarget());
		Script.resumeEval(listener.snapshot(0));

		// Should pause at line 6, having skipped into the proc
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertEquals(6, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		debugCtx.setStepMode(DebugContext.StepMode.OUT,
				depthInside, pause1.getPauseTarget());
		Script.resumeEval(listener.snapshot(0));

		// Should pause after returning from the proc
		assertEquals(2, listener.pauses.size());
		PausedState pause2 = listener.pauses.get(1);
		assertTrue("Should be at lower depth after step out",
				pause2.getUserCallDepth() < depthInside);

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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

		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.snapshot(0));
		assertEquals(1, listener.resumeCount);

		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
				.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
				.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
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
		private boolean closed = false;

		DAPTestHarness(String script, int[] breakpointLines,
				DebugContext.ThreadingMode mode) throws Exception {
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
			};

			server = new MSDebugServer();
			Environment env = Static.GenerateStandaloneEnvironment();
			env = env.cloneAndAdd(new CommandHelperEnvironment());
			env = server.startListening(0, env, false, mode);
			int port = server.getPort();
			assertTrue("Server should be listening", port > 0);

			clientSocket = new Socket("localhost", port);
			Launcher<IDebugProtocolServer> clientLauncher = DSPLauncher.createClientLauncher(
					mockClient, clientSocket.getInputStream(), clientSocket.getOutputStream());
			proxy = clientLauncher.getRemoteProxy();
			clientLauncher.startListening();

			proxy.initialize(new InitializeRequestArguments()).get(5, TimeUnit.SECONDS);

			// Set breakpoints
			SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
			Source source = new Source();
			source.setPath(TEST_FILE.getPath());
			bpArgs.setSource(source);
			SourceBreakpoint[] sbps = new SourceBreakpoint[breakpointLines.length];
			for(int i = 0; i < breakpointLines.length; i++) {
				sbps[i] = new SourceBreakpoint();
				sbps[i].setLine(breakpointLines[i]);
			}
			bpArgs.setBreakpoints(sbps);
			proxy.setBreakpoints(bpArgs).get(5, TimeUnit.SECONDS);

			// Compile and run
			StaticAnalysis analysis = new StaticAnalysis(true);
			analysis.setLocalEnable(false);
			TokenStream tokens = MethodScriptCompiler.lex(script, env, TEST_FILE, true);
			ParseTree tree = MethodScriptCompiler.compile(tokens, env, envs, analysis);
			server.runScript(tree, env, null);
		}

		boolean awaitStop(int index, long seconds) throws InterruptedException {
			return stopLatches.get(index).await(seconds, TimeUnit.SECONDS);
		}

		void stepOver() throws Exception {
			NextArguments args = new NextArguments();
			args.setThreadId(1);
			proxy.next(args).get(5, TimeUnit.SECONDS);
		}

		void continue_() throws Exception {
			ContinueArguments args = new ContinueArguments();
			args.setThreadId(1);
			proxy.continue_(args).get(5, TimeUnit.SECONDS);
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
}
