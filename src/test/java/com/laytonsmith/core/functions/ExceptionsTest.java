/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import com.sk89q.wepif.PermissionsResolverManager;
import java.io.File;
import static org.junit.Assert.fail;
import org.junit.*;
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
        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
        CommandHelperPlugin.myServer = fakeServer;
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
        env.SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testTryCatch1() throws ConfigCompileException {
        String script =
                "try(\n"
                + "ploc('offlineplayer'),\n"
                + "@ex,\n"
                + "msg(@ex[0])\n"
                + "msg(@ex[1])\n"
                + "msg(@ex[2])\n"
                + "msg(@ex[3])\n"
                + ")";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("PlayerOfflineException");
        verify(fakePlayer).sendMessage("The specified player (offlineplayer) is not online");
        verify(fakePlayer).sendMessage("null");
        verify(fakePlayer).sendMessage("2");
    }

    @Test(timeout = 10000)
    public void testTryCatch2() throws ConfigCompileException {
        String script =
                "try(\n"
                + "throw(PlayerOfflineException, This is a message),\n"
                + "@ex,\n"
                + "msg(@ex[0])\n"
                + "msg(@ex[1])\n"
                + "msg(@ex[2])\n"
                + "msg(@ex[3])\n"
                + ")";
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), env, null, null);
        verify(fakePlayer).sendMessage("PlayerOfflineException");
        verify(fakePlayer).sendMessage("This is a message");
        verify(fakePlayer).sendMessage("null");
        verify(fakePlayer).sendMessage("2");
    }

    @Test(timeout = 10000)
    public void testTryCatch3() throws ConfigCompileException {
        String script =
                "try(try(\n"
                + "throw(null, This is a message),\n"
                + "@ex,\n"
                + "msg(@ex[0])\n"
                + "msg(@ex[1])\n"
                + "msg(@ex[2])\n"
                + "msg(@ex[3])\n"
                + "), @ex, msg('2'))";
        try {
            MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, new File("file.txt"))), env, null, null);
            fail("This test was supposed to throw an exception");
        } catch (ConfigRuntimeException e) {
            //Pass
        }
        verify(fakePlayer, never()).sendMessage("2");
    }
}
