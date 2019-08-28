package com.laytonsmith.core.functions;

import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.util.Set;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class RegexTest {

	static Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs = com.laytonsmith.core.environments.Environment.getDefaultEnvClasses();

	public RegexTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testRegMatch() throws Exception {
		assertEquals("{0: word}", SRun("reg_match('word', 'This is a word')", null));
		assertEquals("{}", SRun("reg_match('word', 'This is an airplane')", null));
		assertEquals("{0: word, 1: word}", SRun("reg_match('(word)', 'This is a word')", null));
		assertEquals("{0: This is a word, 1: word}", SRun("reg_match('This is a (word)', 'This is a word')", null));
		assertEquals("{0: WORD}", SRun("reg_match(array(word, i), 'THIS IS A WORD')", null));
		try {
			SRun("reg_match(array(word, l), hi)", null);
			fail();
		} catch (ConfigRuntimeException e) {
			//Pass
		}
	}

	@Test(timeout = 10000)
	public void testRegMatchAll() throws Exception {
		assertEquals("{{0: This is a word, 1: word}, {0: This is a word, 1: word}}", SRun("reg_match_all('This is a (word)', 'word, This is a word, This is a word')", null));
		assertEquals("{}", SRun("reg_match_all('word', 'yay')", null));
	}

	@Test(timeout = 10000)
	public void testRegReplace() throws Exception {
		assertEquals("word", SRun("reg_replace('This is a (word)', '$1', 'This is a word')", null));
		assertEquals("It's a wordy day!", SRun("reg_replace('sunn', 'word', 'It\\'s a sunny day!')", null));
	}

	@Test(timeout = 10000)
	public void testRegSplit() throws Exception {
		assertEquals("{one, two, three}", SRun("reg_split('\\\\|', 'one|two|three')", null));
	}

	@Test
	public void testRegSplitLimit0() throws Exception {
		assertEquals("{a1b2c3d}", SRun("reg_split('\\\\d', 'a1b2c3d', 0)", null));
	}

	@Test
	public void testRegSplitLimit1() throws Exception {
		assertEquals("{a, b2c3d}", SRun("reg_split('\\\\d', 'a1b2c3d', 1)", null));
	}

	@Test
	public void testRegSplitLimit2() throws Exception {
		assertEquals("{a, b, c3d}", SRun("reg_split('\\\\d', 'a1b2c3d', 2)", null));
	}

	@Test(timeout = 10000)
	public void testRegCount() throws Exception {
		assertEquals("3", SRun("reg_count('/', '///yay')", null));
		assertEquals("0", SRun("reg_count('poppycock', 'tiddly winks')", null));
	}

	//Here, it's a compile error, since we're using it statically
	@Test(expected = ConfigCompileException.class)
	public void testRegFailureStatic() throws Exception {
		MethodScriptCompiler.compile(MethodScriptCompiler.lex("reg_match('(?i)asd(', 'irrelevant')", null, null, true), null,
				envs);
	}

	//Here, it's a runtime error, since we're using it dynamically
	@Test(expected = ConfigRuntimeException.class)
	public void testRegFailureDynamic() throws Exception {
		SRun("assign(@a, '(?i)asd(') reg_match(@a, 'irrelevant')", null);
	}

	@Test
	public void testNamedCaptures1() throws Exception {
		if(!hasNamedCaptureCapability()) {
			return;
		}
		assertEquals("123", SRun("reg_match('abc(?<foo>\\\\d+)(xyz)', 'abc123xyz')['foo']", null));
	}

	@Test
	public void testNamedCaptures2() throws Exception {
		if(!hasNamedCaptureCapability()) {
			return;
		}
		assertEquals("123", SRun("reg_match_all('abc(?<foo>\\\\d+)(xyz)', 'abc123xyzabc456xyz')[0]['foo']", null));
		assertEquals("456", SRun("reg_match_all('abc(?<foo>\\\\d+)(xyz)', 'abc123xyzabc456xyz')[1]['foo']", null));
	}

	@Test
	public void testNamedCaptures3() throws Exception {
		if(!hasNamedCaptureCapability()) {
			return;
		}
		assertEquals("123", SRun("reg_match('abc(?<foo>\\\\d+)def\\\\k<foo>', 'abc123def123')['foo']", null));
	}

	@Test
	public void testNamedCaptures4() throws Exception {
		if(!hasNamedCaptureCapability()) {
			return;
		}
		assertEquals("123", SRun("reg_replace('abc(?<foo>\\\\d+)', '${foo}', 'abc123')", null));
	}

	@Test(timeout = 60000)
	public void testInfiniteLoopInRegexCaptures() throws Exception {
		//This code has caused infinite loops in reg_match_all
		SRun("reg_match_all('(?<=@)[^@]*', '@@@@')", null);
	}

	public static boolean hasNamedCaptureCapability() {
		try {
			Matcher.class.getMethod("group", String.class);
			return true;
		} catch (NoSuchMethodException ex) {
			return false;
		} catch (SecurityException ex) {
			throw new Error(ex);
		}
	}
}
