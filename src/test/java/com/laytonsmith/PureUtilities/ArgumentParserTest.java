package com.laytonsmith.PureUtilities;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class ArgumentParserTest {

	public ArgumentParserTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void test2() throws ArgumentParser.ValidationException {
		ArgumentParser p = ArgumentParser.GetParser()
				.addFlag('l', "long", "A flag")
				.addFlag('n', "nope", "Another flag");
		ArgumentParser.ArgumentParserResults r = p.match("-l");
		assertTrue(r.isFlagSet('l'));
		assertFalse(r.isFlagSet('n'));
	}

	@Test
	public void test3() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser().addArgument('c', ArgumentParser.Type.STRING, "", "", true);
		ArgumentParser.ArgumentParserResults r = p.match("-c blah");
		assertEquals("blah", r.getStringArgument('c'));
	}

	@Test
	public void test4() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser().addArgument('c', ArgumentParser.Type.ARRAY_OF_STRINGS, "", "", true);
		ArgumentParser.ArgumentParserResults r = p.match("-c blah blarg blip");
		assertArrayEquals(new String[]{"blah", "blarg", "blip"}, r.getStringListArgument('c').toArray());
	}

	@Test
	public void test5() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser().addArgument('c', ArgumentParser.Type.STRING, "", "", true);
		ArgumentParser.ArgumentParserResults r;
		r = p.match("-c \"blah blarg blip\"");
		assertEquals("blah blarg blip", r.getStringArgument('c'));

		r = p.match("-c 'blah blarg blip'");
		assertEquals("blah blarg blip", r.getStringArgument('c'));

		r = p.match("-c 'blah \\'blarg\\' blip'");
		assertEquals("blah 'blarg' blip", r.getStringArgument('c'));

		r = p.match("-c \"blah \\\"blarg\\\" blip\"");
		assertEquals("blah \"blarg\" blip", r.getStringArgument('c'));

		r = p.match("-c \"blah blarg blip\\\\\"");
		assertEquals("blah blarg blip\\", r.getStringArgument('c'));

		r = p.match("-c 'blah blarg blip\\\\'");
		assertEquals("blah blarg blip\\", r.getStringArgument('c'));

		r = p.match("-c blah\\ blarg\\ blip");
		assertEquals("blah blarg blip", r.getStringArgument('c'));

		r = p.match("-c \\-blah");
		assertEquals("-blah", r.getStringArgument('c'));

		r = p.match("-c \\\\-blah");
		assertEquals("\\-blah", r.getStringArgument('c'));
	}

	@Test
	public void test6() throws Exception {
		//Test long args
		ArgumentParser p = ArgumentParser.GetParser().addFlag("long", "");
		ArgumentParser.ArgumentParserResults r = p.match("--long");
		assertTrue(r.isFlagSet("long"));
	}

	@Test
	public void test7() throws Exception {
		//Test no-switch args
		ArgumentParser p = ArgumentParser.GetParser();
		ArgumentParser.ArgumentParserResults r = p.match("this is a test");
		assertEquals("this is a test", r.getStringArgument());
		assertArrayEquals(new String[]{"this", "is", "a", "test"}, r.getStringListArgument().toArray());
	}

	@Test
	public void test8() throws Exception {
		//Test no-switch args in addition to switched args
		ArgumentParser p = ArgumentParser.GetParser().addArgument('c', ArgumentParser.Type.STRING, "", "", true);
		ArgumentParser.ArgumentParserResults r = p.match("-c blah blarg blip");
		assertEquals("blah", r.getStringArgument('c'));
		assertEquals("blarg blip", r.getStringArgument());
	}

	@Test
	public void test9() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser()
				.addArgument('i', ArgumentParser.Type.STRING, "Input", "", true)
				.addArgument('a', "array", ArgumentParser.Type.ARRAY_OF_STRINGS, "Array", "", true)
				.addArgument("numbers", ArgumentParser.Type.ARRAY_OF_NUMBERS, "An array of numbers", "", true)
				.addArgument('n', ArgumentParser.Type.NUMBER, "This is a single number", "", true)
				.addFlag('x', "Flag x")
				.addFlag('y', "Flag y")
				.addFlag('z', "Flag z")
				.addFlag("", "Seperator")
				.addArgument("This is the default argument", "", true);
		ArgumentParser.ArgumentParserResults r = p.match("-i 'This is a \"string\"' --array blip blop --numbers 1 2 3 -nxy 4 -- loose arguments");

		assertEquals("This is a \"string\"", r.getStringArgument('i'));
		assertArrayEquals(new String[]{"blip", "blop"}, r.getStringListArgument('a').toArray());
		assertArrayEquals(new Double[]{1.0, 2.0, 3.0}, r.getNumberListArgument("numbers").toArray());
		assertEquals(4, r.getNumberArgument('n'), 0.1);
		assertTrue(r.isFlagSet('x'));
		assertTrue(r.isFlagSet('y'));
		assertFalse(r.isFlagSet('z'));
		assertEquals("loose arguments", r.getStringArgument());
		assertArrayEquals(new String[]{"loose", "arguments"}, r.getStringListArgument().toArray());
	}

	@Test(expected = ArgumentParser.ValidationException.class)
	public void test10() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser()
				.addArgument('a', ArgumentParser.Type.STRING, "", "", true)
				.addArgument('b', ArgumentParser.Type.STRING, "", "", true);
		p.match("-ab");
	}

	@Test(expected = ArgumentParser.ValidationException.class)
	public void test11() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser().addArgument('n', ArgumentParser.Type.NUMBER, "", "", true);
		p.match("-n lol");
	}

	@Test
	public void test12() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser()
				.addArgument('i', ArgumentParser.Type.STRING, "Input", "input", true)
				.addArgument('a', "array", ArgumentParser.Type.ARRAY_OF_STRINGS, "Array", "array", false)
				.addArgument("numbers", ArgumentParser.Type.ARRAY_OF_NUMBERS, "A list", "numbers", true)
				.addArgument('n', ArgumentParser.Type.NUMBER, "This is a single \nnumber", "number", true)
				.addFlag('x', "Flag x")
				.addFlag('y', "Flag y")
				.addFlag('z', "Flag z")
				.addFlag("flag", "Big flag")
				.addArgument("This is the default argument", "arguments", true)
				.addDescription("This is the \ndescription");
		String expected = "\tThis is the \ndescription\n\n"
				+ "Usage:\n\t[-xyz] [--flag] [-a <array, ...>] -i <input> -n <#number> --numbers <#numbers, ...> <arguments, ...>\n\n"
				+ "Options:\n\n"
				+ "\t<arguments>: This is the default argument\n"
				+ "Flags (Short flags may be combined):\n"
				+ "\t-x: Flag x\n"
				+ "\t-y: Flag y\n"
				+ "\t-z: Flag z\n"
				+ "\t--flag: Big flag\n"
				+ "\n"
				+ "\t-a: Optional. A list. Array\n"
				+ "\t-i: Required. Input\n"
				+ "\t-n: Required. A numeric value. This is a single \n\t\tnumber\n"
				+ "\t--array: Alias to -a\n"
				+ "\t--numbers: Required. A list of numbers. A list\n";
		String actual = p.getBuiltDescription();
//		System.out.println("Expected:\n\n" + expected);
//		System.out.println("Actual:\n\n" + actual);
		assertEquals(expected, actual);
	}

	@Test
	public void test13() throws Exception {
		ArgumentParser p = ArgumentParser.GetParser()
				.addArgument('a', ArgumentParser.Type.STRING, "", "", true)
				.addArgument('b', ArgumentParser.Type.STRING, "", "", true);
		ArgumentParser.ArgumentParserResults r = p.match(new String[]{"-b", "\"This is a quoted\" 'string'", "-a", "argument with spaces"});
		assertEquals("argument with spaces", r.getStringArgument('a'));
		assertEquals("\"This is a quoted\" 'string'", r.getStringArgument('b'));
	}

}
