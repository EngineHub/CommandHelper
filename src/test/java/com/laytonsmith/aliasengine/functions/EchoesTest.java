/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.testing.C;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.Effect;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import com.laytonsmith.testing.StaticTest;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static com.laytonsmith.testing.StaticTest.*;

/**
 *
 * @author Layton
 */
public class EchoesTest {
    
    Server fakeServer;
    Player fakePlayer;
    
    public EchoesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        fakePlayer = mock(Player.class);
        fakeServer = mock(Server.class);
        when(fakePlayer.getServer()).thenReturn(fakeServer);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDocs() {
        System.out.println("docs");
        StaticTest.TestClassDocs(Echoes.docs(), Echoes.class);
    }
    
    @Test public void testChat() throws CancelCommandException{
        Echoes.chat a = new Echoes.chat();        
        a.exec(0, fakePlayer, C.onstruct("Hello World!"));
        verify(fakePlayer).chat("Hello World!");
    }
    
    @Test public void testBroadcast() throws NoSuchFieldException, InstantiationException, 
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, CancelCommandException{
        Echoes.broadcast a = new Echoes.broadcast();
        TestBoilerplate(a, "broadcast");
        a.exec(0, fakePlayer, C.onstruct("Hello World!"));
        verify(fakeServer).broadcastMessage("Hello World!");        
    }
    
    @Test public void testChatas() throws CancelCommandException{
        Echoes.chatas a = new Echoes.chatas();
        Player wraithguard01 = mock(Player.class);
        when(fakeServer.getPlayer("wraithguard01")).thenReturn(wraithguard01);
        a.exec(0, fakePlayer, C.onstruct("wraithguard01"), C.onstruct("Hello World!"));
        verify(wraithguard01).chat("Hello World!");        
    }
    
    
}
