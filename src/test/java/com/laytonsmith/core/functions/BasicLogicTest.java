package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.MCServer;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.C;
import com.laytonsmith.testing.StaticTest;
import static com.laytonsmith.testing.StaticTest.GetFakeServer;
import static com.laytonsmith.testing.StaticTest.GetOnlinePlayer;
import static com.laytonsmith.testing.StaticTest.SRun;
import static com.laytonsmith.testing.StaticTest.TestClassDocs;
import static com.laytonsmith.testing.StaticTest.assertCFalse;
import static com.laytonsmith.testing.StaticTest.assertCTrue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 *
 *
 */
public class BasicLogicTest {

	MCPlayer fakePlayer;
	MCServer fakeServer;
	CArray commonArray;
	CInt argOne;
	CInt argOne2;
	CInt argTwo;
	CInt argNegOne;
	CInt argNegTwo;
	CBoolean cTrue;
	CBoolean cFalse;
	com.laytonsmith.core.environments.Environment env;

	public BasicLogicTest() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
		env = env.cloneAndAdd(new CommandHelperEnvironment());
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
		commonArray = C.Array(C.Null(), C.Int(1), C.String("2"), C.Double(3.0));
		argOne = C.Int(1);
		argOne2 = C.Int(1);
		argTwo = C.Int(2);
		argNegOne = C.Int(-1);
		argNegTwo = C.Int(-2);
		cTrue = C.Boolean(true);
		cFalse = C.Boolean(false);
		fakeServer = GetFakeServer();
		fakePlayer = GetOnlinePlayer(fakeServer);
		env.getEnv(CommandHelperEnvironment.class).SetPlayer(fakePlayer);
	}

	@After
	public void tearDown() {
	}

	@Test(timeout = 10000)
	public void testDocs() {
		TestClassDocs(BasicLogic.docs(), BasicLogic.class);
	}

	@Test(timeout = 10000)
	public void testEquals() throws Exception {

//			 T   F   1   0  -1  '1' '0' '-1' N  {} 'CH'  '' 1.0
//		---------------------------------------------------
//		T    T   F   T   F   T   T   T   T   F   F   T   F   T
//		F    -   T   F   T   F   F   F   F   T   T   F   T   F
//		1    -   -   T   F   F   T   F   F   F   F   F   F   T
//		0    -   -   -   T   F   F   T   F   F   F   F   F   F
//		-1   -   -   -   -   T   F   F   T   F   F   F   F   F
//		'1'  -   -   -   -   -   T   F   F   F   F   F   F   T
//		'0'  -   -   -   -   -   -   T   F   F   F   F   F   F
//		'-1' -   -   -   -   -   -   -   T   F   F   F   F   F
//		N    -   -   -   -   -   -   -   -   T   F   F   F   F
//		{}   -   -   -   -   -   -   -   -   -   T   F   F   F
//		'CH' -   -   -   -   -   -   -   -   -   -   T   F   F
//		''   -   -   -   -   -   -   -   -   -   -   -   T   F
//		1.0  -   -   -   -   -   -   -   -   -   -   -   -   T
		_t("false", "false");
		_f("false", "1");
		_t("false", "0");
		//TODO: Finish

		_t("true", "true");
		_f("true", "false");
		_t("true", "1");
		_f("true", "0");
		_t("true", "-1");
		_t("true", "'1'");
		_t("true", "'0'");
		_t("true", "'-1'");
		_f("true", "null");
		_f("true", "array()");
		_t("true", "'CH'");
		_f("true", "''");
		_t("true", "1.0");

	}

	@Test(timeout = 10000)
	public void testEqualsMulti() throws Exception {
		assertEquals("true", SRun("equals(1, '1', 1.0)", fakePlayer));
		assertEquals("false", SRun("equals('blah', 'blah', 'blarg')", fakePlayer));
	}

	@Test(timeout = 10000)
	public void testEqualsICMulti() throws Exception {
		assertEquals("true", SRun("equals_ic(1, '1', 1.0)", fakePlayer));
		assertEquals("false", SRun("equals_ic('blah', 'blah', 'blarg')", fakePlayer));
		assertEquals("true", SRun("equals_ic('blah', 'Blah', 'BLAH')", fakePlayer));
	}

	public void _t(String val1, String val2) throws Exception {
		try {
			assertEquals("true", SRun("equals(" + val1 + ", " + val2 + ")", null));
		} catch (ConfigCompileException ex) {
			fail(ex.getMessage());
		}
	}

	public void _f(String val1, String val2) throws Exception {
		try {
			assertEquals("false", SRun("equals(" + val1 + ", " + val2 + ")", null));
		} catch (ConfigCompileException ex) {
			fail(ex.getMessage());
		}
	}

	public void testEqualsIC() throws Exception {
		SRun("if(equals_ic('hi', 'HI'), msg('pass'))", fakePlayer);
		SRun("if(equals_ic('hi', 'hi'), msg('pass'))", fakePlayer);
		SRun("if(not(equals_ic('what', 'hi')), msg('pass'))", fakePlayer);
		SRun("if(equals_ic(2, 2), msg('pass'))", fakePlayer);
		SRun("if(not(equals_ic(2, 'hi')), msg('pass'))", fakePlayer);
		verify(fakePlayer, times(5)).sendMessage("pass");
	}

	@Test(timeout = 10000)
	public void testAnd1() throws Exception {
		SRun("if(and(true, true, true), msg(pass))", fakePlayer);
		SRun("if(and(true, true, false), '', msg(pass))", fakePlayer);
		SRun("if(and(true, true), msg(pass))", fakePlayer);
		SRun("if(and(true, false), '', msg(pass))", fakePlayer);
		SRun("if(and(false, false), '', msg(pass))", fakePlayer);
		SRun("if(and(true), msg(pass))", fakePlayer);
		SRun("if(and(false), '', msg(pass))", fakePlayer);
		verify(fakePlayer, times(7)).sendMessage("pass");
	}

	/**
	 * Tests lazy evaluation
	 *
	 * @return
	 * @throws Exception
	 */
	@Test(timeout = 10000)
	public void testAnd2() throws Exception {
		SRun("and(false, msg(lol))", fakePlayer);
		verify(fakePlayer, times(0)).sendMessage("lol");
	}

	@Test(timeout = 10000)
	public void testOr1() throws Exception {
		SRun("if(or(true, true, true), msg(pass))", fakePlayer);
		SRun("if(or(true, true, false), msg(pass))", fakePlayer);
		SRun("if(or(true, true), msg(pass))", fakePlayer);
		SRun("if(or(true, false), msg(pass))", fakePlayer);
		SRun("if(or(false, false), '', msg(pass))", fakePlayer);
		SRun("if(or(true), msg(pass))", fakePlayer);
		SRun("if(or(false), '', msg(pass))", fakePlayer);
		verify(fakePlayer, times(7)).sendMessage("pass");
	}

	@Test(timeout = 10000)
	public void testOr2() throws Exception {
		SRun("or(true, msg(lol))", fakePlayer);
		verify(fakePlayer, times(0)).sendMessage("lol");
	}

	@Test(timeout = 10000)
	public void testNot() throws CancelCommandException {
		BasicLogic.not a = new BasicLogic.not();
		assertCFalse(a.exec(Target.UNKNOWN, env, cTrue));
		assertCTrue(a.exec(Target.UNKNOWN, env, cFalse));
	}

	@Test(timeout = 10000)
	public void testGt() throws CancelCommandException {
		BasicLogic.gt a = new BasicLogic.gt();
		assertCFalse(a.exec(Target.UNKNOWN, env, argOne, argOne2));
		assertCTrue(a.exec(Target.UNKNOWN, env, argTwo, argOne));
		assertCFalse(a.exec(Target.UNKNOWN, env, argOne, argTwo));
		assertCFalse(a.exec(Target.UNKNOWN, env, argNegOne, argOne));
		assertCTrue(a.exec(Target.UNKNOWN, env, argOne, argNegOne));
	}

	@Test(timeout = 10000)
	public void testGte() throws CancelCommandException {
		BasicLogic.gte a = new BasicLogic.gte();
		assertCTrue(a.exec(Target.UNKNOWN, env, argOne, argOne2));
		assertCTrue(a.exec(Target.UNKNOWN, env, argTwo, argOne));
		assertCFalse(a.exec(Target.UNKNOWN, env, argOne, argTwo));
		assertCFalse(a.exec(Target.UNKNOWN, env, argNegOne, argOne));
		assertCTrue(a.exec(Target.UNKNOWN, env, argOne, argNegOne));
	}

	@Test(timeout = 10000)
	public void testLt() throws CancelCommandException {
		BasicLogic.lt a = new BasicLogic.lt();
		assertCFalse(a.exec(Target.UNKNOWN, env, argOne, argOne2));
		assertCFalse(a.exec(Target.UNKNOWN, env, argTwo, argOne));
		assertCTrue(a.exec(Target.UNKNOWN, env, argOne, argTwo));
		assertCTrue(a.exec(Target.UNKNOWN, env, argNegOne, argOne));
		assertCFalse(a.exec(Target.UNKNOWN, env, argOne, argNegOne));
	}

	@Test(timeout = 10000)
	public void testLte() throws CancelCommandException {
		BasicLogic.lte a = new BasicLogic.lte();
		assertCTrue(a.exec(Target.UNKNOWN, env, argOne, argOne2));
		assertCFalse(a.exec(Target.UNKNOWN, env, argTwo, argOne));
		assertCTrue(a.exec(Target.UNKNOWN, env, argOne, argTwo));
		assertCTrue(a.exec(Target.UNKNOWN, env, argNegOne, argOne));
		assertCFalse(a.exec(Target.UNKNOWN, env, argOne, argNegOne));
	}

	@Test(timeout = 10000)
	public void testXor() throws Exception {
		assertEquals("false", SRun("xor(false, false)", null));
		assertEquals("true", SRun("xor(false, true)", null));
		assertEquals("true", SRun("xor(true, false)", null));
		assertEquals("false", SRun("xor(true, true)", null));
	}

	@Test(timeout = 10000)
	public void testNand() throws Exception {
		assertEquals("true", SRun("nand(false, false)", null));
		assertEquals("true", SRun("nand(false, true)", null));
		assertEquals("true", SRun("nand(true, false)", null));
		assertEquals("false", SRun("nand(true, true)", null));
	}

	@Test(timeout = 10000)
	public void testNor() throws Exception {
		assertEquals("true", SRun("nor(false, false)", null));
		assertEquals("false", SRun("nor(false, true)", null));
		assertEquals("false", SRun("nor(true, false)", null));
		assertEquals("false", SRun("nor(true, true)", null));
	}

	@Test(timeout = 10000)
	public void testXnor() throws Exception {
		assertEquals("true", SRun("xnor(false, false)", null));
		assertEquals("false", SRun("xnor(false, true)", null));
		assertEquals("false", SRun("xnor(true, false)", null));
		assertEquals("true", SRun("xnor(true, true)", null));
	}

	@Test(timeout = 10000)
	public void testBitAnd() throws Exception {
		assertEquals("4", SRun("bit_and(4, 7)", null));
		assertEquals("5", SRun("bit_and(7, 5)", null));
		assertEquals("0", SRun("bit_and(1, 4)", null));
	}

	@Test(timeout = 10000)
	public void testBitOr() throws Exception {
		assertEquals("3", SRun("bit_or(1, 3)", null));
		assertEquals("6", SRun("bit_or(2, 4)", null));
	}

	@Test(timeout = 10000)
	public void testBitXor() throws Exception {
		assertEquals("6", SRun("bit_xor(5, 3)", null));
		assertEquals("8", SRun("bit_xor(2, 10)", null));
	}

	@Test(timeout = 10000)
	public void testBitNot() throws Exception {
		assertEquals("-5", SRun("bit_not(4)", null));
	}

	@Test(timeout = 10000)
	public void testLshift() throws Exception {
		assertEquals("16", SRun("lshift(4, 2)", null));
	}

	@Test(timeout = 10000)
	public void testRshift() throws Exception {
		assertEquals("-3", SRun("rshift(-10, 2)", null));
		assertEquals("1", SRun("rshift(3, 1)", null));
	}

	@Test(timeout = 10000)
	public void testUrshift() throws Exception {
		assertEquals("2", SRun("urshift(10, 2)", null));
		assertEquals("4611686018427387901", SRun("urshift(-10, 2)", null));
	}

	@Test(timeout = 10000)
	public void testSequals() throws Exception {
		assertEquals("true", SRun("sequals(1, 1)", null));
		assertEquals("false", SRun("sequals(1, '1')", null));
		assertEquals("false", SRun("sequals(1, '2')", null));
	}

	@Test
	public void testRefEquals1() throws Exception {
		SRun("@a = array(1, 2, 3)\n" //Same reference
				+ "@b = @a\n"
				+ "msg(ref_equals(@a, @b))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testRefEquals2() throws Exception {
		SRun("@a = array(1, 2, 3)\n" //Cloned array
				+ "@b = @a[]\n"
				+ "msg(ref_equals(@a, @b))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testRefEquals3() throws Exception {
		SRun("@a = array(1, 2, 3)\n" //Duplicated array
				+ "@b = array(1, 2, 3)\n"
				+ "msg(ref_equals(@a, @b))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testRefEquals4() throws Exception {
		SRun("@a = 1\n" //Primitives; same
				+ "@b = 1\n"
				+ "msg(ref_equals(@a, @b))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testRefEquals5() throws Exception {
		SRun("@a = 1\n" //Primitives; different
				+ "@b = 2\n"
				+ "msg(ref_equals(@a, @b))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testSEqualsic1() throws Exception {
		SRun("msg(sequals_ic(1, '1'))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testSEqualsic2() throws Exception {
		SRun("msg(sequals_ic('hello', 'HELLO'))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	@Test
	public void testSEqualsic3() throws Exception {
		SRun("msg(sequals_ic('false', true))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	@Test
	public void testDor() throws Exception {
		SRun("msg(dor('', 'b'))", fakePlayer);
		verify(fakePlayer).sendMessage("b");
	}

	@Test
	public void testDor2() throws Exception {
		SRun("msg(typeof(dor('', null)))", fakePlayer);
		verify(fakePlayer).sendMessage("null");
	}

	@Test
	public void testDand() throws Exception {
		SRun("msg(typeof(dand('a', 'b', false)))", fakePlayer);
		verify(fakePlayer).sendMessage("ms.lang.boolean");
	}
}
