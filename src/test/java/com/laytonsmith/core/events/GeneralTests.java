package com.laytonsmith.core.events;


import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author layton
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest()
public class GeneralTests {
//    MCPlayer fakePlayer;
//    public GeneralTests() {
//    }
//
//    @BeforeClass
//    public static void setUpClass(){
//        //StaticTest.StartServer();
//        Plugin fakePlugin = mock(Plugin.class);        
//        CommandHelperPlugin.persist = new SerializedPersistance(new File("plugins/CommandHelper/persistance.ser"), fakePlugin);
//    }
//    @Before
//    public void setUp() {        
//        fakePlayer = StaticTest.GetOnlinePlayer();
//        StaticTest.InstallFakeConvertor(fakePlayer);
//    }
//    @After
//    public void tearDown(){
//        EventUtils.UnregisterAll();
//    }
//    
//    @Test public void testCallProcInEventHandler() throws ConfigCompileException{
//        String script = "proc(_testproc, @text, msg(@text))"
//                + "bind(player_join, array(priority: highest), null, @eb, msg(@eb)"
//                + "msg(call_proc(_testproc, @eb['player']))  msg(@eb))";
//        MCPlayerJoinEvent mcpje = mock(MCPlayerJoinEvent.class);
//        when(mcpje.getPlayer()).thenReturn(fakePlayer);
//        when(mcpje.getJoinMessage()).thenReturn("player joined");
//        SRun(script, null);
//        EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", mcpje);
//        verify(fakePlayer).sendMessage("");
//    }
}
