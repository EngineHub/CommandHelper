/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.ConfigCompileException;
import com.laytonsmith.aliasengine.Constructs.Token;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class MScriptCompilerTest {
    
    public MScriptCompilerTest() {
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

    /**
     * Test of lex method, of class MScriptCompiler.
     */
    @Test
    public void testLex() throws Exception {
        System.out.println("lex");
        String config = "/cmd = msg('string')";
        List e = null;
        e = new ArrayList();
        //This is the decomposed version of the above config
        e.add(new Token(Token.TType.UNKNOWN, "/cmd", 1));
        e.add(new Token(Token.TType.ALIAS_END, "=", 1));
        e.add(new Token(Token.TType.FUNC_NAME, "msg", 1));
        e.add(new Token(Token.TType.FUNC_START, "(", 1));
        e.add(new Token(Token.TType.STRING, "string", 1));
        e.add(new Token(Token.TType.FUNC_END, ")", 1));
        e.add(new Token(Token.TType.NEWLINE, "\n", 2));
        
        List result = MScriptCompiler.lex(config);
        assertEquals(e, result);
        
        String [] badConfigs = {
            "'\\q'", //Bad escape sequences
            "'\\r'",
        };
        for(String c : badConfigs){
            try{
                MScriptCompiler.lex(c);
                //Shouldn't get here
                fail(c + " should not have lexed, but did.");
            } catch(ConfigCompileException ex){
                //Success!
            }
        }
    }

//    /**
//     * Test of preprocess method, of class MScriptCompiler.
//     */
//    @Test
//    public void testPreprocess() throws Exception {
//        System.out.println("preprocess");
//        List<Token> tokenStream = null;
//        List expResult = null;
//        List result = MScriptCompiler.preprocess(tokenStream);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of compile method, of class MScriptCompiler.
//     */
//    @Test
//    public void testCompile() throws Exception {
//        System.out.println("compile");
//        List<Token> tokenStream = null;
//        GenericTreeNode expResult = null;
//        GenericTreeNode result = MScriptCompiler.compile(tokenStream);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

//    @Test
//    public void testPreprocess() throws Exception {
//        System.out.println("preprocess");
//        List<Token> tokenStream = null;
//        List expResult = null;
//        List result = MScriptCompiler.preprocess(tokenStream);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testCompile() throws Exception {
//        System.out.println("compile");
//        List<Token> tokenStream = null;
//        GenericTreeNode expResult = null;
//        GenericTreeNode result = MScriptCompiler.compile(tokenStream);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    @Test public void testCompile() throws ConfigCompileException{
        System.out.println("compile");        
        MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)")).get(0).compileRight();
        try{
            //extra parameter
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another, oops) function)")).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch(ConfigCompileException e){
            //passed
        }
        try{
            //missing parenthesis
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function")).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch(ConfigCompileException e){
            //passed
        }
        try{
            //extra parenthesis
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another, oops) function)))))")).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch(ConfigCompileException e){
            //passed
        }
        try{
            //extra parenthesis
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg((this is a string, if(true, and, another, oops) function))")).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch(ConfigCompileException e){
            //passed
        }
    }
}
