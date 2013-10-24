

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.*;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.verify;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
/**
 *
 * @author Layton
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Static.class)
@PowerMockIgnore({"javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*"})
public class EnchantmentsTest {
    MCServer fakeServer;
    MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;

    public EnchantmentsTest() throws Exception {
		InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
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
        env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
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
