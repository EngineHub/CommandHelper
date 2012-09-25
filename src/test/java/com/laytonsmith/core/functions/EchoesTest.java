

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import static com.laytonsmith.testing.StaticTest.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Layton
 */
public class EchoesTest {

    MCServer fakeServer;
    MCPlayer fakePlayer;
    com.laytonsmith.core.environments.Environment env;

    public EchoesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        fakeServer = GetFakeServer();
        fakePlayer = GetOnlinePlayer(fakeServer);
		env = Static.GenerateStandaloneEnvironment();
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testDocs() {
        TestClassDocs(Echoes.docs(), Echoes.class);
    }

    @Test(timeout = 10000)
    public void testChat() throws CancelCommandException {
        Echoes.chat a = new Echoes.chat();
        a.exec(Target.UNKNOWN, env, C.onstruct("Hello World!"));
        verify(fakePlayer).chat("Hello World!");
    }

    @Test(timeout = 10000)
    public void testBroadcast() throws NoSuchFieldException, InstantiationException,
            IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, CancelCommandException {
        Echoes.broadcast a = new Echoes.broadcast();
        when(fakePlayer.getServer()).thenReturn(fakeServer);
        CommandHelperPlugin.myServer = fakeServer;
        a.exec(Target.UNKNOWN, env, C.onstruct("Hello World!"));
        verify(fakeServer).broadcastMessage("Hello World!");
    }

    @Test(timeout = 10000)
    public void testLongStringMsgd1() throws ConfigCompileException {
        SRun("msg('@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@')", fakePlayer);
        verify(fakePlayer).sendMessage("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }
    
    @Test(timeout=10000)
    public void testLongStringMsgd2() throws ConfigCompileException{
        SRun("msg('.......................................................................................................................................................................')", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage(Mockito.anyString()); 
    }

    @Test(timeout = 10000)
    public void testChatas() throws CancelCommandException, ConfigCompileException {
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
//        a.exec(Target.UNKNOWN, fakePlayer, C.onstruct("wraithguard02"), C.onstruct("Hello World!"));
//        verify(wraithguard01).chat("Hello World!");        
    }
    
    @Test
    public void testIndentation() throws ConfigCompileException{
        SRun("msg('yay\n yay\n  yay\n   yay')", fakePlayer);
        verify(fakePlayer).sendMessage("yay");
        verify(fakePlayer).sendMessage(" yay");
        verify(fakePlayer).sendMessage("  yay");
        verify(fakePlayer).sendMessage("   yay");
    }
    
    @Test
    public void testColor() throws ConfigCompileException{
        assertEquals(String.format("\u00A7%s", "f"), SRun("color(white)", fakePlayer));
        assertEquals(String.format("\u00A7%s", "6"), SRun("color(gold)", fakePlayer));
        assertEquals(String.format("\u00A7%s", "k"), SRun("color(random)", fakePlayer));
        assertEquals(String.format("\u00A7%s", "m"), SRun("color(strike)", fakePlayer));
        assertEquals(String.format("\u00A7%s", "a"), SRun("color(a)", fakePlayer));
    }
}
