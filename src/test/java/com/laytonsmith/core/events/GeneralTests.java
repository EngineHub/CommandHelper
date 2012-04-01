package com.laytonsmith.core.events;


import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.abstraction.events.MCPlayerJoinEvent;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import com.sk89q.wepif.PermissionsResolverManager;
import java.io.File;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;
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
