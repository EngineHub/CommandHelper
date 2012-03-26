/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
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
public class MethodScriptCompilerTest {

    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();

    public MethodScriptCompilerTest() {
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
     * Test of lex method, of class MethodScriptCompiler.
     */
    @Test
    public void testLex() throws Exception {
        System.out.println("lex");
        String config = "/cmd = msg('string')";
        List e = null;
        e = new ArrayList();
        //This is the decomposed version of the above config
        e.add(new Token(Token.TType.COMMAND, "/cmd", Target.UNKNOWN));
        e.add(new Token(Token.TType.ALIAS_END, "=", Target.UNKNOWN));
        e.add(new Token(Token.TType.FUNC_NAME, "msg", Target.UNKNOWN));
        e.add(new Token(Token.TType.FUNC_START, "(", Target.UNKNOWN));
        e.add(new Token(Token.TType.STRING, "string", Target.UNKNOWN));
        e.add(new Token(Token.TType.FUNC_END, ")", Target.UNKNOWN));
        e.add(new Token(Token.TType.NEWLINE, "\n", Target.UNKNOWN));

        List result = MethodScriptCompiler.lex(config, null);
        assertEquals(e, result);

        String[] badConfigs = {
            "'\\q'", //Bad escape sequences
            "'\\r'",};
        for (String c : badConfigs) {
            try {
                MethodScriptCompiler.lex(c, null);
                //Shouldn't get here
                fail(c + " should not have lexed, but did.");
            } catch (ConfigCompileException ex) {
                //Success!
            }
        }
    }
    @Test(expected=ConfigCompileException.class)
    public void testSmartStrings() throws ConfigCompileException{
        
        SRun("assign(@word, 'word')\n"
                + "msg(\"@word\")", fakePlayer);

    }
    @Test
    public void testCompile() throws ConfigCompileException {
        System.out.println("compile");
        MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)", null), env).get(0).compileRight();
        try {
            //extra parameter
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another, oops) function)", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //missing parenthesis
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra parenthesis
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)))))", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra multiline end construct
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function) <<<", null), env).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        
        try{
            //no multiline end construct
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = >>>\nmsg('hi')\n", null), env).get(0).compileRight();
            fail("Did not expect no multiline end construct to pass");
        } catch(ConfigCompileException e){
            //passed
        }

        MethodScriptCompiler.compile(MethodScriptCompiler.lex("if(1, msg('') msg(''))", null));
    }
    
    @Test
    public void testCompile13() throws ConfigCompileException{
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("msg ('hi')", null));
    }
    
    @Test public void testCompile14() throws ConfigCompileException{
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("msg(('hi'))", null));
    }

    @Test
    public void testExecute1() throws ConfigCompileException {
        String script = "proc(_hello, @hello0,"
                + "         msg(@hello0)"
                + "      )"
                + "      assign(@hello1, 'hello')"
                + "      _hello(@hello1)";


        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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


        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
            MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
        try {
            String script =
                    "]";
            MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute5() throws ConfigCompileException {
        String script =
                "proc(_hello,"
                + "     return('hello')"
                + ")"
                + "msg(_hello())";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute6() throws ConfigCompileException {
        String script =
                "#This is a comment invalid()'\"'' function\n"
                + "msg('hello')";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute7() throws ConfigCompileException {
        String script =
                "msg('hello') #This is a comment too invalid()'\"'' function\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test(expected=ConfigCompileException.class)
    public void testExecute8() throws ConfigCompileException {
        String script =
                "msg('hello') //This is no longer a comment too :( invalid()'\"'' function\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute9() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() */";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test(expected = ConfigCompileException.class)
    public void testExecute10() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() This will fail though, because the comment is unended.";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
    }

    @Test(expected = ConfigCompileException.class)
    public void testExecute11() throws ConfigCompileException {
        String script =
                "msg('hello') 'unended string";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
    }

    @Test
    public void testExecute12() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() */";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    } 
    
    @Test
    public void testExecute13() throws ConfigCompileException {
        String script =
                "assign(@a, array(0, 1, 2))"
                + "msg(@a[0])";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    @Test
    public void testExecute20() throws ConfigCompileException {
        final AtomicBoolean bool = new AtomicBoolean(false);
        String script =
                "msg('hello') world";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, new MethodScriptComplete() {

            public void done(String output) {
                assertEquals("world", output.trim());
                bool.set(true);
            }
        }, null);
        verify(fakePlayer).sendMessage("hello");
        assertTrue(bool.get());
    }
    
    @Test
    public void testExecute21() throws ConfigCompileException{
        String script = "#*\n"
                + "msg('Not a comment')\n"
                + "#*#";
        SRun(script, fakePlayer);
        verify(fakePlayer).sendMessage("Not a comment");
    }
    
    @Test public void testExecute22() throws ConfigCompileException{
        SRun("msg('hi' (this is a thing))", fakePlayer);
        verify(fakePlayer).sendMessage("hi this is a thing");
    }

    @Test
    public void testCompile1() {
        try {
            String config = "/cmd [$p] $q = msg('')";
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0).compile();
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
    }

    @Test
    public void testCompile2() {
        try {
            String config = "/cmd [$p=player()] = msg('')";
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0).compile();
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
        MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env);
    }
    
    @Test
    public void testCompile5() throws ConfigCompileException {
        String config = "label:/cmd = >>>\n"
                + "msg('hello') #This is a comment too invalid()'\"'' function\n"
                + "<<<";
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        assertEquals("label", s.getLabel());
    }
    
    @Test
    public void testCompile6() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg(hello 'world \\\\ \\' \\n')"
                + "<<<";
        SRun(config, fakePlayer);
        verify(fakePlayer).sendMessage("hello world \\ ' ".trim());
    }
    
    @Test
    public void testCompile7() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg(hello) \\ msg(world)"
                + "<<<";
        SRun(config, fakePlayer);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    
    //TODO: Make these tests possible
//    @Test
//    public void testCompile8() throws ConfigCompileException {
//        String config = "/cmd $one $ = >>>\n"
//                + "msg($one) \\ msg($)"
//                + "<<<";
//        
//        RunVars(Arrays.asList(new Variable[]{new Variable("$one", "first", false, false, Target.UNKNOWN),
//            new Variable("$", "several variables", false, true, Target.UNKNOWN)}), config, fakePlayer);
//        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0);
//        s.compile();
//        s.run(Arrays.asList(new Variable[]{new Variable("$one", "first", false, false, Target.UNKNOWN),
//            new Variable("$", "several variables", false, true, Target.UNKNOWN)}), env, null);
//        verify(fakePlayer).sendMessage("first");
//        verify(fakePlayer).sendMessage("several variables");
//    }
//    
//    @Test
//    public void testCompile9() throws ConfigCompileException {
//        String config = "/test [$var=1] = >>>\n"
//                + "msg($var)"
//                + "<<<";
//        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0);
//        s.compile();
//        assertTrue(s.match("/test 2"));
//        s.run(Arrays.asList(new Variable[]{new Variable("$var", "2", true, false, Target.UNKNOWN)}), env, null);
//        verify(fakePlayer).sendMessage("2");
//        assertTrue(s.match("/test"));
//        s.run(new ArrayList<Variable>(), env, null);
//        verify(fakePlayer).sendMessage("1");
//    }
    
    @Test
    public void testCompile10() throws ConfigCompileException{
        String config = "/test $var = >>>\n"
                + "msg($var)"
                + "<<<";
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0);
        s.compile();
        assertTrue(s.match("/test 2"));
        assertFalse(s.match("/test"));
        s.run(Arrays.asList(new Variable[]{new Variable("$var", "2", true, false, Target.UNKNOWN)}), env, null);
    }
    
    //TODO: Make this test possible
//    @Test public void testCompile11() throws ConfigCompileException{
//        
//        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
//        when(CommandHelperPlugin.perms.hasPermission(fakePlayer.getName(), "ch.alias.safe")).thenReturn(true);
//        CommandHelperPlugin.myServer = fakeServer;
//        when(fakeServer.getOnlinePlayers()).thenReturn(new MCPlayer[]{fakePlayer});
//        String config = "safe:/test $var = >>>\n"
//                + "all_players()\n"
//                + "msg($var)\n"
//                + "<<<";
//        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0);        
//        s.compile();
//        assertEquals("safe", s.getLabel());
//        assertTrue(s.match("/test 2"));
//        s.run(Arrays.asList(new Variable[]{new Variable("$var", "2", true, false, Target.UNKNOWN)}), env, null);
//        verify(fakePlayer).sendMessage("2");
//        verify(CommandHelperPlugin.perms).hasPermission(fakePlayer.getName(), "ch.alias.safe");
//    }
    
    @Test public void testCompile12() throws ConfigCompileException{
        String config = "/*/one = bad()*/\n"
                + "/two = msg('Good')\n";
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null), env).get(0);
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
    
    @Test public void testInfixMath1() throws ConfigCompileException{
        assertEquals("4", SRun("2 + 2", fakePlayer));
        assertEquals("4", SRun("8 - 4", fakePlayer));
        assertEquals("4", SRun("2 * 2", fakePlayer));
        assertEquals("4", SRun("16/4", fakePlayer));        
    }
    
    @Test public void testInfixMath2() throws ConfigCompileException{
        assertEquals("2", SRun("-2 + 2 + 2", fakePlayer));
        assertEquals("18", SRun("(2 + 4) * 3", fakePlayer));
        assertEquals("14", SRun("2 + 4 * 3", fakePlayer));
    }
    
    @Test public void testUnary() throws ConfigCompileException{
        assertEquals("1", SRun("2 + - 1", fakePlayer));
    }
    
    @Test public void testSymbolicLogic1() throws ConfigCompileException{
        assertEquals("true", SRun("2 <= 5", fakePlayer));
        assertEquals("false", SRun("2 === '2'", fakePlayer));
        assertEquals("true", SRun("g(assign(@var, false)) !@var", fakePlayer));        
    }
    
    @Test public void testSymbolicLogic2() throws ConfigCompileException{
        assertEquals("true", SRun("true || true", fakePlayer));
        assertEquals("false", SRun("true && false", fakePlayer));
    }
    
    @Test public void testComplexSymbolicLogic() throws ConfigCompileException{
        assertEquals("true", SRun("2 == 2 && true", fakePlayer));
    }
    
    @Test public void testSymbolCompileError(){
        try{
            SRun("(+ * 2)", fakePlayer);
            fail("Did not expect test to pass");
        } catch(ConfigCompileException e){
            //pass
        }
    }        
    
    @Test public void testComplexSymbols() throws ConfigCompileException{
        SRun("assign(@var, 2) if((@var + 2) == 4, msg('Success!'))", fakePlayer);
        verify(fakePlayer).sendMessage("Success!");
    }
    
    @Test public void testPostfix() throws ConfigCompileException{
        SRun("assign(@var, 2) msg(@var++) msg(@var)", fakePlayer);
        verify(fakePlayer).sendMessage("2");
        verify(fakePlayer).sendMessage("3");
    }
    
    @Test public void testPrefix() throws ConfigCompileException{
        SRun("assign(@var, 2) msg(++@var) msg(@var)", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("3");        
    }
    
    @Test public void testModulo() throws ConfigCompileException{
        assertEquals(Integer.toString(2 % 3), SRun("2 % 3", fakePlayer));
    }
    
    @Test public void TestOperationsWithFunction() throws ConfigCompileException{
        SRun("if(!and(false, false), msg('yes'))", fakePlayer);
        verify(fakePlayer).sendMessage("yes");
    }
    
    @Test public void testArrayBooleanType() throws ConfigCompileException{
        assertEquals("true", SRun("boolean(array(1))", null));
        assertEquals("false", SRun("boolean(array())", null));
    }
    
    @Test public void testParenthesisAfterQuotedString() throws ConfigCompileException{
        assertEquals("2 + 2 is 4", SRun("'2 + 2 is' (2 + 2)", fakePlayer));
    }
    
    @Test(expected=ConfigCompileException.class)
    public void testCompileErrorOfStaticConstructOptimization() throws ConfigCompileException{
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("2 / 0", null));
    }
    
    
}
