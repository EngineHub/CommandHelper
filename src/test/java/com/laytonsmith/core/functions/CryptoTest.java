/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.exceptions.ConfigCompileException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;

/**
 *
 * @author Layton
 */
public class CryptoTest {
    
    public CryptoTest() {
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

    @Test public void testRot13() throws ConfigCompileException{
        assertEquals("hello world", SRun("rot13(uryyb jbeyq)", null));
    }
    
    @Test public void testMd5() throws ConfigCompileException{
        assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", SRun("md5('hello world')", null));
    }
    
    @Test public void testSha1() throws ConfigCompileException{
        assertEquals("2aae6c35c94fcfb415dbe95f408b9ce91ee846ed", SRun("sha1('hello world')", null));
    }
}
