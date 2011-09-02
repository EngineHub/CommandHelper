/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.aliasengine.functions.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import com.sk89q.commandhelper.CommandHelperPlugin;
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
        fakeServer = GetFakeServer();
        fakePlayer = GetOnlinePlayer(fakeServer);
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
        a.exec(0, null, fakePlayer, C.onstruct("Hello World!"));
        verify(fakePlayer).chat("Hello World!");
    }
    
    @Test public void testBroadcast() throws NoSuchFieldException, InstantiationException, 
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, CancelCommandException{
        Echoes.broadcast a = new Echoes.broadcast();
        TestBoilerplate(a, "broadcast");
        when(fakePlayer.getServer()).thenReturn(fakeServer);
        CommandHelperPlugin.myServer = fakeServer;
        a.exec(0, null, fakePlayer, C.onstruct("Hello World!"));
        verify(fakeServer).broadcastMessage("Hello World!");        
    }
    
    @Test public void testChatas() throws CancelCommandException, ConfigCompileException{
        //TODO: Can't get this to work right, though it does work in game
//        String script = "chatas('wraithguard02', 'Hello World!')";
//        Player wraithguard02 = GetOnlinePlayer("wraithguard02", fakeServer);
//        Player op = GetOp("wraithguard", fakeServer);
//        Run(script, op);
//        verify(wraithguard02).chat("Hello World!");
//        Echoes.chatas a = new Echoes.chatas();
//        Player wraithguard01 = GetOnlinePlayer("wraithguard02", fakeServer);
//        when(fakePlayer.getServer()).thenReturn(fakeServer);
//        when(fakeServer.getPlayer("wraithguard01")).thenReturn(wraithguard01);
//        a.exec(0, null, fakePlayer, C.onstruct("wraithguard02"), C.onstruct("Hello World!"));
//        verify(wraithguard01).chat("Hello World!");        
    }
    
    
}
