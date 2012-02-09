/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.MScriptCompiler;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import org.junit.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class MetaTest {
    
    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();
    

    public MetaTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
        CommandHelperPlugin.perms = mock(PermissionsResolverManager.class);
        CommandHelperPlugin.myServer = fakeServer;
        env.SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    @Test public void testRunas1() throws ConfigCompileException{
        String script = 
                "runas('wraithguard02', '/cmd yay')";
        MCPlayer fakePlayer2 = StaticTest.GetOnlinePlayer("wraithguard02", fakeServer);
        when(fakeServer.getPlayer("wraithguard02")).thenReturn(fakePlayer2);
        when(fakePlayer.isOp()).thenReturn(true);
        MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(script, null)), env, null, null);
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
