package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.testing.StaticTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SleepingIgnoredTest {

	private MCPlayer  player;
	private Environment env;

	@Before
	public void setUp() throws Exception {
		player = StaticTest.GetOnlinePlayer();

		env = Static.GenerateStandaloneEnvironment()
				.cloneAndAdd(new CommandHelperEnvironment());
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(player);

		player.setSleepingIgnored(false);          // start from a known state
	}

	@Test
	public void testImplicitAndExplicitForms() throws Exception {

		/* ---------- implicit-player form ---------- */
		assertFalse(player.isSleepingIgnored());
		StaticTest.SRun("set_player_sleeping_ignored(true)", player, env);
		assertTrue(player.isSleepingIgnored());

		/* ---------- explicit-player form ---------- */
		String pLiteral = "p@" + player.getName();          // literal player constant

		StaticTest.SRun("set_player_sleeping_ignored(" + pLiteral + ", false)", player, env);

		String raw = StaticTest.SRun("is_player_sleeping_ignored(" + pLiteral + ")", player, env);
		assertFalse(parseBool(raw));                        // now really off
	}

	/** CommandHelper returns “:true” / “:false”. */
	private static boolean parseBool(String s) {
		return s.trim().replaceFirst("^:", "").equalsIgnoreCase("true");
	}
}
