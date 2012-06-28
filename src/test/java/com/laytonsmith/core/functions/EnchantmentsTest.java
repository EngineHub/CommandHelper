/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.*;
import java.lang.reflect.InvocationTargetException;
import org.junit.*;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
/**
 *
 * @author Layton
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
public class EnchantmentsTest {
    MCServer fakeServer;
    MCPlayer fakePlayer;
    Env env = new Env();

    public EnchantmentsTest() {
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
        env.SetPlayer(fakePlayer);
        StaticTest.InstallFakeConvertor(fakePlayer);
        Static.InjectPlayer(fakePlayer);
    }

    @After
    public void tearDown() {
    }
    
    @Test
    /**
     * This is an interesting test. Because the server implementation has to implement the
     * individual enchantments, they aren't implemented here, so everything returns an empty
     * array. However, the test is more for testing array.clone than the enchantments themselves.
     */
    public void testGetEnchants() throws ConfigCompileException{
        SRun("assign(@a, get_enchants(311))\n"
                + "array_push(@a, 'test')\n"
                + "assign(@b, get_enchants(311))\n"
                + "msg(@a)\n"
                + "msg(@b)\n", fakePlayer);
        verify(fakePlayer).sendMessage("{test}");
        verify(fakePlayer).sendMessage("{}");
    }
}
