/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.MScriptComplete;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.sk89q.commandhelper.CommandHelperPlugin;
import org.bukkit.Location;
import org.junit.Test;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;
import static org.junit.Assert.*;

/**
 *
 * @author Layton
 */
public class PlayerManangementTest {

    Server fakeServer;
    Player fakePlayer;
    
    public PlayerManangementTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fakeServer = GetFakeServer();
        fakePlayer = GetOp("wraithguard01", fakeServer);
        when(fakePlayer.getServer()).thenReturn(fakeServer);
        CommandHelperPlugin.myServer = fakeServer;
        when(fakeServer.getPlayer(fakePlayer.getName())).thenReturn(fakePlayer);
    }
    
    @After
    public void tearDown() {
    }

    @Test public void testPlayer() throws ConfigCompileException{
        String script = "player()";
        assertEquals(fakePlayer.getName(), SRun(script, fakePlayer));
        assertEquals("null", SRun(script, null));
    }
    
    @Test public void testPlayer2() throws ConfigCompileException{
        String script = "msg(player())";
        ConsoleCommandSender c = GetFakeConsoleCommandSender();
        Run(script, c);
        verify(c).sendMessage("~console");
    }
    
    @Test 
    public void testPlayer3() throws ConfigCompileException{
        CommandSender c = GetFakeConsoleCommandSender();
        assertEquals("~console", SRun("player()", c));
    }
    
    @Test public void testAllPlayers() throws ConfigCompileException{
        String script = "all_players()";
        String done = SRun(script, fakePlayer);
        //This output is too long to test with msg()        
        assertEquals("{wraithguard01, wraithguard02, wraithguard03}", done);
    }
    
    @Test public void testPloc() throws ConfigCompileException{
        String script = "ploc()";
        World w = GetWorld("world");
        when(fakePlayer.getLocation()).thenReturn(new Location(w, 0, 1, 0));
        final StringBuilder done = new StringBuilder();
        Run(script, fakePlayer, new MScriptComplete() {

            public void done(String output) {
                done.append(output);
            }
        });
        assertEquals("{0.0, 0.0, 0.0}", done.toString());
    }
    
    @Test public void testSetPloc() throws ConfigCompileException{
        World w = GetWorld("world");
        CommandHelperPlugin.myServer = fakeServer;
        when(fakeServer.getPlayer(fakePlayer.getName())).thenReturn(fakePlayer);
        when(fakePlayer.getWorld()).thenReturn(w);
        when(fakePlayer.getLocation()).thenReturn(new Location(w, 0, 0, 0));
        
        Run("set_ploc(1, 1, 1)", fakePlayer);
        verify(fakePlayer).teleport(new Location(w, 1, 2, 1, 0, 0));
        
        Run("set_ploc(array(2, 2, 2))", fakePlayer);
        verify(fakePlayer).teleport(new Location(w, 2, 3, 2, 0, 0));
        
        Run("set_ploc('" + fakePlayer.getName() + "', 3, 3, 3)", fakePlayer);
        verify(fakePlayer).teleport(new Location(w, 3, 4, 3, 0, 0));
        
        Run("set_ploc('" + fakePlayer.getName() + "', array(4, 4, 4))", fakePlayer);
        verify(fakePlayer).teleport(new Location(w, 4, 5, 4, 0, 0));
    }
    
    @Test public void testPcursor() throws ConfigCompileException{  
        Block b = mock(Block.class);
        CommandHelperPlugin.myServer = fakeServer;
        when(fakeServer.getPlayer(fakePlayer.getName())).thenReturn(fakePlayer);
        when(fakePlayer.getTargetBlock(null, 200)).thenReturn(b);
        Run("pcursor()", fakePlayer);
        Run("pcursor('" + fakePlayer.getName() + "')", fakePlayer);
        verify(fakePlayer, times(2)).getTargetBlock(null, 200);
    }
    
    @Test public void testKill() throws ConfigCompileException{        
        Run("kill()", fakePlayer);
        Run("kill('" + fakePlayer.getName() + "')", fakePlayer);
        verify(fakePlayer, times(2)).setHealth(0);
    }
    
    //@Test
    public void testPgroup() throws ConfigCompileException{
        Run("", fakePlayer);
        Run("", fakePlayer);
    }
    
    //@Test
    public void testPinfo(){
        
    }
    
    //@Test
    public void testPworld(){
        
    }
    
    //@Test
    public void testKick(){
        
    }
    
    //@Test
    public void testSetDisplayName(){
        
    }
    
    //@Test
    public void testResetDisplayName(){
        
    }
    
    //@Test
    public void testPFacing(){
        
    }
    
    //@Test
    public void testPinv(){
        
    }
    
    //@Test
    public void testSetPinv(){
        
    }
}
