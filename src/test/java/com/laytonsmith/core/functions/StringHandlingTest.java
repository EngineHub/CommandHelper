

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import static com.laytonsmith.testing.StaticTest.assertCEquals;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.verify;

/**
 *
 *
 */
public class StringHandlingTest {

	MCPlayer fakePlayer;

    public StringHandlingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
		fakePlayer = StaticTest.GetOnlinePlayer();
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testConcat() throws Exception {
        StringHandling.concat a = new StringHandling.concat();
        assertCEquals(C.onstruct("1234"), a.exec(Target.UNKNOWN, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(4)));
        assertCEquals(C.onstruct("astring"), a.exec(Target.UNKNOWN, null, C.onstruct("a"), C.onstruct("string")));
		assertEquals("05", SRun("'0' . 5", null));
    }

    @Test(timeout = 10000)
    public void testLength() {
        StringHandling.length a = new StringHandling.length();
        assertCEquals(C.onstruct(5), a.exec(Target.UNKNOWN, null, C.onstruct("12345")));
        assertCEquals(C.onstruct(2), a.exec(Target.UNKNOWN, null, C.Array(C.onstruct(0), C.onstruct(1))));
    }

    @Test(timeout = 10000)
    public void testParseArgs() {
        StringHandling.parse_args a = new StringHandling.parse_args();
        assertCEquals(C.Array(C.onstruct("one"), C.onstruct("two")), a.exec(Target.UNKNOWN, null, C.onstruct("one   two")));
        assertCEquals(C.Array(C.onstruct("one"), C.onstruct("two")), a.exec(Target.UNKNOWN, null, C.onstruct("one two")));
    }

    @Test(timeout = 10000)
    public void testRead() {
    }

    @Test(timeout = 10000)
    public void testReplace() {
        StringHandling.replace a = new StringHandling.replace();
        assertCEquals(C.onstruct("yay"), a.exec(Target.UNKNOWN, null, C.onstruct("yayathing"), C.onstruct("athing"), C.onstruct("")));
        assertCEquals(C.onstruct("yaymonkey"), a.exec(Target.UNKNOWN, null, C.onstruct("yayathing"), C.onstruct("athing"), C.onstruct("monkey")));
        assertCEquals(C.onstruct("yayathing"), a.exec(Target.UNKNOWN, null, C.onstruct("yayathing"), C.onstruct("wut"), C.onstruct("chicken")));
    }

    @Test(timeout = 10000)
    public void testSconcat() throws Exception {
        StringHandling.sconcat a = new StringHandling.sconcat();
        assertEquals("1 2 3 4", SRun("1 2 3 4", null));
        assertEquals("a string", SRun("'a' 'string'", null));
    }

    @Test(timeout = 10000)
    public void testSubstr() {
        StringHandling.substr a = new StringHandling.substr();
        assertCEquals(C.onstruct("urge"), a.exec(Target.UNKNOWN, null, C.onstruct("hamburger"), C.onstruct(4), C.onstruct(8)));
        assertCEquals(C.onstruct("mile"), a.exec(Target.UNKNOWN, null, C.onstruct("smiles"), C.onstruct(1), C.onstruct(5)));
        assertCEquals(C.onstruct("ning"), a.exec(Target.UNKNOWN, null, C.onstruct("lightning"), C.onstruct(5)));
    }

    @Test(timeout = 10000)
    public void testToUpper() {
        StringHandling.to_upper a = new StringHandling.to_upper();
        assertCEquals(C.onstruct("TESTING 123"), a.exec(Target.UNKNOWN, null, C.onstruct("testing 123")));
        assertCEquals(C.onstruct("TESTING 123"), a.exec(Target.UNKNOWN, null, C.onstruct("TeStInG 123")));
    }

    @Test(timeout = 10000)
    public void testToLower() {
        StringHandling.to_lower a = new StringHandling.to_lower();
        assertCEquals(C.onstruct("testing 123"), a.exec(Target.UNKNOWN, null, C.onstruct("TESTING 123")));
        assertCEquals(C.onstruct("testing 123"), a.exec(Target.UNKNOWN, null, C.onstruct("TeStInG 123")));
    }

    @Test(timeout = 10000)
    public void testTrim() {
        StringHandling.trim a = new StringHandling.trim();
        assertCEquals(C.onstruct("test 123"), a.exec(Target.UNKNOWN, null, C.onstruct("    test 123    ")));
        assertCEquals(C.onstruct("test   123"), a.exec(Target.UNKNOWN, null, C.onstruct("test   123")));
    }

	@Test
	public void testSplit1() throws Exception{
		assertEquals("{a, b}", SRun("split(',', 'a,b')", null));
	}

	@Test
	public void testSplit2() throws Exception{
		assertEquals("{a, , b}", SRun("split('.', 'a..b')", null));
	}

	@Test
	public void testSplitWithLimit() throws Exception {
		assertEquals("{a, bzczd}", SRun("split('z', 'azbzczd', 1)", null));
	}

	@Test
	public void testSplitWithLimit2() throws Exception {
		assertEquals("{a, b, czd}", SRun("split('z', 'azbzczd', 2)", null));
	}

	@Test public void testMulticharacterSplit() throws Exception{
		assertEquals("{aa, aa, aa}", SRun("split('ab', 'aaabaaabaa')", null));
	}

	@Test public void testStringFormat() throws Exception{
		assertEquals("%", SRun("lsprintf('en_US', '%%')", null));
		//ultra simple tests
		assertEquals("1", SRun("lsprintf('en_US', '%d', 1)", null));
		assertEquals("12", SRun("lsprintf('en_US', '%d%d', 1, 2)", null));
		//simple test with array
		assertEquals("12", SRun("lsprintf('en_US', '%d%d', array(1, 2))", null));
		try{
			SRun("lsprintf('en_US', '%d')", null);
			fail("Expected lsprintf('en_US', '%d') to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}

		try{
			SRun("lsprintf('en_US', '%d', 1, 1)", null);
			fail("Expected lsprintf('en_US', '%d') to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}

		try{
			SRun("lsprintf('en_US', '%c', 'toobig')", null);
			fail("Expected lsprintf('en_US', '%c', 'toobig') to throw a compile exception");
		} catch(ConfigCompileException|ConfigCompileGroupException e){
			//pass
		}

		try{
			SRun("lsprintf('en_US', '%0.3f', 1.1)", null);
			fail("Expected lsprintf('en_US', '%0.3f', 1.1) to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}

		//A few advanced usages
		assertEquals("004.000", SRun("lsprintf('en_US', '%07.3f', 4)", null));
		assertEquals("004,000", SRun("lsprintf('no_NO', '%07.3f', 4)", null));

		long s = System.currentTimeMillis();
		assertEquals(String.format("%1$tm %1$te,%1$tY", s), SRun("lsprintf('en_US', '%1$tm %1$te,%1$tY', " + Long.toString(s) + ")", null));

	}
	
	@Test public void testStringFormat2() throws Exception{
		assertEquals("%", SRun("sprintf('%%')", null));
		//ultra simple tests
		assertEquals("1", SRun("sprintf('%d', 1)", null));
		assertEquals("12", SRun("sprintf('%d%d', 1, 2)", null));
		//simple test with array
		assertEquals("12", SRun("sprintf('%d%d', array(1, 2))", null));
		try{
			SRun("sprintf('%d')", null);
			fail("Expected sprintf('%d') to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}

		try{
			SRun("sprintf('%d', 1, 1)", null);
			fail("Expected sprintf('%d') to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}

		try{
			SRun("sprintf('%c', 'toobig')", null);
			fail("Expected sprintf('%c', 'toobig') to throw a compile exception");
		} catch(ConfigCompileException|ConfigCompileGroupException e){
			//pass
		}

		try{
			SRun("sprintf('%0.3f', 1.1)", null);
			fail("Expected sprintf('%0.3f', 1.1) to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}

		//A few advanced usages
		assertEquals(String.format(Locale.getDefault(), "%07.3f", 4.0), SRun("sprintf('%07.3f', 4)", null));

		long s = System.currentTimeMillis();
		assertEquals(String.format("%1$tm %1$te,%1$tY", s), SRun("sprintf('%1$tm %1$te,%1$tY', " + Long.toString(s) + ")", null));

	}

	@Test public void testCharFromUnicode() throws Exception {
		assertEquals("\u2665", SRun("char_from_unicode(parse_int(2665, 16))", null));
	}

	@Test public void testUnicodeFromChar() throws Exception {
		assertEquals("2665", SRun("to_radix(unicode_from_char('\\u2665'), 16)", null));
	}

	//This test is the gold standard for the above two tests
	@Test public void testCharFromUnicodeToChar() throws Exception {
		assertEquals("a", SRun("char_from_unicode(unicode_from_char('a'))", null));
	}

	//Double string tests
	@Test
	public void testDoubleStringWithNoControlCharacters() throws Exception {
		SRun("msg(\"hi\");", fakePlayer);
		verify(fakePlayer).sendMessage("hi");
	}

	@Test
	public void testDoubleStringWithLiteral() throws Exception {
		SRun("msg(\"\\@literal\");", fakePlayer);
		verify(fakePlayer).sendMessage("@literal");
	}

	@Test
	public void testDoubleStringWithOnlyVariable() throws Exception {
		SRun("@v = 'hi'; msg(\"@v\");", fakePlayer);
		verify(fakePlayer).sendMessage("hi");
	}

	@Test(expected = ConfigCompileException.class)
	public void testDoubleStringWithError() throws Exception {
		SRun("msg(\"@ invalid\");", null);
	}

	@Test public void testDoubleStringSimple() throws Exception {
		SRun("@v = 'var';\n"
				+ "msg(\"A @v!\");", fakePlayer);
		verify(fakePlayer).sendMessage("A var!");
	}

	@Test public void testDoubleStringSimpleUsingBraces() throws Exception {
		SRun("@var = 'var';\n"
				+ "msg(\"A @{var} here\");", fakePlayer);
		verify(fakePlayer).sendMessage("A var here");
	}

	@Test(expected = ConfigCompileException.class)
	public void testDoubleStringUnendedBrace() throws Exception {
		SRun("msg(\"@{unfinished\");", null);
	}

	@Test public void testDoubleStringSimpleUsingBracesAndImmediateFollowingCharacters() throws Exception {
		SRun("@var = 'var';\n"
				+ "msg(\"A @{var}here\");", fakePlayer);
		verify(fakePlayer).sendMessage("A varhere");
	}

	//Not yet implemented. Once implemented, comment this out, and it should be fine.
//	@Test public void testDoubleStringWithArrayWithNumericIndex() throws Exception {
//		SRun("@a = array(1, 2, 3);\n"
//				+ "msg(\"@{a[0]}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("1");
//	}
//
//	@Test public void testDoubleStringWithArrayWithStringIndex() throws Exception {
//		SRun("@a = array('one': 1, 'two': 2);\n"
//				+ "msg(\"@{a['one']}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("1");
//	}
//
//	@Test public void testDoubleStringWithArrayWithStringIndexWithInnerQuote() throws Exception {
//		SRun("@a = array('\\'q\\'': 'hi');\n"
//				+ "msg(\"@{a['\\'q\\'']}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("hi");
//	}
//
//	@Test public void testDoubleStringWithMultiDimensionalArrayAndNumericIndexes() throws Exception {
//		SRun("@a = array(\n"
//				+ "array(1, 2, 3),\n"
//				+ "array(4, 5, 6)\n"
//				+ ");\n"
//				+ "msg(\"@{a[0][1]}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("2");
//	}
//
//	@Test public void testDoubleStringWithMultiDimensionalArrayAndStringIndexes() throws Exception {
//		SRun("@a = array(\n"
//				+ "'one': array('a': 1, 'b': 2, 'c': 3),\n"
//				+ "'two': array('x': 4, 'y': 5, 'z': 6)\n"
//				+ ");\n"
//				+ "msg(\"@{a['one']['c']}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("3");
//	}
//
//	@Test public void testDoubleStringWithMultiDimensionalArrayAndStringAndNumericIndexes() throws Exception {
//		SRun("@a = array(\n"
//				+ "'one': array(1, 2, 3),"
//				+ "'two': array(4, 5, 6)"
//				+ ");\n"
//				+ "msg(\"@{a['one'][2]}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("3");
//	}
//
//	@Test public void testDoubleStringWithMultiDimensionalArrayAndStringIndexWithInnerQuote() throws Exception {
//		SRun("@a = array(\n"
//				+ "'\\'q\\'': array(1, 2, '\\'m\\'': 3),"
//				+ "'\\'r\\'': array(4, 5, 6)"
//				+ ");\n"
//				+ "msg(\"@{a['\\'q\\'']['\\'m\\'']}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("3");
//	}
//
//	@Test public void testDoubleStringWithMultiDimensionalArrayAndStringIndexWithInnerQuoteAndNumericIndex() throws Exception {
//		SRun("@a = array(\n"
//				+ "'\\'q\\'': array(1, 2, 3),"
//				+ "'\\'r\\'': array(4, 5, 6)"
//				+ ");\n"
//				+ "msg(\"@{a['\\'q\\''][1]}\");", fakePlayer);
//		verify(fakePlayer).sendMessage("2");
//	}

}
