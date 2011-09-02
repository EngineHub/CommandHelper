/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.sk89q.commandhelper.CommandHelperPlugin;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.laytonsmith.aliasengine.MScriptCompiler;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class MetaTest {
    
    static Server fakeServer;
    static Player fakePlayer;

    public MetaTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
        CommandHelperPlugin.myServer = fakeServer;
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test public void testRunas1() throws ConfigCompileException{
        String script = 
                "runas('wraithguard02', '/cmd yay')";
        Player fakePlayer2 = StaticTest.GetOnlinePlayer("wraithguard02", fakeServer);
        when(fakeServer.getPlayer("wraithguard02")).thenReturn(fakePlayer2);
        when(fakePlayer.isOp()).thenReturn(true);
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
        //verify(fakePlayer2).performCommand("cmd yay");
        verify(fakeServer).dispatchCommand(fakePlayer2, "cmd yay");
    }
    //:( I can't get this to work right, because AlwaysOpPlayer is different than
    //fakePlayer, so I can't get my test to activate when the function is called.
//    @Test public void testRunas2() throws ConfigCompileException{
//        final AtomicBoolean bool = new AtomicBoolean(false); 
//        String script = 
//                "runas(~op, '/cmd yay')";
//        when(fakeServer.dispatchCommand(fakePlayer, "cmd yay")).thenAnswer(new Answer<Boolean>(){
//
//            public Boolean answer(InvocationOnMock invocation) throws Throwable {
//                System.out.println("HERE");
//                assertTrue(((Server)invocation.getMock()).getPlayer(fakePlayer.getName()).isOp());
//                bool.set(true);
//                return true;
//            }
//            
//        });
//        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), fakePlayer, null, null);
//        assertTrue(bool.get());
//    }
    
}
