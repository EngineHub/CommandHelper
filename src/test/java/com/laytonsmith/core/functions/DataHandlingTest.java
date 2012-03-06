/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.MScriptCompiler;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class DataHandlingTest {
    
    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();

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
        env.SetPlayer(fakePlayer);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        
    }
    
    @Test(expected=ConfigRuntimeException.class) 
    public void testFor3() throws ConfigCompileException{
        String script = 
                "   assign(@array, array())"
                + " for('nope', lt(@i, 5), inc(@i),\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
        verify(fakePlayer).sendMessage("{1, 2}");
    }
    
    @Test public void testCallProcIsProc() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(true);
        String config = "/for = >>>\n"
                + " msg(is_proc(_proc))\n"
                + " proc(_proc,"
                + "     msg('hello world')"
                + " )"
                + " msg(is_proc(_proc))"
                + " call_proc(_proc)"
                + "<<<\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
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
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{}), env, null);
        verify(fakePlayer).sendMessage("{j:0, j:1}");
    }
    
    @Test public void testInclude() throws ConfigCompileException, IOException{
        String script = 
                "include('unit_test_inc.ms')";
        //Create the test file
        File test = new File("unit_test_inc.ms");
        FileUtility.write("msg('hello')", test);
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, new File("./script.txt"))), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        //delete the test file
        test.delete();
    }
    
    @Test public void testExportImportIVariable() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(true);
        String script1 = 
                "assign(@var, 10)"
                + "export(@var)";
        SRun(script1, null);
        SRun("import(@var) msg(@var)", fakePlayer);
        verify(fakePlayer).sendMessage("10");
    }
    
    @Test public void testExportImportStringValue() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("export('hi', 20)", fakePlayer);
        SRun("msg(import('hi'))", fakePlayer);
        verify(fakePlayer).sendMessage("20");
    }
    
    @Test public void testIsBoolean() throws ConfigCompileException{
        SRun("msg(is_boolean(1)) msg(is_boolean(true))", fakePlayer);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }
    
    @Test public void testIsInteger() throws ConfigCompileException{
        SRun("msg(is_integer(5.0)) msg(is_integer('s')) msg(is_integer(5))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }
    
    @Test public void testIsDouble() throws ConfigCompileException{
        SRun("msg(is_double(5)) msg(is_double('5.0')) msg(is_double(5.0))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }
    
    @Test public void testIsNull() throws ConfigCompileException{
        SRun("msg(is_null('null')) msg(is_null(null))", fakePlayer);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }
    
    @Test public void testIsNumeric() throws ConfigCompileException{
        SRun("msg(is_numeric('s')) msg(is_numeric(null)) msg(is_numeric(true)) msg(is_numeric(2))"
                + " msg(is_numeric(2.0))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("false");
        verify(fakePlayer, times(3)).sendMessage("true");
    }
    
    @Test public void testIsIntegral() throws ConfigCompileException{
        SRun("msg(is_integral(5.5)) msg(is_integral(5)) msg(is_integral(4.0))", fakePlayer);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer, times(2)).sendMessage("true");
    }
    
    @Test public void testDoubleCastToInteger() throws ConfigCompileException{
        SRun("msg(integer(4.5))", fakePlayer);
        verify(fakePlayer).sendMessage("4");
    }
    
    @Test public void testClosure1() throws ConfigCompileException{
        SRun("assign(@go, closure(console( 'Hello World' ))) msg(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("console('Hello World')");
    }
    
    @Test public void testClosure2() throws ConfigCompileException{
        SRun("assign(@go, closure(msg('Hello World')))", fakePlayer);
        verify(fakePlayer, times(0)).sendMessage("Hello World");
    }
    
    @Test public void testClosure3() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@go, closure(msg('Hello' 'World')))\n"
                + "execute(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }
    
    @Test public void testClosure4() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@hw, 'Hello World')\n"
                + "assign(@go, closure(msg(@hw)))\n"
                + "execute(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }
    
    @Test public void testClosure5() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@hw, 'Nope')\n"
                + "assign(@go, closure(@hw, msg(@hw)))\n"
                + "execute('Hello World', @go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }
    
    @Test public void testClosure6() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@hw, 'Hello World')\n"
                + "assign(@go, closure(msg(@hw)))\n"
                + "execute('Nope', @go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }
    
    @Test public void testClosure7() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@go, closure(assign(@hw, 'Hello World'), msg(@hw)))\n"
                + "execute(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }
}
