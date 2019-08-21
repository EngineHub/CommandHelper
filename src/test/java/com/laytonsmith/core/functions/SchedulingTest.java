package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.mockito.Mockito.verify;

/**
 *
 */
public class SchedulingTest {

	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		StaticTest.InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
		fakePlayer = StaticTest.GetOnlinePlayer();
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testParseDate() throws Exception {
		SRun("@format = 'EEE, d MMM yyyy HH:mm:ss Z'; msg(parse_date(@format, simple_date(@format, 1000)));",
				fakePlayer);
		verify(fakePlayer).sendMessage("1000");
	}
}
