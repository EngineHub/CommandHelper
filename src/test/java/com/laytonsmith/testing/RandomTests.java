package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.annotations.api.Platforms;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Command;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.ArrayHandling;
import com.laytonsmith.core.functions.Function;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.persistence.PersistenceNetworkImpl;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static com.laytonsmith.testing.StaticTest.Run;
import static com.laytonsmith.testing.StaticTest.SRun;
import java.awt.HeadlessException;
import java.util.Arrays;
import java.util.HashSet;

/**
 *
 *
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Static.class)
public class RandomTests {

	MCPlayer fakePlayer;

	Set<Class<? extends Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();

	@Before
	public void setUp() throws Exception {
		fakePlayer = StaticTest.GetOnlinePlayer();
		StaticTest.InstallFakeConvertor(fakePlayer);
	}
	private static final Set<String> TESTED_FUNCTIONS = new TreeSet<>();

	/**
	 * This function automatically tests all the boilerplate portions of all functions. Note that this can be disabled
	 * in the StaticTest class, so that high quality test coverage can be measured.
	 */
	@Test
	@SuppressWarnings({"ThrowableResultIgnored", "CallToPrintStackTrace"})
	public void testAllBoilerplate() {
		Map<String, Throwable> uhohs = new HashMap<>();
		String[] requiredMethods = new String[]{"toString", "equals", "hashCode"};
		//Ensure that all the abstraction objects overloaded
		StaticTest.InstallFakeServerFrontend();
		outer:
		for(Class c : ClassDiscovery.getDefaultInstance().loadClassesThatExtend(AbstractionObject.class)) {
			inner:
			for(Class inter : c.getInterfaces()) {
				for(Class extended : inter.getInterfaces()) {
					if(extended == AbstractionObject.class && !c.isInterface()) {
						//This is a direct subclass of an interface that implements AbstractionObject, so check
						//to ensure that the equals, toString, and hashCode methods are overloaded.
						break inner;
					}
				}
				//It's not, so just skip it.
				continue outer;
			}
			Method[] methods = c.getDeclaredMethods();
			required:
			for(String required : requiredMethods) {
				for(Method method : methods) {
					if(method.getName().equals(required)) {
						continue required;
					}
				}
				uhohs.put(c.getName() + " " + required, new NoSuchMethodException(c.getSimpleName() + " does not define " + required));
			}
		}
		Set<String> classDocs = new TreeSet<>();

		for(FunctionBase f : FunctionList.getFunctionList(null, envs)) {
			try {
				if(TESTED_FUNCTIONS.contains(f.getName())) {
					continue;
				}
				TESTED_FUNCTIONS.add(f.getName());

				StaticTest.TestBoilerplate(f, f.getName());
				Class upper = f.getClass().getEnclosingClass();
				if(upper == null) {
					fail(f.getName() + " is not enclosed in an upper class.");
					return;
				}
				try {
					Method m = upper.getMethod("docs", new Class[]{});
					try {
						String docs = m.invoke(null, new Object[]{}).toString();
						if(!classDocs.contains(docs)) {
							StaticTest.TestClassDocs(docs, upper);
							classDocs.add(docs);
						}
					} catch (NullPointerException ex) {
						fail(upper.getName() + "'s docs function should be static");
					}
				} catch (IllegalAccessException | IllegalArgumentException | SecurityException ex) {
					Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvocationTargetException ex) {
					fail(upper.getName() + " throws an exception!");
				} catch (NoSuchMethodException ex) {
					fail(upper.getName() + " does not include a class level documentation function.");
				} catch (HeadlessException ex) {
					// Hmm. Whatever's running us doesn't have a head, and we just tested a function
					// that requires a head. Whatever, just skip it and move on. It'll have to be tested
					// manually.
				}
			} catch (Throwable t) {
				uhohs.put(f.getClass().getName(), t);
				t.printStackTrace();
			}
		}

		if(!StaticTest.brokenJunk.isEmpty()) {
			System.err.println("There " + StringUtils.PluralTemplateHelper(StaticTest.brokenJunk.size(), "is %d test that has", "are %d tests that have") + " a failure in extreme circumstances.");
			for(String s : StaticTest.brokenJunk) {
				uhohs.put(s, null);
			}
		}
		if(!uhohs.isEmpty()) {
			StringBuilder b = new StringBuilder();
			for(String key : uhohs.keySet()) {
				b.append(key).append(" threw: ").append(uhohs.get(key)).append("\n");
			}
			String output = ("There was/were " + uhohs.size() + " boilerplate failure(s). Output:\n" + b.toString());
			StreamUtils.GetSystemOut().println(output);
			fail(output);
		}
	}

	@Test
	public void testConstuctToString() {
		assertEquals("hello", new CString("hello", Target.UNKNOWN).toString());
	}

	@Test
	public void testClone() throws Exception {
		Environment env = Static.GenerateStandaloneEnvironment(false);
		CArray c1 = C.Array(C.Void(), C.Void()).clone();
		CBoolean c2 = C.Boolean(true).clone();
		CDouble c4 = C.Double(1).clone();
		CFunction c5 = new CFunction("__", Target.UNKNOWN).clone();
		CInt c6 = C.Int(1).clone();
		CNull c7 = C.Null().clone();
		CString c8 = C.String("").clone();
		Construct c9 = C.Void().clone();
		Command c10 = new Command("/c", Target.UNKNOWN).clone();
		IVariable c12 = new IVariable(Auto.TYPE, "@name", C.Null(), Target.UNKNOWN, env).clone();
		Variable c13 = new Variable("$name", "", false, false, Target.UNKNOWN);
	}

	@Test
	public void testJSONEscapeString() throws MarshalException {
		CArray ca = new CArray(Target.UNKNOWN);
		final Target t = Target.UNKNOWN;
		ca.push(C.Int(1), t);
		ca.push(C.Double(2.2), t);
		ca.push(C.String("string"), t);
		ca.push(C.String("\"Quote\""), t);
		ca.push(C.Boolean(true), t);
		ca.push(C.Boolean(false), t);
		ca.push(C.Null(), t);
		ca.push(C.Void(), t);
		ca.push(new Command("/Command", Target.UNKNOWN), t);
		ca.push(new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN)), t);
		//[1, 2.2, "string", "\"Quote\"", true, false, null, "", "/Command", [1]]
		assertEquals("[1,2.2,\"string\",\"\\\"Quote\\\"\",true,false,null,\"\",\"\\/Command\",[1]]", Construct.json_encode(ca, Target.UNKNOWN));
	}

	@Test
	public void testJSONDecodeString() throws MarshalException {
		CArray ca = new CArray(Target.UNKNOWN);
		ca.push(C.Int(1), Target.UNKNOWN);
		ca.push(C.Double(2.2), Target.UNKNOWN);
		ca.push(C.String("string"), Target.UNKNOWN);
		ca.push(C.String("\"Quote\""), Target.UNKNOWN);
		ca.push(C.Boolean(true), Target.UNKNOWN);
		ca.push(C.Boolean(false), Target.UNKNOWN);
		ca.push(C.Null(), Target.UNKNOWN);
		ca.push(C.Void(), Target.UNKNOWN);
		ca.push(new Command("/Command", Target.UNKNOWN), Target.UNKNOWN);
		ca.push(new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN)), Target.UNKNOWN);
		StaticTest.assertCEquals(ca, Construct.json_decode("[1, 2.2, \"string\", \"\\\"Quote\\\"\", true, false, null, \"\", \"\\/Command\", [1]]", Target.UNKNOWN));
	}

	@Test
	public void testReturnArrayFromProc() throws Exception {
		assertEquals("{1, 2, 3}", SRun("proc(_test, @var, assign(@array, array(1, 2)) array_push(@array, @var) return(@array)) _test(3)", null));
	}

	/*@Test*/ public void testStaticGetLocation() {
		MCWorld fakeWorld = mock(MCWorld.class);
		MCServer fakeServer = mock(MCServer.class);
		when(fakeServer.getWorld("world")).thenReturn(fakeWorld);
		CommandHelperPlugin.myServer = fakeServer;
		CArray ca1 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3));
		CArray ca2 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct("world"));
		CArray ca3 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct(45), C.onstruct(50));
		CArray ca4 = new CArray(Target.UNKNOWN, C.onstruct(1), C.onstruct(2), C.onstruct(3), C.onstruct("world"), C.onstruct(45), C.onstruct(50));
		MCLocation l1 = ObjectGenerator.GetGenerator().location(ca1, fakeWorld, Target.UNKNOWN);
		MCLocation l2 = ObjectGenerator.GetGenerator().location(ca2, fakeWorld, Target.UNKNOWN);
		MCLocation l3 = ObjectGenerator.GetGenerator().location(ca3, fakeWorld, Target.UNKNOWN);
		MCLocation l4 = ObjectGenerator.GetGenerator().location(ca4, fakeWorld, Target.UNKNOWN);
		assertEquals(fakeWorld, l1.getWorld());
		assertEquals(fakeWorld, l2.getWorld());
		assertEquals(fakeWorld, l3.getWorld());
		assertEquals(fakeWorld, l4.getWorld());
		assertEquals(1, l1.getX(), 0.00000000000000001);
		assertEquals(1, l2.getX(), 0.00000000000000001);
		assertEquals(1, l4.getX(), 0.00000000000000001);
		assertEquals(1, l4.getX(), 0.00000000000000001);
		assertEquals(2, l1.getY(), 0.00000000000000001);
		assertEquals(2, l2.getY(), 0.00000000000000001);
		assertEquals(2, l3.getY(), 0.00000000000000001);
		assertEquals(2, l4.getY(), 0.00000000000000001);
		assertEquals(3, l1.getZ(), 0.00000000000000001);
		assertEquals(3, l2.getZ(), 0.00000000000000001);
		assertEquals(3, l3.getZ(), 0.00000000000000001);
		assertEquals(3, l4.getZ(), 0.00000000000000001);
		assertEquals(0, l1.getYaw(), 0.0000000000000000001);
		assertEquals(0, l2.getYaw(), 0.0000000000000000001);
		assertEquals(45, l3.getYaw(), 0.0000000000000000001);
		assertEquals(45, l4.getYaw(), 0.0000000000000000001);
		assertEquals(0, l1.getPitch(), 0.0000000000000000001);
		assertEquals(0, l2.getPitch(), 0.0000000000000000001);
		assertEquals(50, l3.getPitch(), 0.0000000000000000001);
		assertEquals(50, l4.getPitch(), 0.0000000000000000001);
		CommandHelperPlugin.myServer = null;
	}

	@Test
	public void expressionTester() throws ConfigRuntimeException {
		//verify basic usage works
		String eClass = "com.sk89q.worldedit.internal.expression.Expression";
		try {
			Class clazz = Class.forName(eClass);
			Object e = ReflectionUtils.invokeMethod(clazz, null, "compile",
					new Class[]{String.class, String.class, String.class},
					new Object[]{"(x + 2) * y", "x", "y"});
			double d = (double) ReflectionUtils.invokeMethod(clazz, e, "evaluate",
					new Class[]{double.class, double.class}, new Object[]{2, 4});
			assertEquals(16, d, 0.00001);
		} catch (ClassNotFoundException cnf) {
			/* Not much we can really do about this during testing.
			throw new CREPluginInternalException("You are missing a required dependency: " + eClass, Target.UNKNOWN);*/
		} catch (ReflectionUtils.ReflectionException rex) {
			throw new CREPluginInternalException("Your expression was invalidly formatted", Target.UNKNOWN, rex.getCause());
		}
	}

	@Test
	public void testProcScope() throws Exception {
		SRun("proc(_b, assign(@a, 2)) assign(@a, 1) _b() msg(@a)", fakePlayer);
		verify(fakePlayer).sendMessage("1");
	}

	@Test
	public void testCastFromBukkitMCPlayerToBukkitMCCommandSender() throws Exception {
		Player p = mock(Player.class);
		BukkitMCCommandSender c = new BukkitMCCommandSender(new BukkitMCPlayer(p));
	}

	@Test
	public void testReflectDocs() throws Throwable {
		String ret = SRun("reflect_docs('reflect_docs', 'return')", null);
		assertEquals("string", ret);
	}

	@Test
	public void testGetValues() throws Exception {
		try {
			Environment env = Static.GenerateStandaloneEnvironment();
			env = env.cloneAndAdd(new CommandHelperEnvironment());
			GlobalEnv g = env.getEnv(GlobalEnv.class);
			ConnectionMixinFactory.ConnectionMixinOptions options;
			options = new ConnectionMixinFactory.ConnectionMixinOptions();
			options.setWorkingDirectory(new File("."));
			PersistenceNetworkImpl network = new PersistenceNetworkImpl("**=json://persistence.json", new URI("default"), options);
			ReflectionUtils.set(GlobalEnv.class, g, "persistenceNetwork", network);
			Run("store_value('t.test1', 'test')\n"
					+ "store_value('t.test2', 'test')\n"
					+ "store_value('t.test3.third', 'test')\n"
					+ "msg(get_values('t'))", fakePlayer, new MethodScriptComplete() {

				@Override
				public void done(String output) {
					//
				}
			}, env);
			verify(fakePlayer).sendMessage("{t.test1: test, t.test2: test, t.test3.third: test}");
		} finally {
			new File("persistence.json").deleteOnExit();
		}
	}

	@Test
	public void testVoidAndReturnedVoidAreTheExactSame() throws Exception {
		try {
			Environment env = Static.GenerateStandaloneEnvironment(true);
			Mixed returnedVoid = new ArrayHandling.array_insert().exec(Target.UNKNOWN, env,
					C.Array(), C.String(""), C.Int(0));
			Construct voidKeyword = Static.resolveConstruct("void", Target.UNKNOWN);
			assertTrue(returnedVoid == voidKeyword);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw e;
		}
	}

	@Test
	public void testVoidAndReturnedVoidAreSEqualsAndOthers() throws Exception {
		assertEquals("true", SRun("array_insert(array(), '', 0) === void", fakePlayer));
		assertEquals("void", SRun("typeof(array_insert(array(), '', 0))", fakePlayer));
		assertEquals("ms.lang.ClassType", SRun("typeof(typeof(array_insert(array(), '', 0)))", fakePlayer));
	}

	@Test
	public void testFunctionsAreOnlyDefinedOnce() throws Exception {
		Set<String> uhohs = new HashSet<>();
		Set<Class<Function>> set = ClassDiscovery.getDefaultInstance().loadClassesThatExtend(Function.class);

		// Iterate over all function classes, adding a message to "uhohs" if they are double defined.
		Map<String, Class<Function>> funcMap = new HashMap<>();
		for(Class<Function> funcClass : set) {

			// Ignore non-api functions.
			api funcClassApi = funcClass.getAnnotation(api.class);
			if(funcClassApi == null) {
				continue;
			}

			// Get the function name.
			String funcName = ReflectionUtils.instantiateUnsafe(funcClass).getName();

			// Create an identifier string of the function name and its platforms.
			// Format: "funcName\tplatform1\tplatform2\t...platformN". Platforms are sorted to 'compare as sets'.
			StringBuilder idStr = new StringBuilder(funcName);
			Platforms[] platforms = funcClassApi.platform();
			Arrays.sort(platforms, (Platforms p1, Platforms p2) -> p1.toString().compareTo(p2.toString()));
			for(Platforms platform : platforms) {
				idStr.append("\t").append(platform.toString());
			}

			// Store the function in the map by its identifier, adding an message if it is double defined.
			Class<Function> replacedFuncClass = funcMap.put(idStr.toString(), funcClass);
			if(replacedFuncClass != null) {
				uhohs.add(funcName + " is implemented in two places, " + funcClass + " and " + replacedFuncClass);
			}
		}

		// Fail if a function was double defined.
		if(!uhohs.isEmpty()) {
			fail(StringUtils.Join(uhohs, "\n"));
		}
	}

//	@Test
//	public void testBlah() throws Throwable{
//		StaticTest.InstallFakeConvertor(fakePlayer);
//		SRun("async_read('lsmith@localhost:/home/lsmith/test.txt', closure(@ret, @ex,"
//			+ "if(@ex != null, sys_out(@ex), sys_out(@ret))))", null);
//	}
}
