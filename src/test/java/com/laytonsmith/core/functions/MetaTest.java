

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import static com.laytonsmith.testing.StaticTest.*;
import org.junit.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class MetaTest {

    MCServer fakeServer;
    MCPlayer fakePlayer;
    com.laytonsmith.core.environments.Environment env;

    public MetaTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        fakePlayer = GetOnlinePlayer();
        fakeServer = GetFakeServer();
        CommandHelperPlugin.myServer = fakeServer;
		env = Static.GenerateStandaloneEnvironment();
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 10000)
    public void testRunas1() throws ConfigCompileException {
        String script =
                "runas('wraithguard02', '/cmd yay')";
        MCPlayer fakePlayer2 = GetOnlinePlayer("wraithguard02", fakeServer);
        when(fakeServer.getPlayer("wraithguard02")).thenReturn(fakePlayer2);
        when(fakePlayer.isOp()).thenReturn(true);
        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, true)), env, null, null);
        //verify(fakePlayer2).performCommand("cmd yay");
        verify(fakeServer).dispatchCommand(fakePlayer2, "cmd yay");
    }
    
    @Test public void testEval() throws ConfigCompileException{
        SRun("eval('msg(\\'Hello World!\\')')", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World!");
    }
    
    @Test public void testEval2() throws ConfigCompileException{
        SRun("assign(@e, 'msg(\\'Hello World!\\')') eval(@e)", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World!");
    }
    //:( I can't get this to work right, because AlwaysOpPlayer is different than
    //fakePlayer, so I can't get my test to activate when the function is called.
//    @Test(timeout=10000)
//    public void testRunas2() throws ConfigCompileException {
//        final AtomicBoolean bool = new AtomicBoolean(false); 
//        String script = 
//                "runas(~op, '/cmd yay')";
//        when(fakeServer.dispatchCommand(fakePlayer, "cmd yay")).thenAnswer(new Answer<Boolean>(){
//
//            public Boolean answer(InvocationOnMock invocation) throws Throwable {
//                assertTrue(((Server)invocation.getMock()).getPlayer(fakePlayer.getName()).isOp());
//                bool.set(true);
//                return true;
//            }
//            
//        });
//        MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null)), fakePlayer, null, null);
//        assertTrue(bool.get());
//    }
    }
