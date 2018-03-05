package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.GetFakeConsoleCommandSender;
import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.GetOp;
import static com.laytonsmith.testing.StaticTest.GetWorld;
import static com.laytonsmith.testing.StaticTest.Run;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class PlayerManangementTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;

	public PlayerManangementTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		//mockStatic(StaticLayer.class);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		fakeServer = GetFakeServer();
		fakePlayer = GetOp("player", fakeServer);
		StaticTest.InstallFakeConvertor(fakePlayer);
		when(fakePlayer.getServer()).thenReturn(fakeServer);
		CommandHelperPlugin.myServer = fakeServer;
		String name = fakePlayer.getName();
		when(fakeServer.getPlayer(name)).thenReturn(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testPlayer() throws Exception {
		String script = "player()";
		assertEquals(fakePlayer.getName(), SRun(script, fakePlayer));
		assertEquals("null", SRun(script, null));
	}

	@Test(timeout = 10000)
	public void testPlayer2() throws Exception {
		String script = "player()";
		MCConsoleCommandSender c = GetFakeConsoleCommandSender();
		assertEquals("~console", SRun(script, c));
	}

	@Test(timeout = 10000)
	public void testPlayer3() throws Exception {
		MCCommandSender c = GetFakeConsoleCommandSender();
		assertEquals("~console", SRun("player()", c));
	}

//    @Test(timeout = 10000)
//    public void testAllPlayers() throws Exception {
//        String script = "all_players()";
//        String done = SRun(script, fakePlayer);
//        //This output is too long to test with msg()
//        assertEquals("{player1, player2, player3, player}", done);
//    }
	@Test
	public void testPloc() throws Exception, Exception {
		String script = "msg(ploc())";
		BukkitMCWorld w = GetWorld("world");
		MCLocation loc = StaticLayer.GetLocation(w, 0, 1, 0);
		when(fakePlayer.getLocation()).thenReturn(loc);
		when(fakePlayer.getWorld()).thenReturn(w);
		SRun(script, fakePlayer);
		verify(fakePlayer).sendMessage("{0: 0.0, 1: 1.0, 2: 0.0, 3: world, 4: 0.0, 5: 0.0, pitch: 0.0, world: world, x: 0.0, y: 1.0, yaw: 0.0, z: 0.0}");
	}

	public void testSetPloc() throws Exception, Exception {
		MCWorld w = GetWorld("world");
		CommandHelperPlugin.myServer = fakeServer;
		String name = fakePlayer.getName();
		when(fakeServer.getPlayer(name)).thenReturn(fakePlayer);
		when(fakePlayer.getWorld()).thenReturn(w);
		MCLocation loc = StaticTest.GetFakeLocation(w, 0, 0, 0);
		when(fakePlayer.getLocation()).thenReturn(loc);

		Run("set_ploc(1, 1, 1)", fakePlayer);
		//when(StaticLayer.GetLocation(w, 1, 2, 1)).thenReturn(loc);
		MCLocation loc1 = StaticTest.GetFakeLocation(w, 1, 2, 1);
		assertEquals(fakePlayer.getLocation().getX(), loc1.getX(), 0.00000000000001);//verify(fakePlayer).teleport(loc1);

		Run("set_ploc(array(2, 2, 2))", fakePlayer);
		verify(fakePlayer).teleport(StaticLayer.GetLocation(w, 2, 3, 2, 0, 0));

		Run("set_ploc('" + fakePlayer.getName() + "', 3, 3, 3)", fakePlayer);
		verify(fakePlayer).teleport(StaticLayer.GetLocation(w, 3, 4, 3, 0, 0));

		Run("set_ploc('" + fakePlayer.getName() + "', array(4, 4, 4))", fakePlayer);
		verify(fakePlayer).teleport(StaticLayer.GetLocation(w, 4, 5, 4, 0, 0));
	}

	@Test(timeout = 10000)
	public void testPcursor() throws Exception, Exception {
		MCBlock b = mock(MCBlock.class);
		CommandHelperPlugin.myServer = fakeServer;
		when(fakeServer.getPlayer(fakePlayer.getName())).thenReturn(fakePlayer);
		when(fakePlayer.getTargetBlock((HashSet) eq(null), anyInt())).thenReturn(b);
		MCWorld w = mock(MCWorld.class);
		MCLocation loc = StaticTest.GetFakeLocation(w, 0, 0, 0);
		when(b.getLocation()).thenReturn(loc);
		when(b.getWorld()).thenReturn(w);
		Run("pcursor()", fakePlayer);
		verify(fakePlayer, times(1)).getTargetBlock((HashSet) eq(null), anyInt());
	}

	@Test(timeout = 10000)
	public void testKill() throws Exception {
		Run("pkill()", fakePlayer);
		Run("pkill('" + fakePlayer.getName() + "')", fakePlayer);
		verify(fakePlayer, times(2)).kill();
	}

	//@Test(timeout=10000)
	public void testPgroup() throws Exception {
		Run("", fakePlayer);
		Run("", fakePlayer);
	}

	@Test
	public void testPlayerNotProvided() throws Exception {
		assertEquals("success", SRun("try(pkill(), assign(@success, 'success')) @success", null));
	}

	@Test
	public void testPlayerFromConsole() throws Exception {
		MCConsoleCommandSender fakeConsole = mock(MCConsoleCommandSender.class);
		when(fakeConsole.getName()).thenReturn("CONSOLE");
		assertEquals("~console", SRun("player()", fakeConsole));
	}

//    //@Test(timeout=10000)
//    public void testPinfo(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testPworld(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testKick(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testSetDisplayName(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testResetDisplayName(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testPFacing(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testPinv(){
//
//    }
//
//    //@Test(timeout=10000)
//    public void testSetPinv(){
//
//    }
}
