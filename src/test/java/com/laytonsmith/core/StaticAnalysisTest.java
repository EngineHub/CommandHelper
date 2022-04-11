
package com.laytonsmith.core;

import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class StaticAnalysisTest {
	@BeforeClass
	public static void beforeClass() {
		StaticTest.InstallFakeServerFrontend();
	}

	public String runScript(String script) throws Exception {
		StaticAnalysis staticAnalysis = new StaticAnalysis(true);
		staticAnalysis.setLocalEnable(true);
		Environment env
				= Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), null, staticAnalysis);
		return StaticTest.SRun(script, null, env);
	}

	public void saScriptExpectException(String script) throws Exception {
		try {
			saScript(script);
			Assert.fail("Expected a compile exception, but got none.");
		} catch(ConfigCompileGroupException | ConfigCompileException ex) {
			// pass
		}
	}

	public void saScript(String script) throws Exception {
		StaticAnalysis staticAnalysis = new StaticAnalysis(true);
		staticAnalysis.setLocalEnable(true);
		Environment env
				= Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), null, staticAnalysis);
		try {
			try {
				TokenStream stream = MethodScriptCompiler.lex(script, env, new File("test.ms"), true);
				MethodScriptCompiler.compile(stream, env, env.getEnvClasses(), staticAnalysis);
			} catch(ConfigCompileException ex) {
				throw new ConfigCompileGroupException(new HashSet<>(Arrays.asList(ex)), ex);
			}
		} catch(ConfigCompileGroupException ex) {
			StringBuilder b = new StringBuilder();
			for(ConfigCompileException e : ex.getList()) {
				b.append(e.toString()).append("\n");
			}
			throw new ConfigCompileException(b.toString(), Target.UNKNOWN, ex);
		}
	}

	@Test
	public void testNoneWorksLikeAuto() throws Exception {
		String script = "rand() ||| die()";
		saScript(script);
	}

	@Test(expected = ConfigCompileException.class)
	public void testNoneDoesntWorkLikeAuto() throws Exception {
		String script = "rand() || die()";
		saScript(script);
	}

	@Test
	public void testBreakWorksInStrict() throws Exception {
		String script = "<! strict >\n foreach(@i in 1..5) { break(); }";
		saScript(script);
	}

	@Test
	public void testBreakWorksInNonStrict() throws Exception {
		String script = "<! strict: false >\n foreach(@i in 1..5) { break(); }";
		saScript(script);
	}

	@Test
	public void testAssignNullToTypeWorks() throws Exception {
		saScript("<! strict > string @s = null; msg(@s);");
	}

	@Test
	public void testIfWithWeirdParameters() throws Exception {
		saScript("void proc _test(string @s, array @a) {"
				+ "	if(function_exists('dyn') && @s != null) {"
				+ "		if(dyn(true), noop(), cancel());"
				+ "		return();"
				+ "	}"
				+ "}");
	}

	@Test
	public void testInstanceof() throws Exception {
		saScript("@a = closure(){}; msg(@a instanceof closure);");
	}

	@Test
	public void testUseGetValue() throws Exception {
		// get_value returns a generic type, which should be converted so the typechecking doesn't fail
		saScript("get_value('asdf') == false");
	}

	@Test
	public void testTernaryIfReturnCorrectType() throws Exception {
		saScript("string @s = if(dyn(true), 'string', 'string')");
		saScriptExpectException("string @s = if(dyn(true), 'string', 0)");
		saScript("string @s = if(dyn(true), get_value('asdf'), get_value('fdsa'));");
		saScript("number @n = if(dyn(true), 1, 2.0);");
	}

	@Test
	public void testCast() throws Exception {
		saScriptExpectException("int @i = 1; string @s = @i;");
		saScript("int @i = 1; string @s = cast(@i);"); // Inferred cast to string
		try {
			runScript("int @i = 1; string @s = cast(@i);");
			Assert.fail();
		} catch(CRECastException ex) {
			// pass
		}
		try {
			// Same script, but try with static analysis off, which is a different code path
			StaticTest.SRun("int @i = 1; string @s = cast(@i);", null);
			Assert.fail();
		} catch(CRECastException ex) {
			// pass
		}
		saScriptExpectException("primitive @p = 1; int @i = @p;");
		saScript("primitive @p = 1; int @i = cast(@p);");
		runScript("primitive @p = 1; int @i = cast(@p);");

	}

}
