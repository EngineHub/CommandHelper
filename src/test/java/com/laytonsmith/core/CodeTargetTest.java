package com.laytonsmith.core;

import com.laytonsmith.core.compiler.TokenStream;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.functions.StringHandling;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.List;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * This class tests various scripts that have historically been shown to have incorrect code targets.
 */
public class CodeTargetTest {

	private static final File TEST_FILE = new File("test.ms");

	private List<ParseTree> compile(String script) throws Exception {
		StaticTest.InstallFakeServerFrontend();
		Environment env = Static.GenerateStandaloneEnvironment();
		File file = TEST_FILE;
		TokenStream stream = MethodScriptCompiler.lex(script, env, file, true);
		List<ParseTree> tree = MethodScriptCompiler.compile(stream, env, env.getEnvClasses()).getChildren();
		if(tree.size() == 1 && tree.get(0).getData() instanceof CFunction cf) {
			if(cf.getCachedFunction().getName().equals(StringHandling.sconcat.NAME)
					|| cf.getCachedFunction().getName().equals(com.laytonsmith.core.functions.Compiler.__statements__.NAME)) {
				return tree.get(0).getChildren();
			}
		}
		return tree;
	}

	private void validateTarget(int expectedLine, int expectedColumn, int expectedLength, ParseTree node) {
		Target actual = node.getTarget();
		if(node.isSyntheticNode()) {
			fail("Testing synthetic node");
		}
		if(!actual.file().equals(TEST_FILE)) {
			fail("Incorrect file");
		}
		if(actual.line() != expectedLine || actual.col() != expectedColumn) {
			fail("Incorrect code target, expected " + expectedLine + "." + expectedColumn + " but got "
				+ actual.line() + "." + actual.col());
		}
		if(expectedLength != actual.length()) {
			fail("Incorrect length, expected " + expectedLength + " but found " + actual.length());
		}
	}

	@Test
	public void test1() throws Exception {
		List<ParseTree> nodes = compile(
				"msg();\n"
				+ "msg();\n"
				+ " msg();\n"
				+ " msg('a', 'bb');\n"
		);
		validateTarget(1, 1, 3, nodes.get(0).getChildAt(0));
		validateTarget(2, 1, 3, nodes.get(1).getChildAt(0));
		validateTarget(3, 2, 3, nodes.get(2).getChildAt(0));
		validateTarget(4, 7, 1, nodes.get(3).getChildAt(0).getChildAt(0));
		validateTarget(4, 12, 2, nodes.get(3).getChildAt(0).getChildAt(1));
	}
}
