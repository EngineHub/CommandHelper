

package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.IVariableList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Math;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Layton
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
        varList.set(new IVariable("var", C.onstruct(1), Target.UNKNOWN));
        varList.set(new IVariable("var2", C.onstruct(2.5), Target.UNKNOWN));
		env = Static.GenerateStandaloneEnvironment();
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
    public void testDec() throws ConfigCompileException {
        Math.dec a = new Math.dec();
        IVariable v = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable("var", C.onstruct(1), Target.UNKNOWN));
        IVariable v2 = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable("var2", C.onstruct(2.5), Target.UNKNOWN));
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
    public void testInc() throws ConfigCompileException {
        Math.inc a = new Math.inc();
        IVariable v = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable("var", C.onstruct(1), Target.UNKNOWN));
        IVariable v2 = (IVariable) a.exec(Target.UNKNOWN, env, new IVariable("var2", C.onstruct(2.5), Target.UNKNOWN));
        assertCEquals(C.onstruct(2), v.ival());
        assertCEquals(C.onstruct(3.5), v2.ival());
        StaticTest.SRun("assign(@var, 0) inc(@var, 2) msg(@var)", fakePlayer);
        verify(fakePlayer).sendMessage("2");
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
        for (int i = 0; i < 1000; i++) {
            long j = Static.getInt(a.exec(Target.UNKNOWN, env, C.onstruct(10)), t);
            if (!(j < 10 && j >= 0)) {
                fail("Expected a number between 0 and 10, but got " + j);
            }
            j = Static.getInt(a.exec(Target.UNKNOWN, env, C.onstruct(10), C.onstruct(20)), t);
            if (!(j < 20 && j >= 10)) {
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
	public void testRand2() throws Exception{
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
    public void testSqrt() throws ConfigCompileException {
        assertEquals("3", StaticTest.SRun("sqrt(9)", fakePlayer));
        assertEquals("Test failed", java.lang.Math.sqrt(2), Double.parseDouble(StaticTest.SRun("sqrt(2)", fakePlayer)), .000001);
        try {
            StaticTest.SRun("sqrt(-1)", fakePlayer);
            fail("Did not expect to pass");
        } catch (ConfigCompileException e) {
            //pass
        }
    }

    @Test(timeout = 10000)
    public void testMin() throws ConfigCompileException {
        assertEquals("-2", StaticTest.SRun("min(2, array(5, 6, 4), -2)", fakePlayer));
    }

    @Test(timeout = 10000)
    public void testMax() throws ConfigCompileException {
        assertEquals("50", StaticTest.SRun("max(6, 7, array(4, 4, 50), 2, 5)", fakePlayer));
    }
	
	@Test
	public void testChained() throws Exception{
		assertEquals("8", SRun("2 + 2 + 2 + 2", null));
		assertEquals("20", SRun("2 * 2 + 2 * 2 * 2 + 2 * 2 * 2", null));
	}
	
	@Test
	public void testRound() throws Exception {
		assertEquals("4", SRun("round(4.4)", null));
		assertEquals("5", SRun("round(4.5)", null));
		assertEquals("4.6", SRun("round(4.55, 1)", null));
	}
}
