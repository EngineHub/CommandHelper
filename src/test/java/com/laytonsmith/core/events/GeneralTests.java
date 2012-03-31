package com.laytonsmith.core.events;


import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.io.File;
import org.bukkit.plugin.Plugin;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import static org.mockito.Mockito.*;

/**
 *
 * @author layton
 */
public class GeneralTests {
    MCPlayer fakePlayer;
    public GeneralTests() {
    }

    @BeforeClass
    public static void setUpClass(){
        Plugin fakePlugin = mock(Plugin.class);        
        CommandHelperPlugin.persist = new SerializedPersistance(new File("plugins/CommandHelper/persistance.ser"), fakePlugin);
    }
    @Before
    public void setUp() {
        fakePlayer = StaticTest.GetOnlinePlayer();
    }
    
    @Test public void testCallProcInEventHandler() throws ConfigCompileException{
        String script = "";
    }
}
