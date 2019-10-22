package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.RunnableQueue;
import com.laytonsmith.abstraction.AbstractConvertor;
import com.laytonsmith.abstraction.Convertor;
import com.laytonsmith.abstraction.ConvertorHelper;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCAttributeModifier;
import com.laytonsmith.abstraction.MCColor;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCConsoleCommandSender;
import com.laytonsmith.abstraction.MCEnchantment;
import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCFireworkBuilder;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemMeta;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMetadataValue;
import com.laytonsmith.abstraction.MCNote;
import com.laytonsmith.abstraction.MCPattern;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.MCPluginMeta;
import com.laytonsmith.abstraction.MCPotionData;
import com.laytonsmith.abstraction.MCRecipe;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCWorld;
import com.laytonsmith.abstraction.enums.MCAttribute;
import com.laytonsmith.abstraction.enums.MCDyeColor;
import com.laytonsmith.abstraction.enums.MCEquipmentSlot;
import com.laytonsmith.abstraction.enums.MCPatternShape;
import com.laytonsmith.abstraction.enums.MCPotionType;
import com.laytonsmith.abstraction.enums.MCRecipeType;
import com.laytonsmith.abstraction.enums.MCTone;
import com.laytonsmith.annotations.convert;
import com.laytonsmith.annotations.noboilerplate;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.Optimizable;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Token;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.exceptions.CRE.AbstractCREException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CREThrowable;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.LoopBreakException;
import com.laytonsmith.core.exceptions.LoopContinueException;
import com.laytonsmith.core.extensions.ExtensionManager;
import com.laytonsmith.core.functions.BasicLogic.equals;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.natives.interfaces.Mixed;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 *
 */
public class StaticTest {

	static com.laytonsmith.core.environments.Environment env;
	static Set<Class<? extends Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();

	static {
		try {
			envs.add(CommandHelperEnvironment.class);
			Implementation.setServerType(Implementation.Type.TEST);
			env = Static.GenerateStandaloneEnvironment();
			env = env.cloneAndAdd(new CommandHelperEnvironment());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Tests the boilerplate functions in a Function. While all functions should conform to at least this, it is useful
	 * to also use the more strict TestBoilerplate function.
	 *
	 * @param ff
	 * @param name
	 * @throws java.lang.Exception
	 */
	public static void TestBoilerplate(FunctionBase ff, String name) throws Exception {
		if(!(ff instanceof Function)) {
			return;
		}
		Function f = (Function) ff;
		//For the "quality test code coverage" number, set this to true
		boolean runQualityTestsOnly = false;

		MCServer fakeServer = StaticTest.GetFakeServer();
		MCPlayer fakePlayer = StaticTest.GetOnlinePlayer("Player01", fakeServer);

		//make sure that these functions don't throw an exception. Any other results
		//are fine
		f.isRestricted();
		f.runAsync();
		f.preResolveVariables();
		f.thrown();

		//name should match the given value
		if(!f.getName().equals(name)) {
			fail("Expected name of function to be " + name + ", but was given " + f.getName());
		}

		//docs needs to at least be more than a non-empty string, though in the future this should follow a more strict
		//requirement set.
		if(f.docs().length() <= 0) {
			fail("docs must return a non-empty string");
		}

		TestDocs(f);

		if(f.numArgs().length == 0) {
			fail("numArgs must return an Integer array with more than zero values");
		}

		//If we want a "quality test coverage" number, we can't run this section, because it bombards the code
		//with random data to see if it fails in expected ways (to simulate how a user could run the scripts)
		//If we are interested in tests that are specific to the functions however, we shouldn't run this.
		if(!runQualityTestsOnly && f.getClass().getAnnotation(noboilerplate.class) == null) {
			TestExec(f, fakePlayer, "fake player");
			TestExec(f, null, "null command sender");
			TestExec(f, StaticTest.GetFakeConsoleCommandSender(), "fake console command sender");
		}

		//Let's make sure that if execs is defined in the class, useSpecialExec returns true.
		//Same thing for optimize/canOptimize and optimizeDynamic/canOptimizeDynamic
		if(f instanceof Optimizable) {
			Set<Optimizable.OptimizationOption> options = ((Optimizable) f).optimizationOptions();
			if(options.contains(Optimizable.OptimizationOption.CONSTANT_OFFLINE) && options.contains(Optimizable.OptimizationOption.OPTIMIZE_CONSTANT)) {
				fail(f.getName() + " declares both CONSTANT_OFFLINE and OPTIMIZE_CONSTANT, which are mutually exclusive.");
			}
		}
		for(Method method : f.getClass().getDeclaredMethods()) {
			if(method.getName().equals("execs")) {
				if(!f.useSpecialExec()) {
					fail(f.getName() + " declares execs, but returns false for useSpecialExec.");
				}
			}

			if(f instanceof Optimizable) {
				Set<Optimizable.OptimizationOption> options = ((Optimizable) f).optimizationOptions();
				if(method.getName().equals("optimize")) {
					if(!options.contains(Optimizable.OptimizationOption.OPTIMIZE_CONSTANT)) {
						fail(f.getName() + " declares optimize, but does not declare that it can OPTIMIZE_CONSTANT");
					}
				}
				if(method.getName().equals("optimizeDynamic")) {
					if(!options.contains(Optimizable.OptimizationOption.OPTIMIZE_DYNAMIC)) {
						fail(f.getName() + " declares optimizeDynamic, but does not declare that it can OPTIMIZE_DYNAMIC");
					}
				}
			}
		}

		//now the only function left to test is exec. This cannot be abstracted, unfortunately.
	}

	/**
	 * Checks to see if the documentation follows the specified format
	 *
	 * @param f
	 */
	public static void TestDocs(Function f) {
		//TODO
	}

	private static final ArrayList<String> TESTED = new ArrayList<String>();

	public static void TestExec(Function f, MCCommandSender p, String commandType) throws Exception {
		if(TESTED.contains(f.getName() + String.valueOf(p))) {
			return;
		}
		TESTED.add(f.getName() + String.valueOf(p));
		env.getEnv(CommandHelperEnvironment.class).SetCommandSender(p);
		//See if the function throws something other than a ConfigRuntimeException or CancelCommandException if we send it bad arguments,
		//keeping in mind of course, that it isn't supposed to be able to accept the wrong number of arguments. Specifically, we want to try
		//strings, numbers, arrays, and nulls
		for(Integer i : f.numArgs()) {
			if(i == Integer.MAX_VALUE) {
				//er.. let's just try with 10...
				i = 10;
			}
			Mixed[] con = new Mixed[i];
			//Throw the book at it. Most functions will fail, and that is ok, what isn't
			//ok is if it throws an unexpected type of exception. It should only ever
			//throw a ConfigRuntimeException, or a CancelCommandException. Further,
			//if it throws a ConfigRuntimeException, the documentation should state so.
			for(int z = 0; z < 10; z++) {
				for(int a = 0; a < i; a++) {
					switch(z) {
						case 0:
							con[a] = C.onstruct("hi");
							break;
						case 1:
							con[a] = C.onstruct(1);
							break;
						case 2:
							con[a] = C.Array(C.onstruct("hi"), C.onstruct(1));
							break;
						case 3:
							con[a] = C.Null();
							break;
						case 4:
							con[a] = C.onstruct(-1);
							break;
						case 5:
							con[a] = C.onstruct(0);
							break;
						case 6:
							con[a] = C.onstruct(100);
							break;
						case 7:
							con[a] = C.onstruct(a);
							break;
						case 8:
							con[a] = C.onstruct(true);
							break;
						case 9:
							con[a] = C.onstruct(false);
							break;
					}
				}
				try {
					f.exec(Target.UNKNOWN, env, con);
				} catch (CancelCommandException e) {
				} catch (ConfigRuntimeException e) {
					if(f.getName().equals("throw")) {
						// throw() can throw anything.
						return;
					}
					String name = AbstractCREException.getExceptionName(e);
					// This eventually needs to be changed. It should return class
					// objects instead, but for now, it returns an enum. This will
					// be a large change.
					List<String> expectedNames = new ArrayList<>();
					Class[] thrown = f.thrown();
					if(thrown == null) {
						thrown = new Class[0];
					}
					for(Class<? extends CREThrowable> tt : thrown) {
						expectedNames.add(ClassDiscovery.GetClassAnnotation(tt, typeof.class).value());
					}
					if(f.thrown() == null || !expectedNames.contains(name)) {
						fail("The documentation for " + f.getName() + " doesn't state that it can throw a "
								+ name + ", but it did.");
					}
				} catch (Throwable e) {
					if(e instanceof LoopBreakException && !f.getName().equals("break")) {
						fail("Only break() can throw LoopBreakExceptions");
					}
					if(e instanceof LoopContinueException && !f.getName().equals("continue")) {
						fail("Only continue() can throw LoopContinueExceptions");
					}
					if(e instanceof FunctionReturnException && !f.getName().equals("return")) {
						fail("Only return() can throw FunctionReturnExceptions");
					}
					if(e instanceof NullPointerException) {
						String error = (f.getName() + " breaks if you send it the following while using a " + commandType + ": " + Arrays.deepToString(con) + "\n");
						error += ("Here is the first few stack trace lines:\n");
						error += ("\t" + e.getStackTrace()[0].toString() + "\n");
						error += ("\t" + e.getStackTrace()[1].toString() + "\n");
						error += ("\t" + e.getStackTrace()[2].toString() + "\n");
						System.err.println(StackTraceUtils.GetStacktrace(e));
						if(!brokenJunk.contains(error)) {
							brokenJunk.add(error);
						}
					}
				}
			}
		}
	}
	static Set<String> brokenJunk = new TreeSet<String>();

	public static void TestClassDocs(String docs, Class container) {
		if(docs.length() <= 0) {
			fail("The docs for the " + container.getSimpleName() + " class are missing");
		}
	}

	/**
	 * Gets the value out of s construct, ignoring information like line numbers.
	 *
	 * @param c
	 * @return
	 */
	public static Object Val(Mixed c) {
		return c.val();
	}

	/**
	 * Checks to see if two constructs are equal, using the same method that MethodScript equals() uses. In fact, this
	 * method depends on equals() working, as it actually uses the function.
	 *
	 * @param expected
	 * @param actual
	 */
	public static void assertCEquals(Mixed expected, Mixed actual) throws CancelCommandException {
		equals e = new equals();
		CBoolean ret = (CBoolean) e.exec(Target.UNKNOWN, null, expected, actual);
		if(ret.getBoolean() == false) {
			throw new AssertionError("Expected " + expected + " and " + actual + " to be equal to each other");
		}
	}

	/**
	 * Does the opposite of assertCEquals
	 *
	 * @param expected
	 * @param actual
	 * @throws CancelCommandException
	 */
	public static void assertCNotEquals(Mixed expected, Mixed actual) throws CancelCommandException {
		equals e = new equals();
		CBoolean ret = (CBoolean) e.exec(Target.UNKNOWN, null, expected, actual);
		if(ret.getBoolean() == true) {
			throw new AssertionError("Did not expect " + expected + " and " + actual + " to be equal to each other");
		}
	}

	/**
	 * Verifies that the given construct <em>resolves</em> to true. The resolution uses ArgumentValidation.getBoolean to do the
	 * resolution.
	 *
	 * @param actual
	 */
	public static void assertCTrue(Mixed actual) {
		if(!ArgumentValidation.getBoolean(actual, Target.UNKNOWN)) {
			fail("Expected '" + actual.val() + "' to resolve to true, but it did not");
		}
	}

	/**
	 * Verifies that the given construct <em>resolves</em> to false. The resolution uses ArgumentValidation.getBoolean to do the
	 * resolution.
	 *
	 * @param actual
	 */
	public static void assertCFalse(Mixed actual) {
		if(ArgumentValidation.getBoolean(actual, Target.UNKNOWN)) {
			fail("Expected '" + actual.val() + "' to resolve to false, but it did not");
		}
	}

	/**
	 * This function is used to assert that the type of a construct is one of the specified types.
	 *
	 * @param test
	 * @param retTypes
	 */
	public static void assertReturn(Mixed test, Class... retTypes) {
		if(!Arrays.asList(retTypes).contains(test.getClass())) {
			StringBuilder b = new StringBuilder();
			if(retTypes.length == 1) {
				b.append("Expected return type to be ").append(retTypes[0].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
			} else if(retTypes.length == 2) {
				b.append("Expected return type to be either ").append(retTypes[0].getSimpleName()).append(" or ").append(retTypes[1].getSimpleName()).append(", but found ").append(test.getClass().getSimpleName());
			} else {
				b.append("Expected return type to be one of: ");
				for(int i = 0; i < retTypes.length; i++) {
					if(i < retTypes.length - 1) {
						b.append(retTypes[i].getSimpleName()).append(", ");
					} else {
						b.append("or ").append(retTypes[i].getSimpleName());
					}
				}
				b.append(", but found ").append(test.getClass().getSimpleName());
			}
			throw new AssertionError(b);
		}
	}

	public static List<Token> tokens(Token... array) {
		List<Token> tokens = new ArrayList<Token>();
		tokens.addAll(Arrays.asList(array));
		return tokens;
	}

	public static MCPlayer GetOnlinePlayer() {
		MCServer s = GetFakeServer();
		return GetOnlinePlayer("Player01", s);
	}

	public static MCPlayer GetOnlinePlayer(MCServer s) {
		return GetOnlinePlayer("Player01", s);
	}

	public static MCPlayer GetOnlinePlayer(String name, MCServer s) {
		return GetOnlinePlayer(name, "world", s);
	}

	public static MCPlayer GetOnlinePlayer(String name, String worldName, MCServer s) {
		MCPlayer p = mock(MCPlayer.class);
		MCWorld w = mock(MCWorld.class);
		MCLocation fakeLocation = StaticTest.GetFakeLocation(w, 0, 0, 0);
		MCItemStack fakeItemStack = mock(MCItemStack.class);

		when(w.getName()).thenReturn(worldName);
		when(p.getWorld()).thenReturn(w);
		when(p.isOnline()).thenReturn(true);
		when(p.getName()).thenReturn(name);
		when(p.getServer()).thenReturn(s);
		when(p.isOp()).thenReturn(true);
		if(s != null && s.getOnlinePlayers() != null) {
			Collection<MCPlayer> online = s.getOnlinePlayers();
			boolean alreadyOnline = false;
			for(MCPlayer o : online) {
				if(o.getName().equals(name)) {
					alreadyOnline = true;
					break;
				}
			}
			if(!alreadyOnline) {
				online.add(p);
				when(s.getOnlinePlayers()).thenReturn(new HashSet<MCPlayer>());
			}
		}

		//Plethora of fake data
		when(p.getCompassTarget()).thenReturn(fakeLocation);
		when(p.getItemAt((Integer) Mockito.any())).thenReturn(fakeItemStack);
		return p;
	}

	public static MCPlayer GetOp(String name, MCServer s) {
		MCPlayer p = GetOnlinePlayer(name, s);
		when(p.isOp()).thenReturn(true);
		return p;
	}

	public static BukkitMCWorld GetWorld(String name) {
		BukkitMCWorld w = mock(BukkitMCWorld.class);
		when(w.getName()).thenReturn(name);
		return w;
	}

	public static MCConsoleCommandSender GetFakeConsoleCommandSender() {
		MCConsoleCommandSender c = mock(MCConsoleCommandSender.class);
		when(c.getName()).thenReturn("CONSOLE");
		MCServer s = GetFakeServer();
		when(c.getServer()).thenReturn(s);
		return c;
	}

	public static MCLocation GetFakeLocation(MCWorld w, double x, double y, double z) {
		MCLocation loc = mock(BukkitMCLocation.class);
		when(loc.getWorld()).thenReturn(w);
		when(loc.getX()).thenReturn(x);
		when(loc.getY()).thenReturn(y - 1);
		when(loc.getZ()).thenReturn(z);
		return loc;
	}

	public static Object GetVariable(Object instance, String var) throws Exception {
		return GetVariable(instance.getClass(), var, instance);
	}

	public static Object GetVariable(Class c, String var, Object instance) throws Exception {
		Field f = c.getField(var);
		f.setAccessible(true);
		return f.get(instance);
	}

	/**
	 * Lexes, compiles, and runs a given MethodScript, using the given player.
	 *
	 * @param script
	 * @param player
	 * @throws Exception
	 */
	public static void Run(String script, MCCommandSender player) throws Exception {
		Run(script, player, null, null);
	}

	public static void Run(String script, MCCommandSender player, MethodScriptComplete done, Environment env) throws Exception {
		InstallFakeServerFrontend();
		if(env == null) {
			env = StaticTest.env;
		}
		env.getEnv(CommandHelperEnvironment.class).SetCommandSender(player);
		MethodScriptCompiler.execute(MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, env, null, true), env, envs), env, done, null);
	}

	public static void RunCommand(String combinedScript, MCCommandSender player, String command) throws Exception {
		RunCommand(combinedScript, player, command, env);
	}

	public static void RunCommand(String combinedScript, MCCommandSender player, String command, Environment env) throws Exception {
		InstallFakeServerFrontend();
		if(env == null) {
			env = StaticTest.env;
		}
		env.getEnv(CommandHelperEnvironment.class).SetCommandSender(player);
		List<Script> scripts = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(combinedScript, env, null, false), env.getEnvClasses());
		for(Script s : scripts) {
			s.compile();
			if(s.match(command)) {
				s.run(s.getVariables(command), env, null);
			}
		}
	}

	public static String SRun(String script, MCCommandSender player, Environment env) throws Exception {
		InstallFakeServerFrontend();
		final StringBuffer b = new StringBuffer();
		Run(script, player, new MethodScriptComplete() {

			@Override
			public void done(String output) {
				b.append(output);
			}
		}, env);
		return b.toString();
	}

	public static String SRun(String script, MCCommandSender player) throws Exception {
		return SRun(script, player, env);
	}
	//TODO: Fix this
//	public static void RunVars(List<Variable> vars, String script, MCCommandSender player) throws Exception{
//		Env env = new Env();
//		env.SetCommandSender(player);
//		MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null));
//		injectAliasCore();
//		Script s = MethodScriptCompiler.preprocess(MethodScriptCompiler.lex(script, null), env).get(0);
//		s.compile();
//		s.run(vars, env, null);
//
//	}

	//Blarg. Dumb thing.
//	private static void injectAliasCore() throws Exception{
//		PermissionsResolverManager prm = mock(PermissionsResolverManager.class);
//		CommandHelperPlugin chp = mock(CommandHelperPlugin.class);
//		AliasCore ac = new AliasCore(new File("plugins/CommandHelper/config.txt"),
//				new File("plugins/CommandHelper/LocalPackages"),
//				new File("plugins/CommandHelper/preferences.ini"),
//				new File("plugins/CommandHelper/main.ms"), prm, chp);
//		try {
//			Field aliasCore = CommandHelperPlugin.class.getDeclaredField("ac");
//			aliasCore.setAccessible(true);
//			aliasCore.set(null, ac);
//		} catch (Exception e){
//			throw new RuntimeException("Core could not be injected", e);
//		}
//	}
	/**
	 * Creates an entire fake server environment, adding players and everything.
	 *
	 * @return The fake MCServer
	 */
	public static MCServer GetFakeServer() {
		MCServer fakeServer = mock(MCServer.class);
		String[] pnames = new String[]{"player1", "player2", "player3"};
		ArrayList<MCPlayer> pps = new ArrayList<MCPlayer>();
		for(String p : pnames) {
			MCPlayer pp = GetOnlinePlayer(p, fakeServer);
			pps.add(pp);
		}
		when(fakeServer.getOnlinePlayers()).thenReturn(new HashSet<MCPlayer>());
		CommandHelperPlugin.myServer = fakeServer;
		return fakeServer;
	}

	private static boolean frontendInstalled = false;

	/**
	 * This installs a fake server frontend. You must have already included
	 *
	 * @PrepareForTest(Static.class) in the calling test code, which will allow the proper static methods to be mocked.
	 */
	public static void InstallFakeServerFrontend() {
		if(frontendInstalled) {
			return;
		}
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(Static.class));
		ClassDiscovery.getDefaultInstance().addDiscoveryLocation(ClassDiscovery.GetClassContainer(StaticTest.class));
		ExtensionManager.Initialize(ClassDiscovery.getDefaultInstance());
		Implementation.setServerType(Implementation.Type.TEST);
		AliasCore fakeCore = mock(AliasCore.class);
		fakeCore.autoIncludes = new ArrayList<File>();
		SetPrivate(CommandHelperPlugin.class, "ac", fakeCore, AliasCore.class);
		frontendInstalled = true;
		try {
			Prefs.init(new File("preferences.ini"));
		} catch (IOException ex) {
			Logger.getLogger(StaticTest.class.getName()).log(Level.SEVERE, null, ex);
		}
		MSLog.initialize(new File("."));
	}

	/**
	 * Installs the fake convertor into the server, so event based calls will work. Additionally, adds the fakePlayer to
	 * the server, if player based events are to be called, this is the player returned.
	 *
	 * @param fakePlayer
	 * @throws java.lang.Exception
	 */
	public static void InstallFakeConvertor(MCPlayer fakePlayer) throws Exception {
		InstallFakeServerFrontend();
		try {
			//We need to add the test directory to the ClassDiscovery path
			//This should probably not be hard coded at some point.
			ClassDiscovery.getDefaultInstance().addDiscoveryLocation(new File("./target/test-classes").toURI().toURL());
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}

		Implementation.setServerType(Implementation.Type.TEST);
		MCServer fakeServer = GetFakeServer();
		TestConvertor.fakeServer = fakeServer;
		FakeServerMixin.fakePlayer = fakePlayer;

	}

	@convert(type = Implementation.Type.TEST)
	public static class TestConvertor extends AbstractConvertor {

		private static MCServer fakeServer;
		private final RunnableQueue queue = new RunnableQueue("TestConvertorRunnableQueue");

		@Override
		public MCLocation GetLocation(MCWorld w, double x, double y, double z, float yaw, float pitch) {
			return StaticTest.GetFakeLocation(w, x, y + 1, z);
		}

		@Override
		public Class GetServerEventMixin() {
			return FakeServerMixin.class;
		}

		@Override
		public MCEnchantment[] GetEnchantmentValues() {
			Convertor c = new BukkitConvertor();
			return c.GetEnchantmentValues();
		}

		@Override
		public MCEnchantment GetEnchantmentByName(String name) {
			Convertor c = new BukkitConvertor();
			return c.GetEnchantmentByName(name);
		}

		@Override
		public MCServer GetServer() {
			return fakeServer;
		}

		@Override
		public void Startup(CommandHelperPlugin chp) {
			//Nothing.
		}

		@Override
		public MCItemStack GetItemStack(MCMaterial type, int qty) {
			Convertor c = new BukkitConvertor();
			return c.GetItemStack(type, qty);
		}

		@Override
		public MCItemStack GetItemStack(String type, int qty) {
			Convertor c = new BukkitConvertor();
			return c.GetItemStack(type, qty);
		}

		@Override
		public MCPotionData GetPotionData(MCPotionType type, boolean extended, boolean upgraded) {
			Convertor c = new BukkitConvertor();
			return c.GetPotionData(type, extended, upgraded);
		}

		@Override
		public MCAttributeModifier GetAttributeModifier(MCAttribute attr, UUID id, String name, double amt, MCAttributeModifier.Operation op, MCEquipmentSlot slot) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public int SetFutureRunnable(DaemonManager dm, long ms, Runnable r) {
			//This needs fixing later
			queue.invokeLater(dm, r);
			return 0;
		}

		@Override
		public void ClearAllRunnables() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void ClearFutureRunnable(int id) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public int SetFutureRepeater(DaemonManager dm, long ms, long initialDelay, Runnable r) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCEntity GetCorrectEntity(MCEntity e) {
			return e;
		}

		@Override
		public MCInventory GetEntityInventory(MCEntity entity) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCInventory GetLocationInventory(MCLocation location) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCInventoryHolder CreateInventoryHolder(String id) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCNote GetNote(int octave, MCTone tone, boolean sharp) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCColor GetColor(final int red, final int green, final int blue) {
			return new MCColor() {

				@Override
				public int getRed() {
					return red;
				}

				@Override
				public int getGreen() {
					return green;
				}

				@Override
				public int getBlue() {
					return blue;
				}

				@Override
				public MCColor build(int red, int green, int blue) {
					return GetColor(red, green, blue);
				}
			};
		}

		@Override
		public MCColor GetColor(String colorName, Target t) throws CREFormatException {
			return ConvertorHelper.GetColor(colorName, t);
		}

		@Override
		public MCPattern GetPattern(MCDyeColor color, MCPatternShape shape) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCFireworkBuilder GetFireworkBuilder() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCPluginMeta GetPluginMeta() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCMaterial[] GetMaterialValues() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCMaterial GetMaterialFromLegacy(String name, int data) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCMaterial GetMaterialFromLegacy(int id, int data) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCItemMeta GetCorrectMeta(MCItemMeta im) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public List<MCEntity> GetEntitiesAt(MCLocation loc, double radius) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCRecipe GetNewRecipe(String key, MCRecipeType type, MCItemStack result) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCRecipe GetRecipe(MCRecipe unspecific) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCMaterial GetMaterial(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public MCMetadataValue GetMetadataValue(Object value, MCPlugin plugin) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String GetPluginName() {
			return new BukkitConvertor().GetPluginName();
		}

		@Override
		public MCPlugin GetPlugin() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String GetUser(Environment env) {
			return "testUser";
		}
	}

	public static class FakeServerMixin implements EventMixinInterface {

		public static MCPlayer fakePlayer;
		public boolean cancelled = false;

		public FakeServerMixin(AbstractEvent e) {

		}

		@Override
		public void cancel(BindableEvent e, boolean state) {
			cancelled = state;
		}

		@Override
		public boolean isCancellable(BindableEvent o) {
			return true;
		}

		@Override
		public Map<String, Mixed> evaluate_helper(BindableEvent e) throws EventException {
			Map<String, Mixed> map = new HashMap<>();
			if(fakePlayer != null) {
				map.put("player", new CString(fakePlayer.getName(), Target.UNKNOWN));
			}
			return map;
		}

		@Override
		public void manualTrigger(BindableEvent e) {
			throw new RuntimeException("Manual triggering is not supported in tests yet");
		}

		@Override
		public boolean isCancelled(BindableEvent o) {
			return cancelled;
		}

	}

	/**
	 * Returns the value of a private (or any other variable for that matter) data member contained in the object
	 * provided. If the value isn't there, the test fails automatically.
	 *
	 * @param in The object to look in, or the Class object for static varibles.
	 * @param name The name of the variable to get.
	 * @param expected The type of the value that you expect to be. This is the type that will be returned.
	 * @return
	 */
	public static <T> T GetPrivate(Object in, String name, Class<T> expected) {
		return GetSetPrivate(in, name, null, false, expected);
	}

	/**
	 * Sets the value of a private (or any other variable for that matter) data member contained in the object provided.
	 *
	 * @param in Either the class of the object (for static variables) or an instance of the object.
	 * @param name The name of the field
	 * @param value The actual value to set
	 * @param expected The type of the value that you expect to be in the code.
	 */
	public static void SetPrivate(Object in, String name, Object value, Class expected) {
		GetSetPrivate(in, name, value, true, expected);
	}

	private static <T> T GetSetPrivate(Object in, String name, Object value, boolean isSet, Class<T> expected) {
		Object ret = null;
		try {
			Field f = null;
			Class search = in.getClass();
			if(in instanceof Class) {
				search = (Class) in;
			}
			while(search != null) {
				try {
					f = search.getDeclaredField(name);
					break;
				} catch (NoSuchFieldException e) {
					search = search.getSuperclass();
				}
			}
			if(f == null) {
				throw new NoSuchFieldException();
			}
			f.setAccessible(true);
			if(expected != null && !expected.isAssignableFrom(f.getType())) {
				fail("Expected the value to be a " + expected.getName() + ", but it was actually a " + f.getType().getName());
			}
			if(isSet) {
				f.set(in, value);
			} else {
				ret = f.get(in);
			}
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			//This shouldn't happen ever, since we are using the class provided by in, and sending
			//get/set in as well.
			fail(ex.getMessage());
		} catch (NoSuchFieldException ex) { //This shouldn't happen ever, since we set it to accessible
			fail("No such field \"" + name + "\" exists in the class " + in.getClass().getName());
		} catch (SecurityException ex) {
			fail("A security policy is preventing the test from getting \"" + name + "\" in the object provided.");
		}
		return (T) ret;
	}

	/**
	 * Installs a fake logger. Returns the proxied object.
	 *
	 * @return
	 */
	public static MSLog InstallFakeLogger() {
		MSLog l = mock(MSLog.class);
		SetPrivate(MSLog.class, "instance", l, MSLog.class);
		return l;
	}

}
