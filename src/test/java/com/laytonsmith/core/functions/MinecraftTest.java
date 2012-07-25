package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.*;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import com.sk89q.wepif.PermissionsResolverManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
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
    PermissionsResolverManager fakePerms;
    Env env = new Env();

    public MinecraftTest() {
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
        fakePerms = mock(PermissionsResolverManager.class);
        env.SetPlayer(fakePlayer);
        mockStatic(Static.class);
        when(Static.getPermissionsResolverManager()).thenReturn(null);
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
