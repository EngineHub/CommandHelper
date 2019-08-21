package com.laytonsmith.core.functions;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import static com.laytonsmith.testing.CustomMatchers.regexMatch;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.SRun;
import static org.hamcrest.CoreMatchers.is;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class DataTransformationsTest {

	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;

	public DataTransformationsTest() throws Exception {
		StaticTest.InstallFakeServerFrontend();
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
	public void setUp() {
		fakePlayer = StaticTest.GetOnlinePlayer();
		fakeServer = StaticTest.GetFakeServer();
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(expected = ConfigRuntimeException.class)
	public void testIniEncodeFailsIfGivenArray() throws Exception {
		SRun("ini_encode(array(val1: 1, val2: array('hi')))", null);
	}

	@Test
	public void testIniEncodeWithNull() throws Exception {
		String nl = StringUtils.NL;
		assertThat(SRun("ini_encode(array(val1: null))", null), is(regexMatch("#.*?" + nl + "val1=")));
	}

	@Test
	public void testIniEncode() throws Exception {
		String nl = StringUtils.NL;
		assertThat(SRun("ini_encode(array(val1: 'value'), 'comment')", null), is(regexMatch("#comment" + nl + "#.*?" + nl + "val1=value")));
	}

}
