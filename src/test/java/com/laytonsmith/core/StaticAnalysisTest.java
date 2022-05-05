
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.io.IOException;
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

	Environment env;
	StaticAnalysis staticAnalysis;
	ScriptProvider scriptProvider;

	public void setup(ScriptProvider provider) throws Exception {
		staticAnalysis = new StaticAnalysis(true);
		staticAnalysis.setLocalEnable(true);
		scriptProvider = provider != null ? provider : new ScriptProvider.FileSystemScriptProvider();
		this.env = Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), null, staticAnalysis);
		this.env.getEnv(GlobalEnv.class).SetScriptProvider(scriptProvider);
	}

	public String runScript(String script) throws Exception {
		return runScript(script, null);
	}
	public String runScript(String script, ScriptProvider provider) throws Exception {
		setup(provider);
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

	public ParseTree saScript(String script) throws Exception {
		return saScript(script, null);
	}

	public ParseTree saScript(String script, ScriptProvider provider) throws Exception {
		setup(provider);
		try {
			try {
				TokenStream stream = MethodScriptCompiler.lex(script, env, new File("test.ms"), true);
				return MethodScriptCompiler.compile(stream, env, env.getEnvClasses(), staticAnalysis);
			} catch(ConfigCompileException ex) {
				throw new ConfigCompileGroupException(new HashSet<>(Arrays.asList(ex)), ex);
			}
		} catch(ConfigCompileGroupException ex) {
			StringBuilder b = new StringBuilder();
			for(ConfigCompileException e : ex.getList()) {
				b.append(e.toString()).append(" cause: ").append(StackTraceUtils.GetStacktrace(e)).append("\n");
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
		saScript("string @r = 'string'; string @k = 'string'; string @s = if(dyn(true), @r, @k)");
		saScriptExpectException("string @r = 'string'; int @i = 0; string @s = if(dyn(true), @r, @i)");
		saScript("string @s = if(dyn(true), get_value('asdf'), get_value('fdsa'));");
		saScript("int @i = 1; double @d = 2.0; number @n = if(dyn(true), @i, @d);");
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

	@Test
	public void testRecursiveTypeInferrenceWorks() throws Exception {
		// dyn is defined as `<T> T dyn(T @value)` and so is a perfect candidate for this test
		ParseTree tree = saScript("string @r = 'string'; string @s = dyn(dyn(dyn(dyn(dyn(@r)))));");
		ParseTree topDynNode = tree.getChildAt(0).getChildAt(1).getChildAt(2);
		Assert.assertEquals(CString.TYPE.asLeftHandSideType(),
				topDynNode.getDeclaredType(staticAnalysis, env, null));
		Assert.assertEquals(CString.TYPE.asLeftHandSideType(),
				topDynNode.getDeclaredType(staticAnalysis, env, CString.TYPE.asLeftHandSideType()));
		// This is auto, because constants are auto typed.
		tree = saScript("string @s = dyn(dyn(dyn(dyn(dyn('string')))));");
		topDynNode = tree.getChildAt(0).getChildAt(0).getChildAt(2);
		Assert.assertEquals(Auto.LHSTYPE,
				topDynNode.getDeclaredType(staticAnalysis, env, null));
		Assert.assertEquals(Auto.LHSTYPE,
				topDynNode.getDeclaredType(staticAnalysis, env, CString.TYPE.asLeftHandSideType()));

		// Except in strict mode it really is a string
		tree = saScript("<! strict > string @s = dyn(dyn(dyn(dyn(dyn('string')))));");
		topDynNode = tree.getChildAt(0).getChildAt(0).getChildAt(2);
		Assert.assertEquals(CString.TYPE.asLeftHandSideType(),
				topDynNode.getDeclaredType(staticAnalysis, env, null));
		Assert.assertEquals(CString.TYPE.asLeftHandSideType(),
				topDynNode.getDeclaredType(staticAnalysis, env, CString.TYPE.asLeftHandSideType()));

		saScriptExpectException("string @s = dyn(dyn(dyn(array())));");
	}

	@Test
	public void testHardcodedTypesAreAuto() throws Exception {
		saScript("boolean @b = '123' < '123';");
	}

	@Test
	public void testProcs() throws Exception {
		saScript("void proc _asdf(array @a) { return(array_keys(@a)); } _asdf(array(test: 1, testing: 2));");
	}

	@Test
	public void testIncludesAreProcessedProperly() throws Exception {
		ScriptProvider provider = (File file) -> {
			if(file.getName().endsWith("file1.ms")) {
				return "void proc _asdf(array @a) { return(array_keys(@a)); }";
			}
			throw new IOException();
		};

		saScript("include('file1.ms'); _asdf(array(test: 1, testing: 2, onetwothree: 3));", provider);
	}

	@Test
	public void testArrayReturnWithoutGenerics() throws Exception {
		saScript("array proc _test() {\n"
			+ "    array @t = null;\n"
			+ "    return(if(!is_array(@t), array(), @t));\n"
			+ "}");
	}

	@Test
	public void testLabels() throws Exception {
		saScript("array @a = array(label: 'goes', here: 'too'); msg(@a);");
	}

	@Test
	public void testLabelsWithStrictMode() throws Exception {
		saScript("<!strict> assign(@w, array('world': 'overworld', 'world_nether': 'nether', 'world_the_end': 'end'))");
	}

	@Test
	public void testVariablesInStrictMode() throws Exception {
		saScriptExpectException("<! strict > if($var > 1) { msg(''); }");
		saScript("if($var > 1) { msg(''); }");
	}

	@Test
	public void testNestedIf() throws Exception {
		saScript("<! strict > @s = if(dyn(true), 'asdf', if(dyn(true), true, 'string'))");
	}

	@Test
	public void testNestedIf2() throws Exception {
		saScript("<! strict > @s = if(dyn(true), 'asdf', if(dyn(true), true, null))");
	}

	@Test
	public void testGetProc() throws Exception {
		runScript("proc _test() {}\n"
				+ "@var = proc _test;\n"
				+ "@var();");
	}

}
