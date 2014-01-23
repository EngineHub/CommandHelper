package com.laytonsmith.core.events;


import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.events.MCPlayerJoinEvent;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
//import static org.powermock.api.mockito.PowerMockito.when;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 * @author layton
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Static.class)
public class GeneralTest {
    MCPlayer fakePlayer;
    public GeneralTest() {
    }

    @BeforeClass
    public static void setUpClass(){

    }
    @Before
    public void setUp() throws Exception {        
        fakePlayer = StaticTest.GetOnlinePlayer();
        StaticTest.InstallFakeConvertor(fakePlayer);
        Static.InjectPlayer(fakePlayer);
    }
    @After
    public void tearDown(){
        EventUtils.UnregisterAll();
    }
    

//    @Test
//    //This is moreso a test of the event testing framework
//    public void testBasicEventUsage() throws ConfigCompileException{
//        //Register the event handler
//        String script = "bind(player_join, null, null, @event, msg('success'))";
//        SRun(script, null);
//                
//        //Create the mock event, and have it return the fakePlayer
//        MCPlayerJoinEvent mcpje = mock(MCPlayerJoinEvent.class);        
//        when(mcpje.getPlayer()).thenReturn(fakePlayer);
//        
//        //Trigger the event
//        EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", mcpje);
//        
//        //Verify that it ran correctly
//        verify(fakePlayer).sendMessage("success");
//    }
    
    //This is an invalid test. It's working as expected, because the proc is not in scope
    //When the event is run.
//    @Test 
//    public void testCallProcInEventHandler() throws ConfigCompileException{
//        
//        String script = ""
//                + "proc(_testproc, @text, msg(@text))"
//                + "bind(player_join, array(priority: highest), null, @eb, "
//                + " msg(@eb)"
//                + " call_proc(_testproc, @eb['player'])"
//                + " msg(@eb)"
//                + ")";
//        MCPlayerJoinEvent mcpje = mock(MCPlayerJoinEvent.class);
//        when(mcpje.getPlayer()).thenReturn(fakePlayer);
//        when(mcpje.getJoinMessage()).thenReturn("player joined");
//        SRun(script, null);
//        EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", mcpje);
//        String name = fakePlayer.getName();
//        InOrder inOrder = Mockito.inOrder(fakePlayer);
//        inOrder.verify(fakePlayer).sendMessage("{join_message: player joined, player: " + name + "}");
//        inOrder.verify(fakePlayer).sendMessage(name);
//        inOrder.verify(fakePlayer).sendMessage("{join_message: player joined, player: " + name + "}");
//    }
    
}
