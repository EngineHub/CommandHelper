package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.RunCommand;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class DataHandlingTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;
	static Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs = com.laytonsmith.core.environments.Environment.getDefaultEnvClasses();

	public DataHandlingTest() throws Exception {
		StaticTest.InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
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
	public void testCallProcIsProc() throws Exception {
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

	@Test(timeout = 10000)
	public void testInclude() throws Exception, IOException {
		String script
				= "include('unit_test_inc.ms')";
		//Create the test file
		File test = new File("unit_test_inc.ms");
		FileUtil.write("msg('hello')", test);
		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, new File("./script.txt"), true), null, envs), env, null, null, null);
		verify(fakePlayer).sendMessage("hello");
		//delete the test file
		test.delete();
		test.deleteOnExit();
	}

	// This feature has been deprecated, and now removed.
//	@Test(timeout = 10000)
//	public void testExportImportIVariable() throws Exception {
//		when(fakePlayer.isOp()).thenReturn(true);
//		String script1 =
//				"assign(@var, 10)"
//				+ "export(@var)";
//		SRun(script1, null);
//		SRun("import(@var) msg(@var)", fakePlayer);
//		verify(fakePlayer).sendMessage("10");
//	}
	@Test(timeout = 10000)
	public void testExportImportStringValue1() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("export('hi', 20)", fakePlayer);
		SRun("msg(import('hi'))", fakePlayer);
		verify(fakePlayer).sendMessage("20");
	}

	@Test
	public void testExportImportStringValue2() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@test, array(1, 2, 3))"
				+ "export('myarray', @test)"
				+ "msg(@newtest)", fakePlayer);
		SRun("assign(@newtest, import('myarray')) msg(@newtest)", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2, 3}");
	}

	@Test
	public void testExportImportWithProcs1() throws Exception {
		SRun("proc(_derping,"
				+ "   msg(import('borked'))"
				+ "   assign(@var, import('borked'))"
				+ "   assign(@var, array('Am', 'I', 'borked?'))"
				+ "   export('borked', @var)"
				+ "   msg(import('borked'))"
				+ ")\n"
				+ "_derping()\n"
				+ "_derping()", fakePlayer);
		verify(fakePlayer).sendMessage("null");
		verify(fakePlayer, times(3)).sendMessage("{Am, I, borked?}");
	}

	@Test
	public void testExportImportWithProcs2() throws Exception {
		SRun("assign(@array, array(1, 2))"
				+ "export('myarray', @array)", fakePlayer);
		SRun("proc(_get, return(import('myarray')))"
				+ "msg(_get())", fakePlayer);
		verify(fakePlayer).sendMessage("{1, 2}");
	}

	@Test
	public void testExportImportArrayNameSpace() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@key, array('custom', 'key1'))"
				+ "assign(@value, 'key1Value')"
				+ "export(@key, @value)"
				+ "msg(import('custom.key1'))", fakePlayer);
		verify(fakePlayer).sendMessage("key1Value");
		SRun("assign(@key, array('custom', 'key2'))"
				+ "assign(@value, 'key2Value')"
				+ "export('custom.key2', @value)"
				+ "msg(import(@key))", fakePlayer);
		verify(fakePlayer).sendMessage("key2Value");
	}

	@Test(timeout = 10000)
	public void testIsBoolean() throws Exception {
		SRun("msg(is_boolean(1)) msg(is_boolean(true))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
		verify(fakePlayer).sendMessage("true");
	}

	@Test(timeout = 10000)
	public void testIsInteger() throws Exception {
		SRun("msg(is_integer(5.0)) msg(is_integer('s')) msg(is_integer(5))", fakePlayer);
		verify(fakePlayer, times(2)).sendMessage("false");
		verify(fakePlayer).sendMessage("true");
	}

	@Test(timeout = 10000)
	public void testIsDouble() throws Exception {
		SRun("msg(is_double(5)) msg(is_double('5.0')) msg(is_double('5'.0)) msg(is_double(5.'0'))"
				+ "msg(is_double(5.0)) msg(is_double(5 . 0))", fakePlayer);
		verify(fakePlayer, times(4)).sendMessage("false");
		verify(fakePlayer, times(2)).sendMessage("true");
	}

	@Test(timeout = 10000)
	public void testIsNull() throws Exception {
		SRun("msg(is_null('null')) msg(is_null(null))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
		verify(fakePlayer).sendMessage("true");
	}

	@Test(timeout = 10000)
	public void testIsNumeric() throws Exception {
		SRun("msg(is_numeric('s')) "
				+ " msg(is_numeric(null))"
				+ " msg(is_numeric(true))"
				+ " msg(is_numeric(2))"
				+ " msg(is_numeric(2.0))", fakePlayer);
		verify(fakePlayer, times(1)).sendMessage("false");
		verify(fakePlayer, times(4)).sendMessage("true");
	}

	@Test(timeout = 10000)
	public void testIsIntegral() throws Exception {
		SRun("msg(is_integral(5.5)) msg(is_integral(5)) msg(is_integral(4.0))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
		verify(fakePlayer, times(2)).sendMessage("true");
	}

	@Test(timeout = 10000)
	public void testDoubleCastToInteger() throws Exception {
		SRun("msg(integer(4.5))", fakePlayer);
		verify(fakePlayer).sendMessage("4");
	}

	@Test(timeout = 10000)
	public void testClosure1() throws Exception {
		SRun("assign(@go, closure(console( 'Hello World' ))) msg(@go)", fakePlayer);
		verify(fakePlayer).sendMessage("console('Hello World')");
	}

	@Test(timeout = 10000)
	public void testClosure2() throws Exception {
		SRun("assign(@go, closure(msg('Hello World')))", fakePlayer);
		verify(fakePlayer, times(0)).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure3() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@go, closure(msg('Hello' 'World')))\n"
				+ "execute(@go)", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure4() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@hw, 'Hello World')\n"
				+ "assign(@go, closure(msg(@hw)))\n"
				+ "execute(@go)", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure5() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@hw, 'Nope')\n"
				+ "assign(@go, closure(@hw, msg(@hw)))\n"
				+ "execute('Hello World', @go)", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure6() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@hw, 'Hello World')\n"
				+ "assign(@go, closure(msg(@hw)))\n"
				+ "execute('Nope', @go)", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure7() throws Exception {
		when(fakePlayer.isOp()).thenReturn(Boolean.TRUE);
		SRun("assign(@go, closure(assign(@hw, 'Hello World'), msg(@hw)))\n"
				+ "execute(@go)", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure8() throws Exception {
		when(fakePlayer.isOp()).thenReturn(true);
		SRun("execute(Hello, World, closure(msg(@arguments)))", fakePlayer);
		verify(fakePlayer).sendMessage("{Hello, World}");
	}

	@Test(timeout = 10000)
	public void testClosure9() throws Exception {
		when(fakePlayer.isOp()).thenReturn(true);
		SRun("assign(@a, closure(@array, assign(@array[0], 'Hello World')))\n"
				+ "assign(@value, array())\n"
				+ "execute(@value, @a)\n"
				+ "msg(@value)", fakePlayer);
		verify(fakePlayer).sendMessage("{Hello World}");
	}

	@Test(timeout = 10000)
	public void testClosure10() throws Exception {
		MCPlayer fakePlayer2 = StaticTest.GetOnlinePlayer("Player02", fakeServer);
		when(fakeServer.getPlayer("Player02")).thenReturn(fakePlayer2);
		SRun("@c = closure(){msg(reflect_pull('label'))};"
				+ "executeas('Player02', 'newlabel', @c);"
				+ "execute(@c);", fakePlayer);
		verify(fakePlayer2).sendMessage("newlabel");
	}

	@Test(timeout = 10000)
	public void testClosure11() throws Exception {
		SRun("@c = closure(@msg){msg(@msg)};\n"
				+ "@c('Hello World');", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000)
	public void testClosure12() throws Exception {
		SRun("@c = closure(@msg = 'Hello World'){msg(@msg)};\n"
				+ "@c();", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World");
	}

	@Test(timeout = 10000, expected = CRECastException.class)
	public void testClosure13() throws Exception {
		SRun("@s = 'string';\n"
				+ "@s();", fakePlayer);
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

	@Test
	public void testEmptyClosureFunction() throws Exception {
		// This should not throw an exception
		SRun("closure()", null);
	}

	@Test
	public void testAssignmentTypes1() throws Exception {
		SRun("string @ivar = 'value'", null);
	}

	@Test
	public void testAssignmentTypes2() throws Exception {
		try {
			SRun("array @ivar = 'value'", null);
			fail("Excepted a CastException because string is not array");
		} catch (CRECastException ex) {
			// Test passed.
		}
	}

	@Test
	public void testAssignmentTypes3() throws Exception {
		try {
			SRun("void @ivar = 'value'", null);
			fail("Expected a compile exception because IVariable cannot be assigned to void");
		} catch (ConfigCompileException ex) {
			// Test passed.
		}
	}
}
