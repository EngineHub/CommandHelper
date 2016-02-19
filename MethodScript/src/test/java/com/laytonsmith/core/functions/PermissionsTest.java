package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;

import static org.mockito.Mockito.mock;
//import static org.powermock.api.mockito.PowerMockito.*;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

//TODO: Nothing in this class works anymore due to PowerMock missing.
//This should be re-implemented once permissions aren't static anymore.
/**
 *
 * 
 */

//@RunWith(PowerMockRunner.class)
//@PrepareForTest({Static.class, CommandHelperPlugin.class})
public class PermissionsTest {
    MCServer fakeServer;
    MCPlayer fakePlayer;
    com.laytonsmith.core.environments.Environment env;

    public PermissionsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        Prefs.init(new File("plugins/CommandHelper/preferences.ini"));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {        
        fakePlayer = StaticTest.GetOnlinePlayer();
        MCWorld fakeWorld = mock(MCWorld.class);
//        when(fakeWorld.getName()).thenReturn("world");
//        when(fakePlayer.getWorld()).thenReturn(fakeWorld);
        fakeServer = StaticTest.GetFakeServer();
		env = Static.GenerateStandaloneEnvironment();
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }
    
//    @Test
//    public void testHasPermission() throws ConfigCompileException{
//        when(fakePerms.hasPermission(fakePlayer.getName(), "this.is.a.test")).thenReturn(true);
//        when(fakePerms.hasPermission(fakePlayer.getName(), "does.not.have")).thenReturn(false);
//        SRun("if(has_permission('this.is.a.test'), msg('success1'))\n"
//                + "if(!has_permission('does.not.have'), msg('success2'))", fakePlayer, env);
//        verify(fakePlayer).sendMessage("success1");
//        verify(fakePlayer).sendMessage("success2");
//    }
//    
//    @Test
//    public void testQuickPermissions() throws ConfigCompileException{
//        when(fakePlayer.isOp()).thenReturn(false);
//        when(fakePlayer.isOnline()).thenReturn(Boolean.TRUE);
//        Static.InjectPlayer(fakePlayer);
//        String world = fakePlayer.getWorld().getName();
//        String name = fakePlayer.getName();
//        when(fakePerms.hasPermission(name, "commandhelper.alias.simple", world)).thenReturn(true);
//        StaticTest.RunCommand("simple:/cmd = tmsg(player(), 'hi')", fakePlayer, "/cmd", env);
//        verify(fakePerms, atLeastOnce()).hasPermission(name, "commandhelper.alias.simple", world);
//    }
    
    //TODO: Get this working again. The behavior is correct, but somewhere along the line, the perms that actually
    //gets used ends up not being the same as fakePerms
//    @Test
//    public void testLongPermissions() throws ConfigCompileException{
//        when(fakePlayer.isOp()).thenReturn(false);
//        when(fakePlayer.isOnline()).thenReturn(Boolean.TRUE);
//        String world = fakePlayer.getWorld().getName();
//        String name = fakePlayer.getName();
//        Static.InjectPlayer(fakePlayer);
//        when(fakePerms.hasPermission(world, name, "arbitrary.permission")).thenReturn(true);
//        StaticTest.RunCommand("arbitrary.permission:/cmd = tmsg(player(), 'hi')", fakePlayer, "/cmd");
//        verify(fakePerms, atLeastOnce()).hasPermission(world, name, "arbitrary.permission");
//    }
    
//    @Test
//    public void testGroupPermissions() throws ConfigCompileException{
//        when(fakePlayer.isOp()).thenReturn(false);
//        when(fakePlayer.isOnline()).thenReturn(Boolean.TRUE);
//        Static.InjectPlayer(fakePlayer);
//        when(fakePerms.inGroup(fakePlayer.getName(), "group1")).thenReturn(false);
//        when(fakePerms.inGroup(fakePlayer.getName(), "group2")).thenReturn(true);
//        StaticTest.RunCommand("~group1/group2:/cmd = tmsg(player(), 'hi')", fakePlayer, "/cmd", env);
//        verify(fakePerms, atLeastOnce()).inGroup(fakePlayer.getName(), "group1");
//        verify(fakePerms, atLeastOnce()).inGroup(fakePlayer.getName(), "group2");
//    }
//    
//    @Test(expected=ConfigRuntimeException.class)
//    public void testNegativeGroupPermissions() throws ConfigCompileException{
//        when(fakePlayer.isOp()).thenReturn(false);
//        when(fakePlayer.isOnline()).thenReturn(Boolean.TRUE);
//        Static.InjectPlayer(fakePlayer);
//        when(fakePerms.inGroup(fakePlayer.getName(), "group1")).thenReturn(false);
//        when(fakePerms.inGroup(fakePlayer.getName(), "group2")).thenReturn(true);
//        StaticTest.RunCommand("~group1/-group2:/cmd = tmsg(player(), 'hi')", fakePlayer, "/cmd", env);
//        //We expect this to fail; we don't have permission.
//    }
}
