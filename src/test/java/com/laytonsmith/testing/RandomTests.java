package com.laytonsmith.testing;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.abstraction.MCWorld;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.MethodScriptComplete;
import com.laytonsmith.core.ObjectGenerator;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CFunction;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Command;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.Variable;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.MarshalException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.functions.FunctionBase;
import com.laytonsmith.core.functions.FunctionList;
import com.laytonsmith.persistence.PersistenceNetwork;
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

import static com.laytonsmith.testing.StaticTest.Run;
import static com.laytonsmith.testing.StaticTest.SRun;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 *
 *
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(Static.class)
public class RandomTests {

	MCPlayer fakePlayer;

	@Before
	public void setUp() throws Exception {
		fakePlayer = StaticTest.GetOnlinePlayer();
		StaticTest.InstallFakeConvertor(fakePlayer);
	}
	private static Set<String> testedFunctions = new TreeSet<String>();

	/**
	 * This function automatically tests all the boilerplate portions of all
	 * functions. Note that this can be disabled in the StaticTest class, so
	 * that high quality test coverage can be measured.
	 */
	@Test
	public void testAllBoilerplate() {
		Map<String, Throwable> uhohs = new HashMap<String, Throwable>();
		String[] requiredMethods = new String[]{"toString", "equals", "hashCode"};
		//Ensure that all the abstraction objects overloaded
		StaticTest.InstallFakeServerFrontend();
		outer:
		for (Class c : ClassDiscovery.getDefaultInstance().loadClassesThatExtend(AbstractionObject.class)) {
			inner:
			for (Class inter : c.getInterfaces()) {
				for (Class extended : inter.getInterfaces()) {
					if (extended == AbstractionObject.class && !c.isInterface()) {
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
			for (String required : requiredMethods) {
				for (Method method : methods) {
					if (method.getName().equals(required)) {
						continue required;
					}
				}
				uhohs.put(c.getName() + " " + required, new NoSuchMethodException(c.getSimpleName() + " does not define " + required));
			}
		}
		Set<String> classDocs = new TreeSet<String>();

		for (FunctionBase f : FunctionList.getFunctionList(null)) {
			try {
				if (testedFunctions.contains(f.getName())) {
					continue;
				}
				testedFunctions.add(f.getName());

				StaticTest.TestBoilerplate(f, f.getName());
				Class upper = f.getClass().getEnclosingClass();
				if (upper == null) {
					fail(f.getName() + " is not enclosed in an upper class.");
				}
				try {
					Method m = upper.getMethod("docs", new Class[]{});
					try {
						String docs = m.invoke(null, new Object[]{}).toString();
						if (!classDocs.contains(docs)) {
							StaticTest.TestClassDocs(docs, upper);
							classDocs.add(docs);
						}
					} catch (NullPointerException ex) {
						fail(upper.getName() + "'s docs function should be static");
					}
				} catch (IllegalAccessException ex) {
					Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
				} catch (IllegalArgumentException ex) {
					Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvocationTargetException ex) {
					fail(upper.getName() + " throws an exception!");
				} catch (NoSuchMethodException ex) {
					fail(upper.getName() + " does not include a class level documentation function.");
				} catch (SecurityException ex) {
					Logger.getLogger(RandomTests.class.getName()).log(Level.SEVERE, null, ex);
				}
			} catch (Throwable t) {
				uhohs.put(f.getClass().getName(), t);
				t.printStackTrace();
			}
		}
		if (!StaticTest.brokenJunk.isEmpty()) {
			System.err.println("There " + StringUtils.PluralTemplateHelper(StaticTest.brokenJunk.size(), "is %d test that has", "are %d tests that have") + " a failure in extreme circumstances.");
		}
		if (!uhohs.isEmpty()) {
			StringBuilder b = new StringBuilder();
			for (String key : uhohs.keySet()) {
				b.append(key).append(" threw: ").append(uhohs.get(key)).append("\n");
			}
			String output = ("There was/were " + uhohs.size() + " boilerplate failure(s). Output:\n" + b.toString());
			System.out.println(output);
			fail(output);
		}
	}

	@Test
	public void testConstuctToString() {
		assertEquals("hello", new CString("hello", Target.UNKNOWN).toString());
	}

	@Test
	public void testClone() throws CloneNotSupportedException {
		CArray c1 = C.Array(C.Void(), C.Void()).clone();
		CBoolean c2 = C.Boolean(true).clone();
		CDouble c4 = C.Double(1).clone();
		CFunction c5 = new CFunction("", Target.UNKNOWN).clone();
		CInt c6 = C.Int(1).clone();
		CNull c7 = C.Null().clone();
		CString c8 = C.String("").clone();
		CVoid c9 = C.Void().clone();
		Command c10 = new Command("/c", Target.UNKNOWN).clone();
		IVariable c12 = new IVariable(CClassType.AUTO, "@name", C.Null(), Target.UNKNOWN).clone();
		Variable c13 = new Variable("$name", "", false, false, Target.UNKNOWN);
	}

	@Test
	public void testJSONEscapeString() throws MarshalException {
		CArray ca = new CArray(Target.UNKNOWN);
		ca.push(C.Int(1));
		ca.push(C.Double(2.2));
		ca.push(C.String("string"));
		ca.push(C.String("\"Quote\""));
		ca.push(C.Boolean(true));
		ca.push(C.Boolean(false));
		ca.push(C.Null());
		ca.push(C.Void());
		ca.push(new Command("/Command", Target.UNKNOWN));
		ca.push(new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN)));
		//[1, 2.2, "string", "\"Quote\"", true, false, null, "", "/Command", [1]]
		assertEquals("[1,2.2,\"string\",\"\\\"Quote\\\"\",true,false,null,\"\",\"\\/Command\",[1]]", Construct.json_encode(ca, Target.UNKNOWN));
	}

	@Test
	public void testJSONDecodeString() throws MarshalException {
		CArray ca = new CArray(Target.UNKNOWN);
		ca.push(C.Int(1));
		ca.push(C.Double(2.2));
		ca.push(C.String("string"));
		ca.push(C.String("\"Quote\""));
		ca.push(C.Boolean(true));
		ca.push(C.Boolean(false));
		ca.push(C.Null());
		ca.push(C.Void());
		ca.push(new Command("/Command", Target.UNKNOWN));
		ca.push(new CArray(Target.UNKNOWN, new CInt(1, Target.UNKNOWN)));
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
					new Class[] { String.class, String.class, String.class },
					new Object[] { "(x + 2) * y", "x", "y" });
			double d = (double) ReflectionUtils.invokeMethod(clazz, e, "evaluate",
					new Class[] { double.class, double.class }, new Object[] { 2, 4 });
			assertEquals(16, d, 0.00001);
		} catch (ClassNotFoundException cnf) {
			/* Not much we can really do about this during testing.
			throw new ConfigRuntimeException("You are missing a required dependency: " + eClass,
					ExceptionType.PluginInternalException, Target.UNKNOWN);*/
		} catch (ReflectionUtils.ReflectionException rex) {
			throw new ConfigRuntimeException("Your expression was invalidly formatted",
					ExceptionType.PluginInternalException, Target.UNKNOWN, rex.getCause());
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
		try{
			Environment env = Static.GenerateStandaloneEnvironment();
			GlobalEnv g = env.getEnv(GlobalEnv.class);
			ConnectionMixinFactory.ConnectionMixinOptions options;
			options = new ConnectionMixinFactory.ConnectionMixinOptions();
			options.setWorkingDirectory(new File("."));
			PersistenceNetwork network = new PersistenceNetwork("**=json://persistence.json", new URI("default"), options);
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

//    @Test
//    public void testBlah() throws Throwable{
//	    StaticTest.InstallFakeConvertor(fakePlayer);
//	    SRun("async_read('lsmith@localhost:/home/lsmith/test.txt', closure(@ret, @ex,"
//		    + "if(@ex != null, sys_out(@ex), sys_out(@ret))))", null);
//    }
}
