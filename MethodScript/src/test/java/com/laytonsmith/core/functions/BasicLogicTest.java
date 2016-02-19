

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
    CInt arg1_1;
    CInt arg1_2;
    CInt arg2_1;
    CInt argn1_1;
    CInt argn2_1;
    CBoolean _true;
    CBoolean _false;
    com.laytonsmith.core.environments.Environment env;

    public BasicLogicTest() throws Exception{
		env = Static.GenerateStandaloneEnvironment();
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
        arg1_1 = C.Int(1);
        arg1_2 = C.Int(1);
        arg2_1 = C.Int(2);
        argn1_1 = C.Int(-1);
        argn2_1 = C.Int(-2);
        _true = C.Boolean(true);
        _false = C.Boolean(false);
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
        BasicLogic.equals e = new BasicLogic.equals();

//             T   F   1   0  -1  '1' '0' '-1' N  {} 'CH'  '' 1.0
//        ---------------------------------------------------
//        T    T   F   T   F   T   T   T   T   F   F   T   F   T
//        F    -   T   F   T   F   F   F   F   T   T   F   T   F
//        1    -   -   T   F   F   T   F   F   F   F   F   F   T
//        0    -   -   -   T   F   F   T   F   F   F   F   F   F
//        -1   -   -   -   -   T   F   F   T   F   F   F   F   F
//        '1'  -   -   -   -   -   T   F   F   F   F   F   F   T
//        '0'  -   -   -   -   -   -   T   F   F   F   F   F   F
//        '-1' -   -   -   -   -   -   -   T   F   F   F   F   F
//        N    -   -   -   -   -   -   -   -   T   F   F   F   F
//        {}   -   -   -   -   -   -   -   -   -   T   F   F   F
//        'CH' -   -   -   -   -   -   -   -   -   -   T   F   F
//        ''   -   -   -   -   -   -   -   -   -   -   -   T   F
//        1.0  -   -   -   -   -   -   -   -   -   -   -   -   T

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
        assertCFalse(a.exec(Target.UNKNOWN, env, _true));
        assertCTrue(a.exec(Target.UNKNOWN, env, _false));
    }

    @Test(timeout = 10000)
    public void testGt() throws CancelCommandException {
        BasicLogic.gt a = new BasicLogic.gt();
        assertCFalse(a.exec(Target.UNKNOWN, env, arg1_1, arg1_2));
        assertCTrue(a.exec(Target.UNKNOWN, env, arg2_1, arg1_1));
        assertCFalse(a.exec(Target.UNKNOWN, env, arg1_1, arg2_1));
        assertCFalse(a.exec(Target.UNKNOWN, env, argn1_1, arg1_1));
        assertCTrue(a.exec(Target.UNKNOWN, env, arg1_1, argn1_1));
    }

    @Test(timeout = 10000)
    public void testGte() throws CancelCommandException {
        BasicLogic.gte a = new BasicLogic.gte();
        assertCTrue(a.exec(Target.UNKNOWN, env, arg1_1, arg1_2));
        assertCTrue(a.exec(Target.UNKNOWN, env, arg2_1, arg1_1));
        assertCFalse(a.exec(Target.UNKNOWN, env, arg1_1, arg2_1));
        assertCFalse(a.exec(Target.UNKNOWN, env, argn1_1, arg1_1));
        assertCTrue(a.exec(Target.UNKNOWN, env, arg1_1, argn1_1));
    }

    @Test(timeout = 10000)
    public void testLt() throws CancelCommandException {
        BasicLogic.lt a = new BasicLogic.lt();
        assertCFalse(a.exec(Target.UNKNOWN, env, arg1_1, arg1_2));
        assertCFalse(a.exec(Target.UNKNOWN, env, arg2_1, arg1_1));
        assertCTrue(a.exec(Target.UNKNOWN, env, arg1_1, arg2_1));
        assertCTrue(a.exec(Target.UNKNOWN, env, argn1_1, arg1_1));
        assertCFalse(a.exec(Target.UNKNOWN, env, arg1_1, argn1_1));
    }

    @Test(timeout = 10000)
    public void testLte() throws CancelCommandException {
        BasicLogic.lte a = new BasicLogic.lte();
        assertCTrue(a.exec(Target.UNKNOWN, env, arg1_1, arg1_2));
        assertCFalse(a.exec(Target.UNKNOWN, env, arg2_1, arg1_1));
        assertCTrue(a.exec(Target.UNKNOWN, env, arg1_1, arg2_1));
        assertCTrue(a.exec(Target.UNKNOWN, env, argn1_1, arg1_1));
        assertCFalse(a.exec(Target.UNKNOWN, env, arg1_1, argn1_1));
    }

    @Test(timeout = 10000)
    public void testIf() throws Exception {
        BasicLogic._if a = new BasicLogic._if();
        SRun("if(true, msg('correct'), msg('incorrect'))", fakePlayer);
        SRun("if(false, msg('incorrect'), msg('correct'))", fakePlayer);
        verify(fakePlayer, times(2)).sendMessage("correct");
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

    @Test
    public void testIfelse() throws Exception {
        assertEquals("3", SRun("ifelse("
                + "false, 1,"
                + "false, 2,"
                + "true, 3,"
                + "true, 4,"
                + "false, 5)", null));
        assertEquals("4", SRun("ifelse("
                + "false, 1,"
                + "false, 2,"
                + "false, 3,"
                + "add(2, 2))", null));
    }

    @Test(timeout = 10000)
    public void testSwitch() throws Exception {
        assertEquals("correct", SRun("switch(3,"
                + "1, wrong,"
                + "2, wrong,"
                + "3, correct,"
                + "4, wrong)", null));
        assertEquals("correct", SRun("switch(4,"
                + "1, wrong,"
                + "2, wrong,"
                + "3, wrong,"
                + "correct)", null));
    }

    @Test public void testSwitch2() throws Exception{
        SRun("switch(2, 1, msg('nope'), 2, msg('yep'))", fakePlayer);
        verify(fakePlayer).sendMessage("yep");
    }

    @Test public void testSwitch3() throws Exception{
        SRun("assign(@args, 'test')" +
                "switch(@args," +
                    "'test'," +
                        "msg('test')," +
                    "msg('default')" +
                ")",
                fakePlayer);
        verify(fakePlayer).sendMessage("test");
    }

    @Test(timeout = 10000)
    public void testSwitchWithArray() throws Exception {
        assertEquals("correct", SRun("switch(3,"
                + "array(1, 2), wrong,"
                + "array(3, 4), correct,"
                + "5, wrong)", null));
    }

    @Test(timeout = 10000)
    public void testSequals() throws Exception {
        assertEquals("true", SRun("sequals(1, 1)", null));
        assertEquals("false", SRun("sequals(1, '1')", null));
        assertEquals("false", SRun("sequals(1, '2')", null));
    }

    @Test(timeout = 10000)
    public void testIf2() throws Exception {
        SRun("assign(@true, true)\n"
                + "if(@true, msg('Hello World!'))", fakePlayer);
        verify(fakePlayer).sendMessage("Hello World!");
    }

	@Test
	public void testSwitchWithRange() throws Exception{
		SRun("switch(dyn(1)){"
				+ "case 0..5: msg('yes')"
				+ "case 6..10: msg('no')"
				+ "}"
				+ "switch(dyn(1)){"
				+ "case 1..5: msg('yes')"
				+ "}"
				+ "switch(dyn(1)){"
				+ "case 5..0: msg('yes')"
				+ "}"
				+ "switch(dyn(1)){"
				+ "case 1..2: case 3..4: msg('yes')"
				+ "}", fakePlayer);
		verify(fakePlayer, times(4)).sendMessage("yes");
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

	public void testSEqualsic1() throws Exception {
		SRun("msg(sequals_ic(1, '1'))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}

	public void testSEqualsic2() throws Exception {
		SRun("msg(sequals_ic('hello', 'HELLO'))", fakePlayer);
		verify(fakePlayer).sendMessage("true");
	}

	public void testSEqualsic3() throws Exception {
		SRun("msg(sequals_ic('false', true))", fakePlayer);
		verify(fakePlayer).sendMessage("false");
	}
}
