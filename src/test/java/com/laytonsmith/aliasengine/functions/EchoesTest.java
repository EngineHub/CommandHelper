/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.CancelCommandException;
import com.laytonsmith.testing.C;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
        TestClassDocs(Echoes.docs(), Echoes.class);
    }
    
    @Test public void testChat() throws CancelCommandException{
        Echoes.chat a = new Echoes.chat();     
        TestBoilerplate(a, "chat");
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
        TestBoilerplate(a, "chatas");
        Player wraithguard01 = mock(Player.class);
        when(fakePlayer.getServer()).thenReturn(fakeServer);
        when(fakeServer.getPlayer("wraithguard01")).thenReturn(wraithguard01);
        a.exec(0, fakePlayer, C.onstruct("wraithguard01"), C.onstruct("Hello World!"));
        verify(wraithguard01).chat("Hello World!");        
    }
    
    
}
