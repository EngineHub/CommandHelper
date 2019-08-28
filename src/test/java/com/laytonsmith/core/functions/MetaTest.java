package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.GetOnlinePlayer;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class MetaTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;
	static Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs
			= com.laytonsmith.core.environments.Environment.getDefaultEnvClasses();
	static {
		envs.add(CommandHelperEnvironment.class);
	}

	public MetaTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		fakePlayer = GetOnlinePlayer();
		fakeServer = GetFakeServer();
		CommandHelperPlugin.myServer = fakeServer;
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testRunas1() throws Exception {
		String script
				= "runas('Player02', '/cmd yay')";
		MCPlayer fakePlayer2 = GetOnlinePlayer("Player02", fakeServer);
		when(fakeServer.getPlayer("Player02")).thenReturn(fakePlayer2);
		when(fakePlayer.isOp()).thenReturn(true);
		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, null, true), null, envs), env, null, null);
		//verify(fakePlayer2).performCommand("cmd yay");
		verify(fakeServer).dispatchCommand(fakePlayer2, "cmd yay");
	}

	@Test
	public void testEval() throws Exception {
		SRun("eval('msg(\\'Hello World!\\')')", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World!");
	}

	@Test
	public void testEval2() throws Exception {
		SRun("assign(@e, 'msg(\\'Hello World!\\')') eval(@e)", fakePlayer);
		verify(fakePlayer).sendMessage("Hello World!");
	}

	@Test
	public void testScriptas() throws Exception {
		String script = "scriptas('Player02', 'newlabel', msg(reflect_pull('label'))); msg(reflect_pull('label'))";
		MCPlayer fakePlayer2 = GetOnlinePlayer("Player02", fakeServer);
		when(fakeServer.getPlayer("Player02")).thenReturn(fakePlayer2);
		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, null, true), null, envs), env, null, null);
		verify(fakePlayer2).sendMessage("newlabel");
		verify(fakePlayer).sendMessage("*");
	}

	@Test
	public void testNameof() throws Exception {
		assertEquals("@var", SRun("nameof(@var)", null));
		assertEquals("$var", SRun("nameof($var)", null));
		try {
			SRun("nameof('string')", null);
			fail();
		} catch (Exception e) {
			// pass
		}
	}
}
