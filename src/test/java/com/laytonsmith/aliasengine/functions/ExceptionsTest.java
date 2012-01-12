/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.functions;

import com.laytonsmith.puls3.abstraction.MCPlayer;
import com.laytonsmith.puls3.abstraction.MCServer;
import com.laytonsmith.puls3.core.Env;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import java.io.File;
import com.laytonsmith.puls3.core.MScriptCompiler;
import com.laytonsmith.puls3.core.exceptions.ConfigCompileException;
import com.laytonsmith.puls3.Puls3Plugin;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.laytonsmith.testing.StaticTest;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class ExceptionsTest {
    
    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();

    public ExceptionsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        Puls3Plugin.perms = mock(PermissionsResolverManager.class);
        Puls3Plugin.myServer = fakeServer;
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
        env.SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    @Test public void testTryCatch1() throws ConfigCompileException{
        String script =
                "try(\n"
                + "ploc('offlineplayer'),\n"
                + "@ex,\n"
                + "msg(@ex[0])\n"
                + "msg(@ex[1])\n"
                + "msg(@ex[2])\n"
                + "msg(@ex[3])\n"
                + ")";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("InsufficientPermissionException");
        verify(fakePlayer).sendMessage("You do not have permission to use the ploc function.");
        verify(fakePlayer).sendMessage("null");
        verify(fakePlayer).sendMessage("2");
    }
    
    @Test public void testTryCatch2() throws ConfigCompileException{
        String script =
                "try(\n"
                + "throw(PlayerOfflineException, This is a message),\n"
                + "@ex,\n"
                + "msg(@ex[0])\n"
                + "msg(@ex[1])\n"
                + "msg(@ex[2])\n"
                + "msg(@ex[3])\n"               
                + ")";
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("PlayerOfflineException");
        verify(fakePlayer).sendMessage("This is a message");
        verify(fakePlayer).sendMessage("null");
        verify(fakePlayer).sendMessage("2");
    }
    @Test public void testTryCatch3() throws ConfigCompileException{
        String script =
                "try(try(\n"
                + "throw(null, This is a message),\n"
                + "@ex,\n"
                + "msg(@ex[0])\n"
                + "msg(@ex[1])\n"
                + "msg(@ex[2])\n"
                + "msg(@ex[3])\n"               
                + "), @ex, msg('2'))";
        try{
            MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, new File("file.txt"))), env, null, null);
            fail("This test was supposed to throw an exception");
        }catch(ConfigRuntimeException e){
            //Pass
        }
        verify(fakePlayer, never()).sendMessage("2");
    }
}
