

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.*;
import java.util.HashSet;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
//import static org.powermock.api.mockito.PowerMockito.*;

/**
 *
 * @author Layton
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
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
        fakePlayer = GetOp("wraithguard01", fakeServer);
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
    public void testPlayer() throws ConfigCompileException {
        String script = "player()";
        assertEquals(fakePlayer.getName(), SRun(script, fakePlayer));
        assertEquals("null", SRun(script, null));
    }

    @Test(timeout = 10000)
    public void testPlayer2() throws ConfigCompileException {
        String script = "player()";
        MCConsoleCommandSender c = GetFakeConsoleCommandSender();
        assertEquals("~console", SRun(script, c));
    }

    @Test(timeout = 10000)
    public void testPlayer3() throws ConfigCompileException {
        MCCommandSender c = GetFakeConsoleCommandSender();
        assertEquals("~console", SRun("player()", c));
    }

    @Test(timeout = 10000)
    public void testAllPlayers() throws ConfigCompileException {
        String script = "all_players()";
        String done = SRun(script, fakePlayer);
        //This output is too long to test with msg()        
        assertEquals("{wraithguard01, wraithguard02, wraithguard03}", done);
    }

    @Test
    public void testPloc() throws ConfigCompileException, Exception {
        String script = "msg(ploc())";
        BukkitMCWorld w = GetWorld("world");
        MCLocation loc = StaticLayer.GetLocation(w, 0, 1, 0);
        when(fakePlayer.getLocation()).thenReturn(loc);
        when(fakePlayer.getWorld()).thenReturn(w);
        SRun(script, fakePlayer);
        verify(fakePlayer).sendMessage("{0: 0.0, 1: 1.0, 2: 0.0, 3: world, 4: 0.0, 5: 0.0, pitch: 0.0, world: world, x: 0.0, y: 1.0, yaw: 0.0, z: 0.0}");
    }

    public void testSetPloc() throws ConfigCompileException, Exception {
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
    public void testPcursor() throws ConfigCompileException, Exception {
        MCBlock b = mock(MCBlock.class);
        CommandHelperPlugin.myServer = fakeServer;
        when(fakeServer.getPlayer(fakePlayer.getName())).thenReturn(fakePlayer);
        when(fakePlayer.getTargetBlock((HashSet) eq(null), anyInt(), eq(false))).thenReturn(b);
        MCWorld w = mock(MCWorld.class);
        when(b.getWorld()).thenReturn(w);
        Run("pcursor()", fakePlayer);
        Run("pcursor('" + fakePlayer.getName() + "')", fakePlayer);
        verify(fakePlayer, times(2)).getTargetBlock((HashSet) eq(null), anyInt(), eq(false));
    }

    @Test(timeout = 10000)
    public void testKill() throws ConfigCompileException {
        Run("pkill()", fakePlayer);
        Run("pkill('" + fakePlayer.getName() + "')", fakePlayer);
        verify(fakePlayer, times(2)).kill();
    }

    //@Test(timeout=10000)
    public void testPgroup() throws ConfigCompileException {
        Run("", fakePlayer);
        Run("", fakePlayer);
    }
    
    @Test
    public void testPlayerNotProvided() throws ConfigCompileException{
        assertEquals("success", SRun("try(pkill(), assign(@success, 'success')) @success", null));        
    }
    
    @Test
    public void testPlayerFromConsole() throws ConfigCompileException{
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
