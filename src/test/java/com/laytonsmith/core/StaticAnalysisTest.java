
package com.laytonsmith.core;

import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.compiler.analysis.StaticAnalysis;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.EnumSet;
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

	@Test
	public void testNoneWorksLikeAuto() throws Exception {
		String script = "rand() ||| die()";
		StaticAnalysis staticAnalysis = new StaticAnalysis(true);
		staticAnalysis.setLocalEnable(true);
		Environment env = Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), null, staticAnalysis);
		TokenStream stream = MethodScriptCompiler.lex(script, env, new File("test.ms"), true);
		MethodScriptCompiler.compile(stream, env, env.getEnvClasses(), staticAnalysis);
	}

	@Test(expected=ConfigCompileException.class)
	public void testNoneDoesntWorkLikeAuto() throws Exception {
		String script = "rand() || die()";
		StaticAnalysis staticAnalysis = new StaticAnalysis(true);
		staticAnalysis.setLocalEnable(true);
		Environment env = Static.GenerateStandaloneEnvironment(false, EnumSet.of(RuntimeMode.CMDLINE), null, staticAnalysis);
		TokenStream stream = MethodScriptCompiler.lex(script, env, new File("test.ms"), true);
		MethodScriptCompiler.compile(stream, env, env.getEnvClasses(), staticAnalysis);
	}
}
