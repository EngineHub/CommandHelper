package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.testing.StaticTest;
import org.junit.*;

import java.io.File;
import java.util.Set;

import static org.mockito.Mockito.verify;

public class ThreadingTest {


	MCServer fakeServer;
	MCPlayer fakePlayer;
	com.laytonsmith.core.environments.Environment env;
	static Set<Class<? extends com.laytonsmith.core.environments.Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();

	@BeforeClass
	public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		new File("profiler.config").deleteOnExit();
	}

	@Before
	public void setUp() throws Exception {
		fakePlayer = StaticTest.GetOnlinePlayer();
		fakeServer = StaticTest.GetFakeServer();
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {

	}

	@Test
	public void testInterrupt1() throws Exception {
		String script
				= "export('test', false)\n"
				+ "x_new_thread('test', closure() {\n"
				+ "	 while(true) {\n"
				+ "		 if(x_is_interrupted()) {\n"
				+ "			 msg(x_is_interrupted())\n"
				+ "			 return()\n"
				+ "		 }\n"
				+ "	 }\n"
				+ "})\n"
				+ "x_interrupt('test')\n"
				+ "x_thread_join('test')\n";

		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, null, true), null, envs), env, null, null);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testInterrupt2() throws Exception {
		String script
				= "export('test', false)\n"
				+ "x_new_thread('test', closure() {\n"
				+ "	 while(true) {\n"
				+ "		 if(x_clear_interrupt()) {\n"
				+ "			 msg(x_is_interrupted())\n"
				+ "			 return()\n"
				+ "		 }\n"
				+ "	 }\n"
				+ "})\n"
				+ "x_interrupt('test')\n"
				+ "x_thread_join('test')\n";

		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, null, true), null, envs), env, null, null);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testInterrupt3() throws Exception {
		String script
				= "export('test', false)\n"
				+ "x_new_thread('test', closure() {\n"
				+ "	 try {\n"
				+ "		 sleep(1000)\n"
				+ "	 } catch(InterruptedException @e) {\n"
				+ "		 msg(x_is_interrupted())\n"
				+ "		 return()\n"
				+ "	 }\n"
				+ "})\n"
				+ "x_interrupt('test')\n"
				+ "x_thread_join('test')\n";

		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, null, true), null, envs), env, null, null);
		verify(fakePlayer).sendMessage("false");
	}
}
