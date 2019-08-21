package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigCompileGroupException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.assertCEquals;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.verify;
import static com.laytonsmith.testing.StaticTest.GetOnlinePlayer;
import static com.laytonsmith.testing.StaticTest.SRun;

/**
 *
 *
 */
public class MathTest {

	Target t = Target.UNKNOWN;
	MCServer fakeServer;
	MCPlayer fakePlayer;
	IVariableList varList;
	com.laytonsmith.core.environments.Environment env;

	public MathTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		StaticTest.InstallFakeServerFrontend();
		fakePlayer = GetOnlinePlayer();
		fakeServer = GetFakeServer();

		varList = new IVariableList();
		varList.set(new IVariable(Auto.TYPE, "var", C.onstruct(1), Target.UNKNOWN, env));
		varList.set(new IVariable(Auto.TYPE, "var2", C.onstruct(2.5), Target.UNKNOWN, env));
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
		env.getEnv(GlobalEnv.class).SetVarList(varList);
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testAbs() {
		Math.abs a = new Math.abs();
		assertCEquals(C.onstruct(5), a.exec(Target.UNKNOWN, env, C.onstruct(5)));
		assertCEquals(C.onstruct(3), a.exec(Target.UNKNOWN, env, C.onstruct(-3)));
		assertCEquals(C.onstruct(0), a.exec(Target.UNKNOWN, env, C.onstruct(0)));
		assertCEquals(C.onstruct(3.5), a.exec(Target.UNKNOWN, env, C.onstruct(-3.5)));
	}

	@Test(timeout = 10000)
	public void testAdd() {
		Math.add a = new Math.add();
		assertCEquals(C.onstruct(7), a.exec(Target.UNKNOWN, env, C.onstruct(5), C.onstruct(2)));
		assertCEquals(C.onstruct(6), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(3)));
		assertCEquals(C.onstruct(-4), a.exec(Target.UNKNOWN, env, C.onstruct(-3), C.onstruct(-1)));
		assertCEquals(C.onstruct(1), a.exec(Target.UNKNOWN, env, C.onstruct(1), C.onstruct(0)));
		assertCEquals(C.onstruct(562949953421310L), a.exec(Target.UNKNOWN, env, C.onstruct(281474976710655L), C.onstruct(281474976710655L)));
		assertCEquals(C.onstruct(3.1415), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(0.1415)));
	}

	@Test(timeout = 10000)
	public void testDec() throws Exception {
		Math.dec a = new Math.dec();
		IVariable v = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable(Auto.TYPE, "var", C.onstruct(1), Target.UNKNOWN, env));
		IVariable v2 = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable(Auto.TYPE, "var2", C.onstruct(2.5), Target.UNKNOWN, env));
		assertCEquals(C.onstruct(0), v.ival());
		assertCEquals(C.onstruct(1.5), v2.ival());
		StaticTest.SRun("assign(@var, 0) dec(@var, 2) msg(@var)", fakePlayer);
		verify(fakePlayer).sendMessage("-2");
	}

	@Test(timeout = 10000)
	public void testDivide() {
		Math.divide a = new Math.divide();
		assertCEquals(C.onstruct(2.5), a.exec(Target.UNKNOWN, env, C.onstruct(5), C.onstruct(2)));
		assertCEquals(C.onstruct(1), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(3)));
		assertCEquals(C.onstruct(3), a.exec(Target.UNKNOWN, env, C.onstruct(-3), C.onstruct(-1)));
	}

	@Test(timeout = 10000)
	public void testInc() throws Exception {
		Math.inc a = new Math.inc();
		IVariable v = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable(Auto.TYPE, "var", C.onstruct(1), Target.UNKNOWN, env));
		IVariable v2 = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable(Auto.TYPE, "var2", C.onstruct(2.5), Target.UNKNOWN, env));
		assertCEquals(C.onstruct(2), v.ival());
		assertCEquals(C.onstruct(3.5), v2.ival());
		StaticTest.SRun("assign(@var, 0) inc(@var, 2) msg(@var)", fakePlayer);
		verify(fakePlayer).sendMessage("2");
	}

	@Test(timeout = 10000)
	public void testArrayGetInc() throws Exception {
		StaticTest.SRun("@var = array(1.1); @var[0]++; msg(@var[0])", fakePlayer);
		verify(fakePlayer).sendMessage("2.1");
	}

	@Test(timeout = 10000)
	public void testMod() {
		Math.mod a = new Math.mod();
		assertCEquals(C.onstruct(1), a.exec(Target.UNKNOWN, env, C.onstruct(5), C.onstruct(2)));
		assertCEquals(C.onstruct(0), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(3)));
		assertCEquals(C.onstruct(-1), a.exec(Target.UNKNOWN, env, C.onstruct(-3), C.onstruct(-2)));
	}

	@Test(timeout = 10000)
	public void testMultiply() {
		Math.multiply a = new Math.multiply();
		assertCEquals(C.onstruct(10), a.exec(Target.UNKNOWN, env, C.onstruct(5), C.onstruct(2)));
		assertCEquals(C.onstruct(9), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(3)));
		assertCEquals(C.onstruct(6), a.exec(Target.UNKNOWN, env, C.onstruct(-3), C.onstruct(-2)));
		assertCEquals(C.onstruct(5), a.exec(Target.UNKNOWN, env, C.onstruct(10), C.onstruct(0.5)));
		assertCEquals(C.onstruct(-562949953421311L), a.exec(Target.UNKNOWN, env, C.onstruct(281474976710655L), C.onstruct(281474976710655L)));
		assertCEquals(C.onstruct(5312385410449346020L), a.exec(Target.UNKNOWN, env, C.onstruct(9876543210L), C.onstruct(9876543210L)));
	}

	@Test(timeout = 10000)
	public void testPow() {
		Math.pow a = new Math.pow();
		assertCEquals(C.onstruct(25), a.exec(Target.UNKNOWN, env, C.onstruct(5), C.onstruct(2)));
		assertCEquals(C.onstruct(27), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(3)));
		assertCEquals(C.onstruct(1), a.exec(Target.UNKNOWN, env, C.onstruct(-1), C.onstruct(-2)));
	}

	@Test(timeout = 10000)
	public void testRand1() {
		Math.rand a = new Math.rand();
		for(int i = 0; i < 1000; i++) {
			long j = Static.getInt(a.exec(Target.UNKNOWN, env, C.onstruct(10)), t);
			if(!(j < 10 && j >= 0)) {
				fail("Expected a number between 0 and 10, but got " + j);
			}
			j = Static.getInt(a.exec(Target.UNKNOWN, env, C.onstruct(10), C.onstruct(20)), t);
			if(!(j < 20 && j >= 10)) {
				fail("Expected a number between 10 and 20, but got " + j);
			}
		}
		try {
			a.exec(Target.UNKNOWN, env, C.onstruct(20), C.onstruct(10));
			fail("Didn't expect this test to pass");
		} catch (ConfigRuntimeException e) {
		}
		try {
			a.exec(Target.UNKNOWN, env, C.onstruct(-1));
			fail("Didn't expect this test to pass");
		} catch (ConfigRuntimeException e) {
		}
		try {
			a.exec(Target.UNKNOWN, env, C.onstruct(87357983597853791L));
			fail("Didn't expect this test to pass");
		} catch (ConfigRuntimeException e) {
		}
	}

	@Test
	public void testRand2() throws Exception {
		SRun("assign(@rand, rand()) if(@rand >= 0 && @rand <= 1, msg('pass'), msg('fail'))", fakePlayer);
		verify(fakePlayer).sendMessage("pass");
	}

	@Test(timeout = 10000)
	public void testSubtract() {
		Math.subtract a = new Math.subtract();
		assertCEquals(C.onstruct(3), a.exec(Target.UNKNOWN, env, C.onstruct(5), C.onstruct(2)));
		assertCEquals(C.onstruct(0), a.exec(Target.UNKNOWN, env, C.onstruct(3), C.onstruct(3)));
		assertCEquals(C.onstruct(-1), a.exec(Target.UNKNOWN, env, C.onstruct(-3), C.onstruct(-2)));
		assertCEquals(C.onstruct(3), a.exec(Target.UNKNOWN, env, C.onstruct(3.1415), C.onstruct(0.1415)));
		assertCEquals(C.onstruct(281474976710655L), a.exec(Target.UNKNOWN, env, C.onstruct(562949953421310L), C.onstruct(281474976710655L)));
	}

	@Test(timeout = 10000)
	public void testFloor() {
		Math.floor a = new Math.floor();
		assertCEquals(C.onstruct(3), a.exec(Target.UNKNOWN, env, C.onstruct(3.8415)));
		assertCEquals(C.onstruct(-4), a.exec(Target.UNKNOWN, env, C.onstruct(-3.1415)));
	}

	@Test(timeout = 10000)
	public void testCeil() {
		Math.ceil a = new Math.ceil();
		assertCEquals(C.onstruct(4), a.exec(Target.UNKNOWN, env, C.onstruct(3.1415)));
		assertCEquals(C.onstruct(-3), a.exec(Target.UNKNOWN, env, C.onstruct(-3.1415)));
	}

	@Test(timeout = 10000)
	public void testSqrt() throws Exception {
		assertEquals("3", StaticTest.SRun("sqrt(9)", fakePlayer));
		assertEquals("Test failed", java.lang.Math.sqrt(2), Double.parseDouble(StaticTest.SRun("sqrt(2)", fakePlayer)), .000001);
		try {
			StaticTest.SRun("sqrt(-1)", fakePlayer);
			fail("Did not expect to pass");
		} catch (ConfigCompileException | ConfigCompileGroupException e) {
			//pass
		}
	}

	@Test(timeout = 10000)
	public void testMin() throws Exception {
		assertEquals("-2", StaticTest.SRun("min(2, array(5, 6, 4), -2)", fakePlayer));
	}

	@Test(timeout = 10000)
	public void testMax() throws Exception {
		assertEquals("50", StaticTest.SRun("max(6, 7, array(4, 4, 50), 2, 5)", fakePlayer));
	}

	@Test
	public void testChained() throws Exception {
		assertEquals("8", SRun("2 + 2 + 2 + 2", null));
		assertEquals("20", SRun("2 * 2 + 2 * 2 * 2 + 2 * 2 * 2", null));
	}

	@Test
	public void testRound() throws Exception {
		assertEquals("4.0", SRun("round(4.4)", null));
		assertEquals("5.0", SRun("round(4.5)", null));
		assertEquals("4.6", SRun("round(4.55, 1)", null));
	}

	@Test
	public void testSinh() throws Exception {
		assertEquals("1.1752011936438014", SRun("sinh(1)", null));
		assertEquals("0", SRun("sinh(0)", null));
		assertEquals("6.0502044810397875", SRun("sinh(2.5)", null));
		assertEquals("-6.0502044810397875", SRun("sinh(-2.5)", null));
	}

	@Test
	public void testCosh() throws Exception {
		assertEquals("1.543080634815244", SRun("cosh(1)", null));
		assertEquals("1", SRun("cosh(0)", null));
		assertEquals("6.132289479663686", SRun("cosh(2.5)", null));
		assertEquals("6.132289479663686", SRun("cosh(-2.5)", null));
	}

	@Test
	public void testTanh() throws Exception {
		assertEquals("0.7615941559557649", SRun("tanh(1)", null));
		assertEquals("0", SRun("tanh(0)", null));
		assertEquals("0.9866142981514303", SRun("tanh(2.5)", null));
		assertEquals("-0.9866142981514303", SRun("tanh(-2.5)", null));
	}

	@Test
	public void testClamp() throws Exception {
		assertEquals("8.0", SRun("clamp(8, 1, 10);", null));
		assertEquals("10.0", SRun("clamp(1, 10, 20);", null));
		assertEquals("25.0", SRun("clamp(50, 10, 25);", null));
		assertEquals("5.0", SRun("clamp(5, 20, 10);", null));
		assertEquals("50.0", SRun("clamp(50, 20, 10);", null));
		assertEquals("10.0", SRun("clamp(12, 20, 10);", null));
		assertEquals("20.0", SRun("clamp(19, 20, 10);", null));
		assertEquals("10.0", SRun("clamp(15, 20, 10);", null));
	}
}
