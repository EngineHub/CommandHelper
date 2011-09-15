/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import java.io.File;
import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import java.io.IOException;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import com.laytonsmith.aliasengine.Constructs.Variable;
import java.util.Arrays;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.MScriptCompiler;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class DataHandlingTest {
    
    Server fakeServer;
    Player fakePlayer;

    public DataHandlingTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testFor1() throws ConfigCompileException {
        String config = "/for = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{0, 1, 2, 3, 4}");
    }
    
    @Test(expected=ConfigRuntimeException.class) 
    public void testFor2() throws ConfigCompileException{
        String script = "/for = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), 'nope', inc(@i),\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
        
    }
    
    @Test(expected=ConfigRuntimeException.class) 
    public void testFor3() throws ConfigCompileException{
        String script = 
                "   assign(@array, array())"
                + " for('nope', lt(@i, 5), inc(@i),\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
        
    }
    
    @Test public void testForeach1() throws ConfigCompileException{
        String config = "/for = >>>\n"
                + " assign(@array, array(1, 2, 3, 4, 5))\n"
                + " assign(@array2, array())"
                + " foreach(@array, @i,\n"
                + "     array_push(@array2, @i)\n"
                + " )\n"
                + " msg(@array2)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{1, 2, 3, 4, 5}");
    }
    
    @Test public void testForeach2() throws ConfigCompileException{
        String config = "/for = >>>\n"
                + " assign(@array, array(1, 2, 3, 4, 5))\n"
                + " assign(@array2, array())"
                + " foreach(@array, @i,\n"
                + "     if(equals(@i, 1), continue(2))"
                + "     array_push(@array2, @i)\n"
                + " )\n"
                + " msg(@array2)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{3, 4, 5}");
    }
    
    @Test public void testForeach3() throws ConfigCompileException{
        String config = "/for = >>>\n"
                + " assign(@array, array(1, 2, 3, 4, 5))\n"
                + " assign(@array1, array(1, 2, 3, 4, 5))\n"
                + " assign(@array2, array())\n"
                + " foreach(@array1, @j,"
                + "     foreach(@array, @i,\n"
                + "         if(equals(@i, 3), break(2))"
                + "         array_push(@array2, @i)\n"
                + "     )\n"
                + " )"
                + " msg(@array2)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{1, 2}");
    }
    
    @Test public void testCallProcIsProc() throws ConfigCompileException{
        String config = "/for = >>>\n"
                + " msg(is_proc(_proc))\n"
                + " proc(_proc,"
                + "     msg('hello world')"
                + " )"
                + " msg(is_proc(_proc))"
                + " call_proc(_proc)"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
        verify(fakePlayer).sendMessage("hello world");
    }
    
    /**
     * There is a bug that causes an infinite loop, so we put a 10 second timeout
     * @throws ConfigCompileException 
     */
    @Test(timeout=10000) public void testContinue1() throws ConfigCompileException{
        String config = "/continue = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     if(equals(@i, 2), continue(1))\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{0, 1, 3, 4}");
    }
    
    @Test(timeout=10000) public void testContinue2() throws ConfigCompileException{
        String config = "/continue = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     if(equals(@i, 2), continue(2))\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{0, 1, 4}");
    }
    
    @Test(timeout=10000) public void testContinue3() throws ConfigCompileException{
        String config = "/continue = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     if(equals(@i, 2), continue(3))\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{0, 1}");
    }
    
    @Test public void testBreak1() throws ConfigCompileException{
        String config = "/break = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 2), inc(@i),\n"
                + "     for(assign(@j, 0), lt(@j, 5), inc(@j),\n"
                + "         if(equals(@j, 2), break())\n"
                + "         array_push(@array, concat('j:', @j))\n"
                + "     )\n"
                + "     array_push(@array, concat('i:', @i))\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{j:0, j:1, i:0, j:0, j:1, i:1}");
    }
    
    @Test public void testBreak2() throws ConfigCompileException{
        String config = "/break = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 2), inc(@i),\n"
                + "     for(assign(@j, 0), lt(@j, 5), inc(@j),\n"
                + "         if(equals(@j, 2), break(2))\n"
                + "         array_push(@array, concat('j:', @j))\n"
                + "     )\n"
                + "     array_push(@array, concat('i:', @i))\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null)).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), fakePlayer, null);
        verify(fakePlayer).sendMessage("{j:0, j:1}");
    }
    
    @Test public void testInclude() throws ConfigCompileException, IOException{
        String script = 
                "include('unit_test_inc.ms')";
        //Create the test file
        File test = new File("unit_test_inc.ms");
        FileUtility.write("msg('hello')", test);
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
        verify(fakePlayer).sendMessage("hello");
        //delete the test file
        test.delete();
    }
}
