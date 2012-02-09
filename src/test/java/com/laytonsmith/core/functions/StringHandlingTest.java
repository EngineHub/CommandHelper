/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

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
    
    @Test
    public void testConcat(){
        StringHandling.concat a = new StringHandling.concat();
        assertCEquals(C.onstruct("1234"), a.exec(0, null, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(4)));
        assertCEquals(C.onstruct("astring"), a.exec(0, null, null, C.onstruct("a"), C.onstruct("string")));
    }
    
    @Test
    public void testLength(){
        StringHandling.length a = new StringHandling.length();
        assertCEquals(C.onstruct(5), a.exec(0, null, null, C.onstruct("12345")));
        assertCEquals(C.onstruct(2), a.exec(0, null, null, C.Array(C.onstruct(0), C.onstruct(1))));
    }
    
    @Test
    public void testParseArgs(){
        StringHandling.parse_args a = new StringHandling.parse_args();
        assertCEquals(C.Array(C.onstruct("one"), C.onstruct("two")), a.exec(0, null, null, C.onstruct("one   two")));
        assertCEquals(C.Array(C.onstruct("one"), C.onstruct("two")), a.exec(0, null, null, C.onstruct("one two")));
    }
    
    @Test
    public void testRead(){
        
    }
    
    @Test
    public void testReplace(){
        StringHandling.replace a = new StringHandling.replace();
        assertCEquals(C.onstruct("yay"), a.exec(0, null, null, C.onstruct("yayathing"), C.onstruct("athing"), C.onstruct("")));
        assertCEquals(C.onstruct("yaymonkey"), a.exec(0, null, null, C.onstruct("yayathing"), C.onstruct("athing"), C.onstruct("monkey")));
        assertCEquals(C.onstruct("yayathing"), a.exec(0, null, null, C.onstruct("yayathing"), C.onstruct("wut"), C.onstruct("chicken")));
    }
    
    @Test
    public void testSconcat() throws ConfigCompileException{
        StringHandling.sconcat a = new StringHandling.sconcat();
        assertEquals("1 2 3 4", SRun("1 2 3 4", null));
        assertEquals("a string", SRun("a string", null));
    }
    
    @Test
    public void testSubstr(){
        StringHandling.substr a = new StringHandling.substr();
        assertCEquals(C.onstruct("urge"), a.exec(0, null, null, C.onstruct("hamburger"), C.onstruct(4), C.onstruct(8)));
        assertCEquals(C.onstruct("mile"), a.exec(0, null, null, C.onstruct("smiles"), C.onstruct(1), C.onstruct(5)));
        assertCEquals(C.onstruct("ning"), a.exec(0, null, null, C.onstruct("lightning"), C.onstruct(5)));
    }
    
    @Test
    public void testToUpper(){
        StringHandling.to_upper a = new StringHandling.to_upper();
        assertCEquals(C.onstruct("TESTING 123"), a.exec(0, null, null, C.onstruct("testing 123")));
        assertCEquals(C.onstruct("TESTING 123"), a.exec(0, null, null, C.onstruct("TeStInG 123")));
    }
    
    @Test
    public void testToLower(){
        StringHandling.to_lower a = new StringHandling.to_lower();
        assertCEquals(C.onstruct("testing 123"), a.exec(0, null, null, C.onstruct("TESTING 123")));
        assertCEquals(C.onstruct("testing 123"), a.exec(0, null, null, C.onstruct("TeStInG 123")));
    }
    
    @Test
    public void testTrim(){
        StringHandling.trim a = new StringHandling.trim();
        assertCEquals(C.onstruct("test 123"), a.exec(0, null, null, C.onstruct("    test 123    ")));
        assertCEquals(C.onstruct("test   123"), a.exec(0, null, null, C.onstruct("test   123")));
    }
}
