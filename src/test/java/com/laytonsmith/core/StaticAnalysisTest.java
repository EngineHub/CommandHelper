
package com.laytonsmith.core;

import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class StaticAnalysisTest {
	@BeforeClass
	public static void beforeClass() {
		StaticTest.InstallFakeServerFrontend();
	}

	public void runScript(String script) throws Exception {
		runScript(script, null);
	}

	public void runScript(String script, ScriptProvider scriptProvider) throws Exception {
		StaticAnalysis staticAnalysis = new StaticAnalysis(true);
		staticAnalysis.setLocalEnable(true);
		Environment env = Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), null, staticAnalysis);
		if(scriptProvider != null) {
			env.getEnv(GlobalEnv.class).SetScriptProvider(scriptProvider);
		}
		try {
			try {
				TokenStream stream = MethodScriptCompiler.lex(script, env, new File("test.ms"), true);
				MethodScriptCompiler.compile(stream, env, env.getEnvClasses(), staticAnalysis);
			} catch(ConfigCompileException ex) {
				throw new ConfigCompileGroupException(new HashSet<>(Arrays.asList(ex)));
			}
		} catch(ConfigCompileGroupException ex) {
			StringBuilder b = new StringBuilder();
			for(ConfigCompileException e : ex.getList()) {
				b.append(e.toString()).append("\n");
			}
			throw new ConfigCompileException(b.toString(), Target.UNKNOWN);
		}
	}

	@Test
	public void testNoneWorksLikeAuto() throws Exception {
		String script = "rand() ||| die()";
		runScript(script);
	}

	@Test(expected = ConfigCompileException.class)
	public void testNoneDoesntWorkLikeAuto() throws Exception {
		String script = "rand() || die()";
		runScript(script);
	}

	@Test
	public void testBreakWorksInStrict() throws Exception {
		String script = "<! strict >\n foreach(@i in 1..5) { break(); }";
		runScript(script);
	}

	@Test
	public void testBreakWorksInNonStrict() throws Exception {
		String script = "<! strict: false >\n foreach(@i in 1..5) { break(); }";
		runScript(script);
	}

	@Test
	public void testInstanceof() throws Exception {
		runScript("@a = closure(){}; msg(@a instanceof closure);");
	}

	@Test
	public void testGetProc() throws Exception {
		runScript("proc _test() {}\n"
				+ "@var = proc _test;\n"
				+ "@var();");
	}

	@Test(expected = ConfigCompileException.class)
	@Ignore("Ignored until parameter typechecking is implemented")
	public void testWrongArgsToProc() throws Exception {
		runScript("<! strict > void proc _test(int @a) {} _test(array());");
	}

	@Test
	@Ignore("Ignored until parameter typechecking is implemented")
	public void testVarargsProc() throws Exception {
		runScript("<! strict > void proc _test(string... @args) { msg(@args); }\n"
				+ "_test('a', 'b', 'c');");
		try {
			runScript("<! strict > void proc _test(string... @args) { msg(@args); }\n"
					+ "_test(1, 2, 3);");
			fail();
		} catch(ConfigCompileException ex) {
			// Pass
		}
		try {
			runScript("<! strict > void proc _test(string... @args) { msg(@args); }\n"
					+ "_test('a', 2, 3);");
			fail();
		} catch(ConfigCompileException ex) {
			// Pass
		}
	}

	@Test
	public void testForwardDeclares() throws Exception {
		runScript("int proc _test(); include(dyn('include.ms')); int @good = _test();",
				file -> "int proc _test() { return 5; }");

		try {
			runScript("int proc _test(); include(dyn('include.ms')); string @bad = _test();",
					file -> "int proc _test() { return 5; }");
			fail();
		} catch(ConfigCompileException ex) {
			// pass
		}

		try {
			runScript("include(dyn('include.ms')); string @bad = _test();",
					file -> "int proc _test() { return 5; }");
			fail();
		} catch(ConfigCompileException ex) {
			// pass
		}
	}

}
