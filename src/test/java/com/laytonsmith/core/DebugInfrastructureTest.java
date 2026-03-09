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
import com.laytonsmith.core.exceptions.StackTraceFrame;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.StaticTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
		final List<Script.DebugSnapshot> pauses = new ArrayList<>();
		boolean completed = false;
		int resumeCount = 0;

		@Override
		public void onPaused(Script.DebugSnapshot snapshot) {
			pauses.add(snapshot);
		}

		@Override
		public void onResumed() {
			resumeCount++;
		}

		@Override
		public void onCompleted() {
			completed = true;
		}
	}

	@Test
	public void testBreakpointHitAndContinue() throws Exception {
		String script = "@x = 1\n@y = @x + 1\n@z = @y + 1";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		Mixed result = executeWithDebugger(script, debugCtx);

		// Should have paused
		assertTrue(Script.isDebuggerPaused(result));
		assertEquals(1, listener.pauses.size());
		assertEquals(2, listener.pauses.get(0).getPauseTarget().line());

		// Resume with continue (NONE = run freely)
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.pauses.get(0));

		assertTrue(listener.completed);
		assertEquals(1, listener.pauses.size());
	}

	@Test
	public void testVariableInspection() throws Exception {
		String script = "@x = 42\n@y = 'hello'\n@z = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);

		assertEquals(1, listener.pauses.size());
		Map<String, Mixed> vars = listener.pauses.get(0).getVariables();
		// @x and @y should be set; @z has not executed yet
		assertEquals("42", vars.get("@x").val());
		assertEquals("hello", vars.get("@y").val());

		// Clean up: continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.pauses.get(0));
		assertTrue(listener.completed);
	}

	@Test
	public void testMultipleBreakpoints() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 1));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);

		// First breakpoint
		assertEquals(1, listener.pauses.size());
		assertEquals(1, listener.pauses.get(0).getPauseTarget().line());

		// Continue to next breakpoint
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.pauses.get(0));

		assertEquals(2, listener.pauses.size());
		assertEquals(3, listener.pauses.get(1).getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.pauses.get(1));
		assertTrue(listener.completed);
	}

	@Test
	public void testDisconnect() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 1));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);

		// Hit first breakpoint
		assertEquals(1, listener.pauses.size());

		// Disconnect and resume - should run to completion without more pauses
		debugCtx.disconnect();
		Script.resumeEval(listener.pauses.get(0));

		assertTrue(listener.completed);
		assertEquals(1, listener.pauses.size());
	}

	@Test
	public void testStepIntoProc() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()\n@done = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 5));

		executeWithDebugger(script, debugCtx);

		// Should pause at line 5 (_foo() call)
		assertEquals(1, listener.pauses.size());
		Script.DebugSnapshot pause1 = listener.pauses.get(0);
		assertEquals(5, pause1.getPauseTarget().line());

		// Step into
		debugCtx.setStepMode(DebugContext.StepMode.INTO,
				pause1.getUserCallDepth(), pause1.getPauseTarget());
		Script.resumeEval(pause1);

		// Should pause inside the proc body (line 2)
		assertEquals(2, listener.pauses.size());
		Script.DebugSnapshot pause2 = listener.pauses.get(1);
		assertEquals(2, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(pause2);
		assertTrue(listener.completed);
	}

	@Test
	public void testStepOverProc() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()\n@done = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 5));

		executeWithDebugger(script, debugCtx);

		Script.DebugSnapshot pause1 = listener.pauses.get(0);
		assertEquals(5, pause1.getPauseTarget().line());

		// Step over
		debugCtx.setStepMode(DebugContext.StepMode.OVER,
				pause1.getUserCallDepth(), pause1.getPauseTarget());
		Script.resumeEval(pause1);

		// Should pause at line 6, having skipped into the proc
		assertEquals(2, listener.pauses.size());
		Script.DebugSnapshot pause2 = listener.pauses.get(1);
		assertEquals(6, pause2.getPauseTarget().line());

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(pause2);
		assertTrue(listener.completed);
	}

	@Test
	public void testStepOut() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()\n@done = true";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		executeWithDebugger(script, debugCtx);

		// Should pause inside proc at line 2
		assertEquals(1, listener.pauses.size());
		Script.DebugSnapshot pause1 = listener.pauses.get(0);
		assertEquals(2, pause1.getPauseTarget().line());
		int depthInside = pause1.getUserCallDepth();
		assertTrue("Should be inside proc (depth > 0)", depthInside > 0);

		// Step out
		debugCtx.setStepMode(DebugContext.StepMode.OUT,
				depthInside, pause1.getPauseTarget());
		Script.resumeEval(pause1);

		// Should pause after returning from the proc
		assertEquals(2, listener.pauses.size());
		Script.DebugSnapshot pause2 = listener.pauses.get(1);
		assertTrue("Should be at lower depth after step out",
				pause2.getUserCallDepth() < depthInside);

		// Continue to completion
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(pause2);
		assertTrue(listener.completed);
	}

	@Test
	public void testCallStackInspection() throws Exception {
		String script = "proc _foo() {\n\t@a = 10\n\treturn(@a)\n}\n@result = _foo()";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 2));

		executeWithDebugger(script, debugCtx);

		assertEquals(1, listener.pauses.size());
		Script.DebugSnapshot pause = listener.pauses.get(0);
		List<StackTraceFrame> callStack = pause.getCallStack();
		assertFalse("Call stack should not be empty inside proc", callStack.isEmpty());
		// Procedure names in the stack trace include the "proc " prefix
		assertTrue("Expected proc name to contain _foo",
				callStack.get(0).getProcedureName().contains("_foo"));

		// Clean up
		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(pause);
	}

	@Test
	public void testResumeCount() throws Exception {
		String script = "@x = 1\n@y = 2\n@z = 3";
		TestDebugListener listener = new TestDebugListener();
		DebugContext debugCtx = new DebugContext(listener);
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 1));
		debugCtx.addBreakpoint(new Breakpoint(TEST_FILE, 3));

		executeWithDebugger(script, debugCtx);
		assertEquals(0, listener.resumeCount);

		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.pauses.get(0));
		assertEquals(1, listener.resumeCount);

		debugCtx.setStepMode(DebugContext.StepMode.NONE, 0, Target.UNKNOWN);
		Script.resumeEval(listener.pauses.get(1));
		assertEquals(2, listener.resumeCount);
		assertTrue(listener.completed);
	}
}
