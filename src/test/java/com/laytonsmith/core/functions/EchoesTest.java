package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.functions.Echoes.color;
import com.laytonsmith.testing.C;
import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.GetOnlinePlayer;
import static com.laytonsmith.testing.StaticTest.SRun;
import static com.laytonsmith.testing.StaticTest.TestClassDocs;
import java.lang.reflect.InvocationTargetException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class EchoesTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;

	public EchoesTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		fakeServer = GetFakeServer();
		fakePlayer = GetOnlinePlayer(fakeServer);
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testDocs() {
		TestClassDocs(Echoes.docs(), Echoes.class);
	}

	@Test(timeout = 10000)
	public void testChat() throws CancelCommandException {
		Echoes.chat a = new Echoes.chat();
		a.exec(Target.UNKNOWN, env, C.onstruct("Hello World!"));
		verify(fakePlayer).chat("Hello World!");
	}

	@Test(timeout = 10000)
	public void testBroadcast() throws NoSuchFieldException, InstantiationException,
			IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, CancelCommandException {
		Echoes.broadcast a = new Echoes.broadcast();
		when(fakePlayer.getServer()).thenReturn(fakeServer);
		CommandHelperPlugin.myServer = fakeServer;
		a.exec(Target.UNKNOWN, env, C.onstruct("Hello World!"));
		verify(fakeServer).broadcastMessage("Hello World!");
	}

	@Test(timeout = 10000)
	public void testLongStringMsgd1() throws Exception {
		SRun("msg('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@')", fakePlayer);
		verify(fakePlayer).sendMessage("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}

	@Test(timeout = 10000)
	public void testChatas() throws CancelCommandException, ConfigCompileException {
		//TODO: Can't get this to work right, though it does work in game
//		String script = "chatas('Player02', 'Hello World!')";
//		Player Player02 = GetOnlinePlayer("Player02", fakeServer);
//		Player op = GetOp("Player", fakeServer);
//		Run(script, op);
//		verify(Player02).chat("Hello World!");
//		Echoes.chatas a = new Echoes.chatas();
//		Player Player01 = GetOnlinePlayer("Player02", fakeServer);
//		when(fakePlayer.getServer()).thenReturn(fakeServer);
//		when(fakeServer.getPlayer("Player01")).thenReturn(Player01);
//		a.exec(Target.UNKNOWN, fakePlayer, C.onstruct("Player02"), C.onstruct("Hello World!"));
//		verify(Player01).chat("Hello World!");
	}

	@Test
	public void testIndentation() throws Exception {
		SRun("msg('yay\n yay\n  yay\n   yay')", fakePlayer);
		verify(fakePlayer).sendMessage("yay\n yay\n  yay\n   yay");
	}

	@Test
	public void testColor() throws Exception {
		assertEquals(String.format("\u00A7%s", "f"), SRun("color(white)", fakePlayer));
		assertEquals(String.format("\u00A7%s", "6"), SRun("color(gold)", fakePlayer));
		assertEquals(String.format("\u00A7%s", "k"), SRun("color(random)", fakePlayer));
		assertEquals(String.format("\u00A7%s", "m"), SRun("color(strike)", fakePlayer));
		assertEquals(String.format("\u00A7%s", "a"), SRun("color(a)", fakePlayer));
	}

	private static final String LOWERCASE_A =
			new color().exec(Target.UNKNOWN, null, new CString("a", Target.UNKNOWN)).val();

	@Test
	public void testColorize1() throws Exception {
		assertEquals(LOWERCASE_A + "Hi", SRun("colorize('&aHi')", fakePlayer));
	}

	@Test
	public void testColorize2() throws Exception {
		assertEquals("&aHi", SRun("colorize('&&aHi')", fakePlayer));
	}

	@Test
	public void testColorize3() throws Exception {
		assertEquals("&", SRun("colorize('&&')", fakePlayer));
	}

	@Test
	public void testColorize4() throws Exception {
		assertEquals("&&" + LOWERCASE_A + "Hi", SRun("colorize('&&&&&aHi')", fakePlayer));
	}

	@Test
	public void testColorize5() throws Exception {
		assertEquals("&&", SRun("colorize('&&&&', '&&')", fakePlayer));
	}

	@Test
	public void testColorize6() throws Exception {
		assertEquals("&&" + LOWERCASE_A + "Hi", SRun("colorize('&&&&&&aHi', '&&')", fakePlayer));
	}
}
