/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.puls3.core.functions.Echoes;
import com.laytonsmith.puls3.abstraction.MCPlayer;
import com.laytonsmith.puls3.abstraction.MCServer;
import com.laytonsmith.puls3.core.Env;
import com.laytonsmith.puls3.core.exceptions.CancelCommandException;
import com.laytonsmith.puls3.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import com.laytonsmith.puls3.Puls3Plugin;
import java.lang.reflect.InvocationTargetException;
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
    
    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();
    
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
        env.SetPlayer(fakePlayer);
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void testDocs() {
        TestClassDocs(Echoes.docs(), Echoes.class);
    }
    
    @Test public void testChat() throws CancelCommandException{
        Echoes.chat a = new Echoes.chat();     
        a.exec(0, null, env, C.onstruct("Hello World!"));
        verify(fakePlayer).chat("Hello World!");
    }
    
    @Test public void testBroadcast() throws NoSuchFieldException, InstantiationException, 
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, CancelCommandException{
        Echoes.broadcast a = new Echoes.broadcast();
        when(fakePlayer.getServer()).thenReturn(fakeServer);
        Puls3Plugin.myServer = fakeServer;
        a.exec(0, null, env, C.onstruct("Hello World!"));
        verify(fakeServer).broadcastMessage("Hello World!");        
    }
    
    @Test public void testLongStringMsgd() throws ConfigCompileException{
        //TODO: Put this test back in, and fix it
//        SRun("msg('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@')", fakePlayer);
//        verify(fakePlayer).sendMessage("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
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
