package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


/**
 *
 * @author layton
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
public class MinecraftTest {
    MCServer fakeServer;
    MCPlayer fakePlayer;
    com.laytonsmith.core.environments.Environment env;

    public MinecraftTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {        
        fakePlayer = StaticTest.GetOnlinePlayer();
        fakeServer = StaticTest.GetFakeServer();
		env = Static.GenerateStandaloneEnvironment();
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }
    
    @Test
    public void testIsTameable() throws ConfigCompileException{
        //Y U NO COOPERATE, TEST FRAMEWORK?
//        MCWorld fakeWorld = mock(MCWorld.class);
//        MCLocation fakeLocation = mock(MCLocation.class);
//        when(fakePlayer.getWorld()).thenReturn(fakeWorld);
//        when(fakePlayer.getLocation()).thenReturn(fakeLocation);
//        when(fakeLocation.getWorld()).thenReturn(fakeWorld);
//        
//        when(fakeWorld.spawnMob(eq("ocelot"), anyString(), anyInt(), (MCLocation)any(), (Target)any()))
//                .thenReturn(new CArray(Target.UNKNOWN, new CInt("10", Target.UNKNOWN)));
//        mockStatic(Static.class);
//        MCTameable fakeTameable = mock(MCTameable.class);
//        when(Static.getEntity(10)).thenReturn(fakeTameable);
//        Preferences fakePrefs = mock(Preferences.class);
//        when(Static.getPreferences()).thenReturn(fakePrefs);
//        when(fakePrefs.getPreference("debug-mode")).thenReturn(false);
//        
//        SRun("is_tameable(spawn_mob('ocelot')[0])", fakePlayer);
//        verify(fakePlayer).sendMessage("true");
        
    }
    
}
