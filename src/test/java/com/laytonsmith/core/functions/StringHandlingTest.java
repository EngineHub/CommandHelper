

package com.laytonsmith.core.functions;

import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import static com.laytonsmith.testing.StaticTest.assertCEquals;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class StringHandlingTest {

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
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testConcat() {
        StringHandling.concat a = new StringHandling.concat();
        assertCEquals(C.onstruct("1234"), a.exec(Target.UNKNOWN, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(4)));
        assertCEquals(C.onstruct("astring"), a.exec(Target.UNKNOWN, null, C.onstruct("a"), C.onstruct("string")));
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
    public void testSconcat() throws ConfigCompileException {
        StringHandling.sconcat a = new StringHandling.sconcat();
        assertEquals("1 2 3 4", SRun("1 2 3 4", null));
        assertEquals("a string", SRun("a string", null));
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
    public void testCC() throws ConfigCompileException{
        assertEquals("Thisshouldbeamess", SRun("cc(This should be a mess)", null));
    }
	
	@Test
	public void testSplit1() throws ConfigCompileException{
		assertEquals("{a, b}", SRun("split(',', 'a,b')", null));
	}
	
	@Test
	public void testSplit2() throws ConfigCompileException{
		assertEquals("{a, , b}", SRun("split('.', 'a..b')", null));
	}
	
	@Test public void testMulticharacterSplit() throws Exception{
		assertEquals("{aa, aa, aa}", SRun("split('ab', 'aaabaaabaa')", null));
	}
	
	@Test public void testStringFormat() throws Exception{
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
		} catch(ConfigCompileException e){
			//pass
		}
		
		try{
			SRun("sprintf('%0.3f', 1.1)", null);
			fail("Expected sprintf('%0.3f', 1.1) to throw a compile exception");
		} catch(ConfigCompileException e){
			//pass
		}
		
		//A few advanced usages
		assertEquals("004.000", SRun("sprintf('%07.3f', 4)", null));
		
		long s = System.currentTimeMillis();
		assertEquals(String.format("%1$tm %1$te,%1$tY", s), SRun("sprintf('%1$tm %1$te,%1$tY', " + Long.toString(s) + ")", null));
		
	}
}
