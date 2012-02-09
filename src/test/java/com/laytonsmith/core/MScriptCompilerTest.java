/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;
import org.junit.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Layton
 */
public class MScriptCompilerTest {

    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();

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
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
        env.SetPlayer(fakePlayer);
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
        e.add(new Token(Token.TType.COMMAND, "/cmd", 1, null));
        e.add(new Token(Token.TType.ALIAS_END, "=", 1, null));
        e.add(new Token(Token.TType.FUNC_NAME, "msg", 1, null));
        e.add(new Token(Token.TType.FUNC_START, "(", 1, null));
        e.add(new Token(Token.TType.STRING, "string", 1, null));
        e.add(new Token(Token.TType.FUNC_END, ")", 1, null));
        e.add(new Token(Token.TType.NEWLINE, "\n", 2, null));

        List result = MScriptCompiler.lex(config, null);
        assertEquals(e, result);

        String[] badConfigs = {
            "'\\q'", //Bad escape sequences
            "'\\r'",};
        for (String c : badConfigs) {
            try {
                MScriptCompiler.lex(c, null);
                //Shouldn't get here
                fail(c + " should not have lexed, but did.");
            } catch (ConfigCompileException ex) {
                //Success!
            }
        }
    }
    @Test
    public void testCompile() throws ConfigCompileException {
        System.out.println("compile");
        MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)", null), env).get(0).compileRight();
        try {
            //extra parameter
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another, oops) function)", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //missing parenthesis
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra parenthesis
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)))))", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra parenthesis
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg((this is a string, if(true, and, another) function))", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra multiline end construct
            MScriptCompiler.preprocess(MScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function) <<<", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }

        MScriptCompiler.compile(MScriptCompiler.lex("if(1, msg('') msg(''))", null));
    }

    @Test
    public void testExecute1() throws ConfigCompileException {
        String script = "proc(_hello, @hello0,"
                + "         msg(@hello0)"
                + "      )"
                + "      assign(@hello1, 'hello')"
                + "      _hello(@hello1)";


        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute2() throws ConfigCompileException {
        String script =
                "proc(_hello,\n"
                + "     assign(@hello, 'hello')\n"
                + "     return(@hello)\n"
                + ")\n"
                + "assign(@blah, 'blah')\n"
                + "assign(@blah, _hello())\n"
                + "msg(@blah)\n";


        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    /**
     * Here we are testing for mismatched (or empty) square brackets. Essentially, it should throw an exception.
     */
    @Test
    public void testExecute3() {
        try {
            String script =
                    "[";
            MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
        try {
            String script =
                    "]";
            MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
    }

    @Test
    public void testExecute4() throws ConfigCompileException {
        String script =
                "proc(_hello,"
                + "     return('hello')"
                + ")"
                + "msg(_hello())";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute5() throws ConfigCompileException {
        String script =
                "proc(_hello,"
                + "     return('hello')"
                + ")"
                + "msg(_hello())";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute6() throws ConfigCompileException {
        String script =
                "#This is a comment invalid()'\"'' function\n"
                + "msg('hello')";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute7() throws ConfigCompileException {
        String script =
                "msg('hello') #This is a comment too invalid()'\"'' function\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test(expected=ConfigCompileException.class)
    public void testExecute8() throws ConfigCompileException {
        String script =
                "msg('hello') //This is no longer a comment too :( invalid()'\"'' function\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute9() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() */";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test(expected = ConfigCompileException.class)
    public void testExecute10() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() This will fail though, because the comment is unended.";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
    }

    @Test(expected = ConfigCompileException.class)
    public void testExecute11() throws ConfigCompileException {
        String script =
                "msg('hello') 'unended string";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
    }

    @Test
    public void testExecute12() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() */";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    } 
    
    @Test
    public void testExecute13() throws ConfigCompileException {
        String script =
                "assign(@a, array(0, 1, 2))"
                + "msg(@a[0])";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("0");
    }
    
    @Test
    public void testExecute14() throws ConfigCompileException {
        String script =
                "proc(_hello, assign(@i, 'world'),"
                + "     return(@i)"
                + ")"
                + "msg(_hello('hello'))"
                + "msg(_hello())";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    
    @Test public void testExecute15() throws ConfigCompileException{
        String script =
                "assign(@i, 0)\n"
                + "msg('@i is currently' @i)\n"
                + "proc(_out, @i,\n"
                + "     msg('@i is currently' @i 'and @j is' @j)\n"
                + ")\n"
                + "_out('hello')\n"
                + "assign(@j, 'goodbye')\n"
                + "_out('world')\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("@i is currently 0");
        verify(fakePlayer).sendMessage("@i is currently hello and @j is");
        verify(fakePlayer).sendMessage("@i is currently world and @j is");
    }
    
    @Test public void testExecute16() throws ConfigCompileException{
        String script =
                "proc(_myProc, @i, @j, @k, msg(@i @j @k))\n"
                + "_myProc()\n"
                + "_myProc(1)\n"
                + "_myProc(1, 2)\n"
                + "_myProc(1, 2, 3)\n"
                + "_myProc(1, 2, 3, 4)\n";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        //verify(fakePlayer).sendMessage("null null null");
        verify(fakePlayer).sendMessage("1");
        verify(fakePlayer).sendMessage("1 2");
        verify(fakePlayer, times(2)).sendMessage("1 2 3");
    }
    
    @Test public void testExecute17() throws ConfigCompileException{
        String script =
                "proc(_addition, @i, @j, msg(add(@i, @j)))\n"
                + "_addition(1, 1)\n"
                + "_addition(2, 2)";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        //verify(fakePlayer).sendMessage("null null null");
        verify(fakePlayer).sendMessage("2");
        verify(fakePlayer).sendMessage("4");       
    }
    
    @Test public void testExecute18() throws ConfigCompileException{
        String script =
                "proc(_myProc, msg(@arguments))\n"
                + "_myProc()\n"
                + "_myProc(1)\n"
                + "_myProc(1, 2)";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        //verify(fakePlayer).sendMessage("null null null");
        verify(fakePlayer).sendMessage("{}");
        verify(fakePlayer).sendMessage("{1}");       
        verify(fakePlayer).sendMessage("{1, 2}");       
    }
    
    /**
     * Variables are locked in when the procedure is defined
     * @throws ConfigCompileException 
     */
    @Test
    public void testExecute19() throws ConfigCompileException {
        String script =
                "assign(@j, 'world')\n"
                + "proc(_hello, assign(@i, @j),"
                + "     return(@i)"
                + ")\n"
                + "assign(@j, 'goodbye')\n"
                + "msg(_hello('hello'))"
                + "msg(_hello())";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    @Test
    public void testExecute20() throws ConfigCompileException {
        final AtomicBoolean bool = new AtomicBoolean(false);
        String script =
                "msg('hello') world";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, new MScriptComplete() {

            public void done(String output) {
                assertEquals("world", output.trim());
                bool.set(true);
            }
        }, null);
        verify(fakePlayer).sendMessage("hello");
        assertTrue(bool.get());
    }

    @Test
    public void testCompile1() {
        try {
            String config = "/cmd [$p] $q = msg('')";
            MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0).compile();
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
    }

    @Test
    public void testCompile2() {
        try {
            String config = "/cmd [$p=player()] = msg('')";
            MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0).compile();
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
    }

    @Test
    public void testCompile4() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg('hello') #This is a comment too invalid()'\"'' function\n"
                + "<<<";
        MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env);
    }
    
    @Test
    public void testCompile5() throws ConfigCompileException {
        String config = "label:/cmd = >>>\n"
                + "msg('hello') #This is a comment too invalid()'\"'' function\n"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        assertEquals("label", s.getLabel());
    }
    
    @Test
    public void testCompile6() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg(hello 'world \\\\ \\' \\n')"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(new ArrayList<Variable>(), env, null);
        verify(fakePlayer).sendMessage("hello world \\ ' ".trim());
    }
    
    @Test
    public void testCompile7() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg(hello) \\ msg(world)"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(new ArrayList<Variable>(), env, null);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    
    @Test
    public void testCompile8() throws ConfigCompileException {
        String config = "/cmd $one $ = >>>\n"
                + "msg($one) \\ msg($)"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        s.run(Arrays.asList(new Variable[]{new Variable("$one", "first", false, false, 0, null),
            new Variable("$", "several variables", false, true, 0, null)}), env, null);
        verify(fakePlayer).sendMessage("first");
        verify(fakePlayer).sendMessage("several variables");
    }
    
    @Test
    public void testCompile9() throws ConfigCompileException {
        String config = "/test [$var=1] = >>>\n"
                + "msg($var)"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        assertTrue(s.match("/test 2"));
        s.run(Arrays.asList(new Variable[]{new Variable("$var", "2", true, false, 0, null)}), env, null);
        verify(fakePlayer).sendMessage("2");
        assertTrue(s.match("/test"));
        s.run(new ArrayList<Variable>(), env, null);
        verify(fakePlayer).sendMessage("1");
    }
    
    @Test
    public void testCompile10() throws ConfigCompileException{
        String config = "/test $var = >>>\n"
                + "msg($var)"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        assertTrue(s.match("/test 2"));
        assertFalse(s.match("/test"));
        s.run(Arrays.asList(new Variable[]{new Variable("$var", "2", true, false, 0, null)}), env, null);
    }
    
    @Test public void testCompile11() throws ConfigCompileException{
        
        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
        when(CommandHelperPlugin.perms.hasPermission(fakePlayer.getName(), "ch.alias.safe")).thenReturn(true);
        CommandHelperPlugin.myServer = fakeServer;
        when(fakeServer.getOnlinePlayers()).thenReturn(new MCPlayer[]{fakePlayer});
        String config = "safe:/test $var = >>>\n"
                + "all_players()\n"
                + "msg($var)\n"
                + "<<<";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);        
        s.compile();
        assertEquals("safe", s.getLabel());
        assertTrue(s.match("/test 2"));
        s.run(Arrays.asList(new Variable[]{new Variable("$var", "2", true, false, 0, null)}), env, null);
        verify(fakePlayer).sendMessage("2");
        verify(CommandHelperPlugin.perms).hasPermission(fakePlayer.getName(), "ch.alias.safe");
    }
    
    @Test public void testCompile12() throws ConfigCompileException{
        String config = "/*/one = bad()*/\n"
                + "/two = msg('Good')\n";
        Script s = MScriptCompiler.preprocess(MScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        assertFalse(s.match("/one"));
        assertTrue(s.match("/two"));
    }
    
    @Test public void testUnicode() throws ConfigCompileException{
        SRun("msg('\\u0037 is win!')", fakePlayer);
        verify(fakePlayer).sendMessage("7 is win!");
        SRun("msg('\\u20ac')", fakePlayer);
        verify(fakePlayer).sendMessage("€");
    }
    
    
}
