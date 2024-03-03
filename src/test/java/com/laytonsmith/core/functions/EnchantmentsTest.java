package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.GetOnlinePlayer;
import static com.laytonsmith.testing.StaticTest.InstallFakeServerFrontend;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.verify;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 *
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Static.class)
//@PowerMockIgnore({"javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*"})
public class EnchantmentsTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;

	public EnchantmentsTest() throws Exception {
		InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
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
	 * This is an interesting test. Because the server implementation has to implement the individual enchantments, they
	 * aren't implemented here, so everything returns an empty array. However, the test is more for testing array.clone
	 * than the enchantments themselves.
	 */
	public void testGetEnchants() throws Exception {
		SRun("assign(@a, get_enchants(array('name': 'DIAMOND_SWORD')))\n"
				+ "array_push(@a, 'test')\n"
				+ "assign(@b, get_enchants(array('name': 'DIAMOND_SWORD')))\n"
				+ "msg(@a)\n"
				+ "msg(@b)\n", fakePlayer);
		verify(fakePlayer).sendMessage("{test}");
		verify(fakePlayer).sendMessage("{}");
	}
}
