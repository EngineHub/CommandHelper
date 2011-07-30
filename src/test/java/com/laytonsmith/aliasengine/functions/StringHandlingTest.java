/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.*;
import static org.mockito.Mockito.*;

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
    public void testDocs() {
        System.out.println("docs");
        StaticTest.TestClassDocs(Math.docs(), Math.class);
    }
    
    @Test
    public void testConcat(){
        StringHandling.concat a = new StringHandling.concat();
        TestBoilerplate(a, "concat");
        assertCEquals(C.onstruct("1234"), a.exec(0, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(4)));
        assertCEquals(C.onstruct("astring"), a.exec(0, null, C.onstruct("a"), C.onstruct("string")));
    }
    
    @Test
    public void testLength(){
        StringHandling.length a = new StringHandling.length();
        TestBoilerplate(a, "length");
        assertCEquals(C.onstruct(5), a.exec(0, null, C.onstruct("12345")));
        assertCEquals(C.onstruct(2), a.exec(0, null, C.Array(C.onstruct(0), C.onstruct(1))));
    }
    
    @Test
    public void testParseArgs(){
        StringHandling.parse_args a = new StringHandling.parse_args();
        TestBoilerplate(a, "parse_args");
        assertCEquals(C.Array(C.onstruct("one"), C.onstruct("two")), a.exec(0, null, C.onstruct("one   two")));
        assertCEquals(C.Array(C.onstruct("one"), C.onstruct("two")), a.exec(0, null, C.onstruct("one two")));
    }
    
    @Test
    public void testRead(){
        
    }
    
    @Test
    public void testReplace(){
        StringHandling.replace a = new StringHandling.replace();
        TestBoilerplate(a, "replace");
        assertCEquals(C.onstruct("yay"), a.exec(0, null, C.onstruct("yayathing"), C.onstruct("athing"), C.onstruct("")));
        assertCEquals(C.onstruct("yaymonkey"), a.exec(0, null, C.onstruct("yayathing"), C.onstruct("athing"), C.onstruct("monkey")));
        assertCEquals(C.onstruct("yayathing"), a.exec(0, null, C.onstruct("yayathing"), C.onstruct("wut"), C.onstruct("chicken")));
    }
    
    @Test
    public void testSconcat(){
        StringHandling.sconcat a = new StringHandling.sconcat();
        TestBoilerplate(a, "sconcat");
        assertCEquals(C.onstruct("1 2 3 4"), a.exec(0, null, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(4)));
        assertCEquals(C.onstruct("a string"), a.exec(0, null, C.onstruct("a"), C.onstruct("string")));
    }
    
    @Test
    public void testSubstr(){
        StringHandling.substr a = new StringHandling.substr();
        TestBoilerplate(a, "substr");
        assertCEquals(C.onstruct("urge"), a.exec(0, null, C.onstruct("hamburger"), C.onstruct(4), C.onstruct(8)));
        assertCEquals(C.onstruct("mile"), a.exec(0, null, C.onstruct("smiles"), C.onstruct(1), C.onstruct(5)));
        assertCEquals(C.onstruct("ning"), a.exec(0, null, C.onstruct("lightning"), C.onstruct(5)));
    }
    
    @Test
    public void testToUpper(){
        StringHandling.to_upper a = new StringHandling.to_upper();
        TestBoilerplate(a, "to_upper");
        assertCEquals(C.onstruct("TESTING 123"), a.exec(0, null, C.onstruct("testing 123")));
        assertCEquals(C.onstruct("TESTING 123"), a.exec(0, null, C.onstruct("TeStInG 123")));
    }
    
    @Test
    public void testToLower(){
        StringHandling.to_lower a = new StringHandling.to_lower();
        TestBoilerplate(a, "to_lower");
        assertCEquals(C.onstruct("testing 123"), a.exec(0, null, C.onstruct("TESTING 123")));
        assertCEquals(C.onstruct("testing 123"), a.exec(0, null, C.onstruct("TeStInG 123")));
    }
    
    @Test
    public void testTrim(){
        StringHandling.trim a = new StringHandling.trim();
        TestBoilerplate(a, "trim");
        assertCEquals(C.onstruct("test 123"), a.exec(0, null, C.onstruct("    test 123    ")));
        assertCEquals(C.onstruct("test   123"), a.exec(0, null, C.onstruct("test   123")));
    }
}
