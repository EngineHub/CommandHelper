

package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.RunCommand;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.io.File;
import java.io.IOException;
import org.junit.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author layton
 */
public class DataHandlingTest {

    MCServer fakeServer;
    MCPlayer fakePlayer;
    com.laytonsmith.core.environments.Environment env;

    public DataHandlingTest() throws Exception{
		StaticTest.InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
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
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testFor1() throws ConfigCompileException {
        String config = "/for = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/for");
        verify(fakePlayer).sendMessage("{0, 1, 2, 3, 4}");
    }

    @Test(expected = ConfigRuntimeException.class, timeout=10000)
    public void testFor3() throws ConfigCompileException {
        String script =
                "   assign(@array, array())"
                + " for('nope', lt(@i, 5), inc(@i),\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);

    }

    @Test(timeout = 10000)
    public void testForeach1() throws ConfigCompileException {
        String config = "/for = >>>\n"
                + " assign(@array, array(1, 2, 3, 4, 5))\n"
                + " assign(@array2, array())"
                + " foreach(@array, @i,\n"
                + "     array_push(@array2, @i)\n"
                + " )\n"
                + " msg(@array2)\n"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/for");
        verify(fakePlayer).sendMessage("{1, 2, 3, 4, 5}");
    }

    @Test(timeout = 10000)
    public void testForeach2() throws ConfigCompileException {
        String config = "/for = >>>\n"
                + " assign(@array, array(1, 2, 3, 4, 5))\n"
                + " assign(@array2, array())"
                + " foreach(@array, @i,\n"
                + "     if(equals(@i, 1), continue(2))"
                + "     array_push(@array2, @i)\n"
                + " )\n"
                + " msg(@array2)\n"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/for");
        verify(fakePlayer).sendMessage("{3, 4, 5}");
    }

    @Test(timeout = 10000)
    public void testForeach3() throws ConfigCompileException {
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
        RunCommand(config, fakePlayer, "/for");
        verify(fakePlayer).sendMessage("{1, 2}");
    }
    
    @Test(timeout = 10000)
    public void testForeachWithArraySlice() throws ConfigCompileException{
        SRun("foreach(1..2, @i, msg(@i))", fakePlayer);
        verify(fakePlayer).sendMessage("1");
        verify(fakePlayer).sendMessage("2");
    }
	
	@Test(timeout = 10000)
	public void testForeachWithKeys1() throws Exception{
		SRun("@array = array(1: 'one', 2: 'two') @string = '' foreach(@array, @key, @value, @string .= (@key.':'.@value.';')) msg(@string)", fakePlayer);
		verify(fakePlayer).sendMessage("1:one;2:two;");
	}
	
	@Test(timeout = 10000)
	public void testForeachWithKeys2() throws Exception{
		SRun("@array = array('one': 1, 'two': 2) @string = '' foreach(@array, @key, @value, @string .= (@key.':'.@value.';')) msg(@string)", fakePlayer);
		verify(fakePlayer).sendMessage("one:1;two:2;");
	}
	
	@Test(timeout = 10000)
	public void testForeachWithKeys3() throws Exception{
		SRun("@array = array('one': 1, 'two': 2)\nforeach(@array, @key, @value){\n\tmsg(@key.':'.@value)\n}", fakePlayer);
		verify(fakePlayer).sendMessage("one:1");
		verify(fakePlayer).sendMessage("two:2");
	}
	
	@Test
	public void testForelse() throws Exception{
		SRun("forelse(assign(@i, 0), @i < 0, @i++, msg('fail'), msg('pass'))", fakePlayer);
		verify(fakePlayer).sendMessage("pass");
		verify(fakePlayer, times(0)).sendMessage("fail");
	}
	
	@Test
	public void testForeachelse() throws Exception{
		SRun("foreachelse(array(), @val, msg('fail'), msg('pass'))", fakePlayer);
		SRun("foreachelse(array(1), @val, msg('pass'), msg('fail'))", fakePlayer);
		SRun("foreachelse(1..2, @val, msg('pass'), msg('fail'))", fakePlayer);
		verify(fakePlayer, times(4)).sendMessage("pass");
		verify(fakePlayer, times(0)).sendMessage("fail");
	}

    @Test(timeout = 10000)
    public void testCallProcIsProc() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(true);
        String config = "/for = >>>\n"
                + " msg(is_proc(_proc))\n"
                + " proc(_proc,"
                + "     msg('hello world')"
                + " )"
                + " msg(is_proc(_proc))"
                + " call_proc(_proc)"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/for");
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
        verify(fakePlayer).sendMessage("hello world");
    }

    /**
     * There is a bug that causes an infinite loop, so we put a 10 second
     * timeout
     *
     * @throws ConfigCompileException
     */
    @Test(timeout = 10000)
    public void testContinue1() throws ConfigCompileException {
        String config = "/continue = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     if(equals(@i, 2), continue(1))\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/continue");
        verify(fakePlayer).sendMessage("{0, 1, 3, 4}");
    }

    @Test(timeout = 10000)
    public void testContinue2() throws ConfigCompileException {
        String config = "/continue = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     if(equals(@i, 2), continue(2))\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/continue");
        verify(fakePlayer).sendMessage("{0, 1, 4}");
    }

    @Test(timeout = 10000)
    public void testContinue3() throws ConfigCompileException {
        String config = "/continue = >>>\n"
                + " assign(@array, array())"
                + " for(assign(@i, 0), lt(@i, 5), inc(@i),\n"
                + "     if(equals(@i, 2), continue(3))\n"
                + "     array_push(@array, @i)\n"
                + " )\n"
                + " msg(@array)\n"
                + "<<<\n";
        RunCommand(config, fakePlayer, "/continue");
        verify(fakePlayer).sendMessage("{0, 1}");
    }

    @Test(timeout = 10000)
    public void testBreak1() throws ConfigCompileException {
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
        RunCommand(config, fakePlayer, "/break");
        verify(fakePlayer).sendMessage("{j:0, j:1, i:0, j:0, j:1, i:1}");
    }

    @Test(timeout = 10000)
    public void testBreak2() throws ConfigCompileException {
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
        RunCommand(config, fakePlayer, "/break");
        verify(fakePlayer).sendMessage("{j:0, j:1}");
    }

    @Test(timeout = 10000)
    public void testInclude() throws ConfigCompileException, IOException {
        String script =
                "include('unit_test_inc.ms')";
        //Create the test file
        File test = new File("unit_test_inc.ms");
        FileUtil.write("msg('hello')", test);
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, new File("./script.txt"), true)), env, null, null);
        verify(fakePlayer).sendMessage("hello");
        //delete the test file
        test.delete();
		test.deleteOnExit();
    }

    @Test(timeout = 10000)
    public void testExportImportIVariable() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(true);
        String script1 =
                "assign(@var, 10)"
                + "export(@var)";
        SRun(script1, null);
        SRun("import(@var) msg(@var)", fakePlayer);
        verify(fakePlayer).sendMessage("10");
    }

    @Test(timeout = 10000)
    public void testExportImportStringValue1() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("export('hi', 20)", fakePlayer);
        SRun("msg(import('hi'))", fakePlayer);
        verify(fakePlayer).sendMessage("20");
    }    
    
    @Test
    public void testExportImportStringValue2() throws ConfigCompileException{
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@test, array(1, 2, 3))"
                + "export('myarray', @test)"             
                + "msg(@newtest)", fakePlayer);
        SRun("assign(@newtest, import('myarray')) msg(@newtest)", fakePlayer);
        verify(fakePlayer).sendMessage("{1, 2, 3}");
    }
    
    @Test
    public void testExportImportWithProcs1() throws ConfigCompileException{
        SRun("proc(_derping," +
                "   msg(import('borked'))"+
                "   assign(@var, import('borked'))" +
                "   assign(@var, array('Am', 'I', 'borked?'))" +
                "   export('borked', @var)" +
                "   msg(import('borked'))" +
                ")\n" +
                "_derping()\n"+
                "_derping()", fakePlayer);
        verify(fakePlayer).sendMessage("null");
        verify(fakePlayer, times(3)).sendMessage("{Am, I, borked?}");
    }
    
    @Test
    public void testExportImportWithProcs2() throws ConfigCompileException{
        SRun("assign(@array, array(1, 2))"
                + "export('myarray', @array)", fakePlayer);
        SRun("proc(_get, return(import('myarray')))"
                + "msg(_get())", fakePlayer);       
        verify(fakePlayer).sendMessage("{1, 2}");
    }

    @Test(timeout = 10000)
    public void testIsBoolean() throws ConfigCompileException {
        SRun("msg(is_boolean(1)) msg(is_boolean(true))", fakePlayer);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }

    @Test(timeout = 10000)
    public void testIsInteger() throws ConfigCompileException {
        SRun("msg(is_integer(5.0)) msg(is_integer('s')) msg(is_integer(5))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }

    @Test(timeout = 10000)
    public void testIsDouble() throws ConfigCompileException {
        SRun("msg(is_double(5)) msg(is_double('5.0')) msg(is_double(5.0))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }

    @Test(timeout = 10000)
    public void testIsNull() throws ConfigCompileException {
        SRun("msg(is_null('null')) msg(is_null(null))", fakePlayer);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer).sendMessage("true");
    }

    @Test(timeout = 10000)
    public void testIsNumeric() throws ConfigCompileException {
        SRun("msg(is_numeric('s')) "
		+ " msg(is_numeric(null))"
		+ " msg(is_numeric(true))"
		+ " msg(is_numeric(2))"
                + " msg(is_numeric(2.0))", fakePlayer);
        verify(fakePlayer, times(1)).sendMessage("false");
        verify(fakePlayer, times(4)).sendMessage("true");
    }

    @Test(timeout = 10000)
    public void testIsIntegral() throws ConfigCompileException {
        SRun("msg(is_integral(5.5)) msg(is_integral(5)) msg(is_integral(4.0))", fakePlayer);
        verify(fakePlayer).sendMessage("false");
        verify(fakePlayer, times(2)).sendMessage("true");
    }

    @Test(timeout = 10000)
    public void testDoubleCastToInteger() throws ConfigCompileException {
        SRun("msg(integer(4.5))", fakePlayer);
        verify(fakePlayer).sendMessage("4");
    }

    @Test(timeout = 10000)
    public void testClosure1() throws ConfigCompileException {
        SRun("assign(@go, closure(console( 'Hello World' ))) msg(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("console('Hello World')");
    }

    @Test(timeout = 10000)
    public void testClosure2() throws ConfigCompileException {
        SRun("assign(@go, closure(msg('Hello World')))", fakePlayer);
        verify(fakePlayer, times(0)).sendMessage("Hello World");
    }

    @Test(timeout = 10000)
    public void testClosure3() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@go, closure(msg('Hello' 'World')))\n"
                + "execute(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }

    @Test(timeout = 10000)
    public void testClosure4() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@hw, 'Hello World')\n"
                + "assign(@go, closure(msg(@hw)))\n"
                + "execute(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }

    @Test(timeout = 10000)
    public void testClosure5() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@hw, 'Nope')\n"
                + "assign(@go, closure(@hw, msg(@hw)))\n"
                + "execute('Hello World', @go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }

    @Test(timeout = 10000)
    public void testClosure6() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@hw, 'Hello World')\n"
                + "assign(@go, closure(msg(@hw)))\n"
                + "execute('Nope', @go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }

    @Test(timeout = 10000)
    public void testClosure7() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
        SRun("assign(@go, closure(assign(@hw, 'Hello World'), msg(@hw)))\n"
                + "execute(@go)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World");
    }

    @Test(timeout = 10000)
    public void testClosure8() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(true);
        SRun("execute(Hello, World, closure(msg(@arguments)))", fakePlayer);
        verify(fakePlayer).sendMessage("{Hello, World}");
    }

    @Test(timeout = 10000)
    public void testClosure9() throws ConfigCompileException {
        when(fakePlayer.isOp()).thenReturn(true);
        SRun("assign(@a, closure(@array, assign(@array[0], 'Hello World')))\n"
                + "assign(@value, array())\n"
                + "execute(@value, @a)\n"
                + "msg(@value)", fakePlayer);
        verify(fakePlayer).sendMessage("{Hello World}");
    }
    
    @Test(timeout=10000)
    public void testWhile() throws ConfigCompileException{
        SRun("assign(@i, 2) while(@i > 0, @i-- msg('hi'))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("hi");
    }
    
    @Test(timeout=10000)
    public void testDoWhile() throws ConfigCompileException{
        SRun("assign(@i, 2) dowhile(@i-- msg('hi'), @i > 0)", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("hi");        
    }
	
	@Test
	public void testToRadix() throws Exception {
		assertEquals("f", SRun("to_radix(15, 16)", null));
		assertEquals("1111", SRun("to_radix(15, 2)", null));
	}
	
	@Test
	public void testParseInt() throws Exception {
		assertEquals("15", SRun("parse_int('F', 16)", null));
		assertEquals("15", SRun("parse_int('1111', 2)", null));
	}
	
	@Test
	public void testClosureReturnsFromExecute() throws Exception {
		assertEquals("3", SRun("execute(closure(return(3)))", fakePlayer));
	}
}
