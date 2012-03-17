/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.google.common.util.concurrent.FakeTimeLimiter;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import static com.laytonsmith.testing.StaticTest.SRun;
import static com.laytonsmith.testing.StaticTest.assertCEquals;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Layton
 */
public class StringHandlingTest {

    public StringHandlingTest() {
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
    
    @Test(timeout = 10000)
    public void testCC() throws ConfigCompileException{
        assertEquals("Thisshouldbeamess", SRun("cc(This should be a mess)", null));
    }
}
