package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.SRun;
import com.sk89q.wepif.PermissionsResolverManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


/**
 *
 * @author layton
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
public class PermissionsTest {
    MCServer fakeServer;
    MCPlayer fakePlayer;
    PermissionsResolverManager fakePerms;
    Env env = new Env();

    public PermissionsTest() {
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
    public void testHasPermission() throws ConfigCompileException{
        when(fakePerms.hasPermission(fakePlayer.getName(), "this.is.a.test")).thenReturn(true);
        when(fakePerms.hasPermission(fakePlayer.getName(), "does.not.have")).thenReturn(false);
        SRun("if(has_permission('this.is.a.test'), msg('success'))\n"
                + "if(!has_permission('does.not.have'), msg('success'))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("success");
    }
}
