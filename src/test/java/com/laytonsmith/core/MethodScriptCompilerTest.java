

package com.laytonsmith.core;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.RunCommand;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author Layton
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CommandHelperPlugin.class)
@PowerMockIgnore({"javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*"})
public class MethodScriptCompilerTest {

    MCServer fakeServer;
    MCPlayer fakePlayer;
    com.laytonsmith.core.environments.Environment env;

    public MethodScriptCompilerTest() {
		//StaticTest.InstallFakeServerFrontend();
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
		new File("profiler.config").deleteOnExit();
    }

    @Before
    public void setUp() throws Exception {        
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
		env = Static.GenerateStandaloneEnvironment();
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of lex method, of class MethodScriptCompiler.
     */
    @Test
    public void testLex() throws Exception {
        String config = "/cmd = msg('string')";
        List e = null;
        e = new ArrayList();
        //This is the decomposed version of the above config
        e.add(new Token(Token.TType.COMMAND, "/cmd", Target.UNKNOWN));
        e.add(new Token(Token.TType.WHITESPACE, " ", Target.UNKNOWN));
        e.add(new Token(Token.TType.ALIAS_END, "=", Target.UNKNOWN));
        e.add(new Token(Token.TType.WHITESPACE, " ", Target.UNKNOWN));
        e.add(new Token(Token.TType.FUNC_NAME, "msg", Target.UNKNOWN));
        e.add(new Token(Token.TType.FUNC_START, "(", Target.UNKNOWN));
        e.add(new Token(Token.TType.STRING, "string", Target.UNKNOWN));
        e.add(new Token(Token.TType.FUNC_END, ")", Target.UNKNOWN));
        e.add(new Token(Token.TType.NEWLINE, "\n", Target.UNKNOWN));

        List result = MethodScriptCompiler.lex(config, null, false);
        assertEquals(e, result);

        String[] badConfigs = {
            "'\\q'", //Bad escape sequences
            "'\\r'",};
        for (String c : badConfigs) {
            try {
                MethodScriptCompiler.lex(c, null, false);
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
        MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)", null, false)).get(0).compileRight();
        try {
            //extra parameter
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another, oops) function)", null, false)).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //missing parenthesis
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function", null, false)).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra parenthesis
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function)))))", null, false)).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        try {
            //extra multiline end construct
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = msg(this is a string, if(true, and, another) function) <<<", null, false)).get(0).compileRight();
            fail("Did not expect test to pass");
        } catch (ConfigCompileException e) {
            //passed
        }
        
        try{
            //no multiline end construct
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("/cmd = >>>\nmsg('hi')\n", null, false)).get(0).compileRight();
            fail("Did not expect no multiline end construct to pass");
        } catch(ConfigCompileException e){
            //passed
        }

        MethodScriptCompiler.compile(MethodScriptCompiler.lex("if(1, msg('') msg(''))", null, true));
        
    }
    
    @Test public void testLabel() throws ConfigCompileException{
        assertEquals(PermissionsResolver.GLOBAL_PERMISSION, MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("*:/cmd = die()", null, false)).get(0).compile().getLabel());
        assertEquals(PermissionsResolver.GLOBAL_PERMISSION, MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("* : /cmd = die()", null, false)).get(0).compile().getLabel());
        assertEquals("~lol/fun", MethodScriptCompiler.preprocess(MethodScriptCompiler.lex("~lol/fun: /cmd = die()", null, false)).get(0).compile().getLabel());
    }
    
    @Test
    public void testCompile13() throws ConfigCompileException{
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("msg ('hi')", null, true));
    }
    
    @Test public void testCompile14() throws ConfigCompileException{
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("msg(('hi'))", null, true));
    }
    
    @Test
    public void testCompile15() throws ConfigCompileException{
        try{
            SRun("\n\nmsg(/, 'test')\n\n", fakePlayer);
        } catch(ConfigCompileException e){
            assertEquals("3", e.getLineNum());
        }
    }

    @Test
    public void testExecute1() throws ConfigCompileException {
        String script = "proc(_hello, @hello0,"
                + "         msg(@hello0)"
                + "      )"
                + "      assign(@hello1, 'hello')"
                + "      _hello(@hello1)";


        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
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


        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
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
            MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
        try {
            String script =
                    "]";
            MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute5() throws ConfigCompileException {
        String script =
                "proc(_hello,"
                + "     return('hello')"
                + ")"
                + "msg(_hello())";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute6() throws ConfigCompileException {
        String script =
                "#This is a comment invalid()'\"'' function\n"
                + "msg('hello')";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute7() throws ConfigCompileException {
        String script =
                "msg('hello') #This is a comment too invalid()'\"'' function\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test(expected=ConfigCompileException.class)
    public void testExecute8() throws ConfigCompileException {
        String script =
                "msg('hello') //This is no longer a comment too :( invalid()'\"'' function\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test
    public void testExecute9() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() */";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    }

    @Test(expected = ConfigCompileException.class)
    public void testExecute10() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() This will fail though, because the comment is unended.";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
    }

    @Test(expected = ConfigCompileException.class)
    public void testExecute11() throws ConfigCompileException {
        String script =
                "msg('hello') 'unended string";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
    }

    @Test
    public void testExecute12() throws ConfigCompileException {
        String script =
                "msg('hello') /* This is a comment too invalid()'\"'' function\n"
                + "yup, still a comment. yay() */";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
    } 
    
    @Test
    public void testExecute13() throws ConfigCompileException {
        String script =
                "assign(@a, array(0, 1, 2))"
                + "msg(@a[0])";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    
    @Test public void testExecute15() throws ConfigCompileException{
        String script =
                "assign(@i, 0)\n"
                + "msg('@i is currently' @i)\n"
                + "proc(_out, @i,\n"
                + "     msg(trim('@i is currently' @i 'and @j is' @j))\n"
                + ")\n"
                + "_out('hello')\n"
                + "assign(@j, 'goodbye')\n"
                + "_out('world')\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("@i is currently 0");
        verify(fakePlayer).sendMessage("@i is currently hello and @j is");
        verify(fakePlayer).sendMessage("@i is currently world and @j is");
    }
    
    @Test public void testExecute16() throws ConfigCompileException{
        String script =
                "proc(_myProc, @i, @j, @k, msg(trim(@i @j @k)))\n"
                + "_myProc()\n"
                + "_myProc(1)\n"
                + "_myProc(1, 2)\n"
                + "_myProc(1, 2, 3)\n"
                + "_myProc(1, 2, 3, 4)\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("1");
        verify(fakePlayer).sendMessage("1 2");
        verify(fakePlayer, times(2)).sendMessage("1 2 3");
    }
    
    @Test public void testExecute17() throws ConfigCompileException{
        String script =
                "proc(_addition, @i, @j, msg(add(@i, @j)))\n"
                + "_addition(1, 1)\n"
                + "_addition(2, 2)";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
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
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        verify(fakePlayer).sendMessage("world");
    }
    @Test
    public void testExecute20() throws ConfigCompileException {
        final AtomicBoolean bool = new AtomicBoolean(false);
        String script =
                "msg('hello') world";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, new MethodScriptComplete() {

			@Override
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
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0).compile();
            fail("Test passed, but wasn't supposed to");
        } catch (ConfigCompileException ex) {
            //Passed
        }
    }
    
    @Test
    public void testCompileWithDoubleSlashCommand() throws ConfigCompileException{
        AliasCore ac = mock(AliasCore.class);
        ac.autoIncludes = new ArrayList<File>();
        PowerMockito.mockStatic(CommandHelperPlugin.class);
        when(CommandHelperPlugin.getCore()).thenReturn(ac);
        assertEquals(ac, CommandHelperPlugin.getCore());
        String config = "//cmd blah = msg('success')";
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0);
        s.compile();
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);		
        s.run(Arrays.asList(new Variable("$var", "hello", Target.UNKNOWN)), env, null);
        verify(fakePlayer).sendMessage("success");
    }
    
    @Test
    public void testCompileTwoAliases() throws ConfigCompileException{
        AliasCore ac = mock(AliasCore.class);
        ac.autoIncludes = new ArrayList<File>();
        PowerMockito.mockStatic(CommandHelperPlugin.class);
        when(CommandHelperPlugin.getCore()).thenReturn(ac);
        assertEquals(ac, CommandHelperPlugin.getCore());
        String config = "/cmd1 = msg('success')\n"
                + "                 \n" //Spaces and tabs are here
                + "/cmd2 = msg('success')";
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
        Script s1 = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0);
        s1.compile();
        s1.run(Arrays.asList(new Variable("$var", "hello", Target.UNKNOWN)), env, null);
        Script s2 = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(1);
        s2.compile();
        s2.run(Arrays.asList(new Variable("$var", "hello", Target.UNKNOWN)), env, null);
        verify(fakePlayer, times(2)).sendMessage("success");
    }

    @Test
    public void testCompile2() {
        try {
            String config = "/cmd [$p=player()] = msg('')";
            MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0).compile();
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
        MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false));
    }
    
    @Test
    public void testCompile5() throws ConfigCompileException {
        String config = "label:/cmd = >>>\n"
                + "msg('hello') #This is a comment too invalid()'\"'' function\n"
                + "<<<";
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0);
        s.compile();
        assertEquals("label", s.getLabel());
    }
    
    @Test
    public void testCompile6() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg(trim(hello 'world \\\\ \\' \\n'))"
                + "<<<";
        RunCommand(config, fakePlayer, "/cmd");
        verify(fakePlayer).sendMessage("hello world \\ '");
    }
    
    @Test
    public void testCompile7() throws ConfigCompileException {
        String config = "/cmd = >>>\n"
                + "msg(hello) \\ msg(world)"
                + "<<<";
        RunCommand(config, fakePlayer, "/cmd");
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
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0);
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
        Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(config, null, false)).get(0);
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
    
    @Test public void testInfixMath3() throws ConfigCompileException{
        assertEquals("8.0", SRun("2 ** 3", fakePlayer));
    }
    
    @Test public void testUnary() throws ConfigCompileException{
        assertEquals("1", SRun("2 + - 1", fakePlayer));
    }
    
    @Test public void testSymbolicConcat() throws ConfigCompileException{
        SRun("@hello = 'hello'\n"
				+ "@world = 'world'\n"
				+ "msg(@hello.@world)", fakePlayer);
		verify(fakePlayer).sendMessage("helloworld");
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
        } catch(ConfigRuntimeException e){
            //pass?
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
        MethodScriptCompiler.compile(MethodScriptCompiler.lex("2 / 0", null, true));
    }
    
    @Test public void testLineNumberCorrectInException1() throws ConfigCompileException{
        String script = 
                "try(\n" //Line 1
                + "assign(@a, array(1, 2))\n" //Line 2
                + "\n" //Line 3
                + "assign(@d, @a[@b])\n" //Line 4
                + "\n" //Line 5
                + ", @e, msg(@e[3]))\n"; //Line 6
        SRun(script, fakePlayer);
        verify(fakePlayer).sendMessage("4");                
    }
    
    @Test public void testLineNumberCorrectInException2() throws ConfigCompileException{
        String script = 
                "assign(@a, array(1, 2))\n" //Line 1
                + "\n" //Line 2
                + "assign(@d, @a[@b])\n"; //Line 3
        try{
            SRun(script, fakePlayer);
        } catch(ConfigRuntimeException e){
            assertEquals(3, e.getLineNum());
        }
        
    }
    
    @Test public void testLineNumberCorrectInException3() throws ConfigCompileException{
        String script = 
                "\n"
                + "assign(@a, array('aaa', 'bbb'))\n"
                + "assign(@b, 'test')\n"
                + "msg('test1')\n"
                + "assign(@d, @a[@b])\n"
                + "msg('test2')\n";
        try{
            SRun(script, fakePlayer);
        } catch(ConfigRuntimeException e){
            assertEquals(5, e.getLineNum());
        }
    }
    
    @Test public void testExtraParenthesis(){
        try{
            SRun("\n"
                    + "\n"
                    + "player())\n", fakePlayer);
        } catch(ConfigCompileException e){
            assertEquals("3", e.getLineNum());
        }
    }
    
    @Test(expected=ConfigCompileException.class) 
    public void testSpuriousSymbols() throws ConfigCompileException{
        SRun("2 +", fakePlayer);
    }
    
    @Test
    public void testBraceIf() throws ConfigCompileException{
        SRun("if(true)\n\n"
                + "{\n"
                + "msg('success!')\n"
                + "}", fakePlayer);
        verify(fakePlayer).sendMessage("success!");
    }
    
    @Test
    public void testBraceElseIfElse() throws ConfigCompileException{
        SRun("if(false){"
                + "msg('fail')"
                + "} else if(true == true){"
                + "msg('success!')"
                + "} else {\n"
                + "msg('fail')"
                + "}", fakePlayer);
        verify(fakePlayer).sendMessage("success!");
    }
    
    @Test
    public void testBraceElseIfElseWithElseCondTrue() throws ConfigCompileException{
        SRun("if(false){"
                + "msg('fail')"
                + "} else if(false){"
                + "msg('fail')"
                + "} else {"
                + "msg('success!')"
                + "}", fakePlayer);
        verify(fakePlayer).sendMessage("success!");
    }
    
    @Test(expected=ConfigCompileException.class)
    public void testFailureOfBraces() throws ConfigCompileException{
        SRun("and(1){ 1 }", fakePlayer);
    }
    
    @Test(expected=ConfigCompileException.class)
    public void testInnerElseInElseIf() throws ConfigCompileException{
        SRun("if(true){"
                + "msg('fail')"
                + "} else {"
                + "msg('fail')"
                + "} else if(true){"
                + "msg('fail')"
                + "}", fakePlayer);
    }
    
    @Test
    public void testBracketsOnFor() throws ConfigCompileException{
        SRun("for(assign(@i, 0), @i < 5, @i++){\n"
                + "msg('worked')\n"
                + "}", fakePlayer);
        verify(fakePlayer, times(5)).sendMessage("worked");
    }
    
    @Test
    public void testBracketsOnForeach() throws ConfigCompileException{
        SRun("foreach(range(2), @i){\n"
                + "msg(@i)\n"
                + "}", fakePlayer);
        verify(fakePlayer).sendMessage("0");
        verify(fakePlayer).sendMessage("1");
    }
    
    @Test
    public void testBracketsOnSwitch() throws ConfigCompileException{
        SRun("switch('hi'){\n"
                + "'nope',"
                + "     msg('fail'),"
                + "'hi',"
                + "     msg('pass')"
                + "}", fakePlayer);
        verify(fakePlayer).sendMessage("pass");
    }     
    
    @Test public void testWhitespaceInBetweenFunctionAndParen() throws ConfigCompileException{
        SRun("msg ('hi')", fakePlayer);
        verify(fakePlayer).sendMessage("hi");
    }
    
    @Test public void testMultipleFunctionsWithBraces() throws ConfigCompileException{
        SRun("if(dyn(false)){\n"
                + "msg('nope')\n"
                + "} else {\n"
                + " msg('hi')\n"
                + " msg('hi')\n"
                + "}", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("hi");
    }
    
    @Test
    public void testArrayGetCatchesInvalidParameter(){
        try{
            try {
                SRun("1[4]", null);
                fail("Did not expect test to pass");
            } catch(ConfigCompileException e){
                //Pass
            }
            try {
                SRun("'string'['index']", null);
                fail("Did not expect test to pass");
            } catch(ConfigCompileException e){
                //Pass
            }
        } catch(ConfigRuntimeException e){
            fail("Expecting a compile error here, not a runtime exception");
        }
    }
    
    @Test
    public void testBlockComments1() throws ConfigCompileException{
        SRun(     "/*\n"
                + " * Herp\n"
                + " * Derp\n"
                + " */\n", fakePlayer);
    }
    
    @Test
    public void testCommentsInStrings() throws ConfigCompileException{
	    SRun("'#'", fakePlayer);
    }
    
    @Test
    public void testCommentsInStrings2() throws ConfigCompileException{
	    SRun("'/*'", fakePlayer);
    }
	
	@Test
	public void testDoubleQuotesInSingleQuotes() throws Exception{
		SRun("'This \"should work\" correctly, and not throw an exception'", null);
	}
	
	@Test public void testClosureToString1() throws Exception{
		SRun("msg(closure(msg('')))", fakePlayer);
		verify(fakePlayer).sendMessage("msg('')");
	}
	
	@Test public void testClosureToString2() throws Exception{
		SRun("msg(closure('\\n'))", fakePlayer);
		verify(fakePlayer).sendMessage("'\\n'");
	}
	
	@Test public void testClosureToString3() throws Exception{
		SRun("msg(closure('\\\\'))", fakePlayer);
		verify(fakePlayer).sendMessage("'\\\\'");
	}
    
	@Test public void testClosureToString4() throws Exception{
		SRun("msg(closure('\\''))", fakePlayer);
		verify(fakePlayer).sendMessage("'\\''");
	}
	
	@Test public void testCSlices() throws Exception{
		assertEquals("-1", SRun("-1..-3[0]", null));
		assertEquals("-2", SRun("-1..-3[1]", null));
		assertEquals("-3", SRun("-1..-3[2]", null));
		
		assertEquals("0", SRun("0..2[0]", null));
		assertEquals("1", SRun("0..2[1]", null));
		assertEquals("2", SRun("0..2[2]", null));
		
		assertEquals("5", SRun("5..3[0]", null));
		assertEquals("4", SRun("5..3[1]", null));
		assertEquals("3", SRun("5..3[2]", null));
		
		assertEquals("1", SRun("1..3[0]", null));
		assertEquals("2", SRun("1..3[1]", null));
		assertEquals("3", SRun("1..3[2]", null));
		
		try{
			SRun("1..2[10]", null);
			fail("Expected an exception");
		} catch (ConfigRuntimeException e){
			//pass
		}
		
		assertEquals("2", SRun("array_size(1..2)", null));
		
		assertEquals("true", SRun("array_contains(1..2, 1)", null));
		assertEquals("false", SRun("array_contains(1..2, 3)", null));
		
		assertEquals("true", SRun("array_contains(-1..-2, -2)", null));
		assertEquals("false", SRun("array_contains(-1..-2, -3)", null));
		
		assertEquals("true", SRun("array_index_exists(1..2, 0)", null));
		assertEquals("false", SRun("array_index_exists(1..2, 2)", null));
	}
	
	@Test public void testWhitespaceAroundSymbol1() throws Exception{
		assertEquals("true", SRun("false == false", null));
	}
	//This is accounted for in the new compiler branch, and will not be fixed in this branch.
//	@Test public void testWhitespaceAroundSymbol2() throws Exception{
//		assertEquals("true", SRun("false==false", null));
//	}
    
    //TODO: Once the lexer is rewritten, this should work
//    @Test
//    public void testAssignmentWithEquals1() throws ConfigCompileException{
//	    SRun("@var=yep nope msg(@var)", fakePlayer);
//	    verify(fakePlayer).sendMessage("yep");
//    }
//    
//    @Test
//    public void testAssignmentWithEquals2() throws ConfigCompileException{
//	    SRun("@var = yep nope msg(@var)", fakePlayer);
//	    verify(fakePlayer).sendMessage("yep");	    
//    }
//    
//    @Test
//    public void testAssignmentWithEquals3() throws ConfigCompileException{
//	    SRun("@var = 'yep yep' msg(@var)", fakePlayer);
//	    verify(fakePlayer).sendMessage("yep yep");	    	    
//    }
    
}
