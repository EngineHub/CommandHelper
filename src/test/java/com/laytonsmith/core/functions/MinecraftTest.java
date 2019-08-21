package com.laytonsmith.core.functions;

import static org.junit.Assert.assertEquals;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.enums.MCVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class MinecraftTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;

	public MinecraftTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		fakePlayer = StaticTest.GetOnlinePlayer();
		fakeServer = StaticTest.GetFakeServer();
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testIsTameable() throws ConfigCompileException {
		//Y U NO COOPERATE, TEST FRAMEWORK?
//		MCWorld fakeWorld = mock(MCWorld.class);
//		MCLocation fakeLocation = mock(MCLocation.class);
//		when(fakePlayer.getWorld()).thenReturn(fakeWorld);
//		when(fakePlayer.getLocation()).thenReturn(fakeLocation);
//		when(fakeLocation.getWorld()).thenReturn(fakeWorld);
//
//		when(fakeWorld.spawnMob(eq("ocelot"), anyString(), anyInt(), (MCLocation)any(), (Target)any()))
//				.thenReturn(new CArray(Target.UNKNOWN, new CInt("10", Target.UNKNOWN)));
//		mockStatic(Static.class);
//		MCTameable fakeTameable = mock(MCTameable.class);
//		when(Static.getEntity(10)).thenReturn(fakeTameable);
//		Preferences fakePrefs = mock(Preferences.class);
//		when(Static.getPreferences()).thenReturn(fakePrefs);
//		when(fakePrefs.getPreference("debug-mode")).thenReturn(false);
//
//		SRun("is_tameable(spawn_mob('ocelot')[0])", fakePlayer);
//		verify(fakePlayer).sendMessage("true");

	}

	@Test
	public void testGetMCVersion() {
		assertEquals(MCVersion.match(new String[]{"1", "8"}), MCVersion.MC1_8);
		assertEquals(MCVersion.match(new String[]{"1", "8", "0"}), MCVersion.MC1_8);
		assertEquals(MCVersion.match(new String[]{"1", "8", "6"}), MCVersion.MC1_8_6);
		assertEquals(MCVersion.match(new String[]{"1", "8", "8"}), MCVersion.MC1_8_X);
		assertEquals(MCVersion.match(new String[]{"2", "10", "20"}), MCVersion.MC2_X);
		assertEquals(MCVersion.match(new String[]{"3", "1", "4"}), MCVersion.MCX_X);
	}
}
