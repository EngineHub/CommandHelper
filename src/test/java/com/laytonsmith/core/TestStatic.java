package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.C;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 *
 */
public class TestStatic {

	Target t = Target.UNKNOWN;
	Environment env;

	public TestStatic() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@Test
	public void testGetNumber() {
		assertEquals(1.0, ArgumentValidation.getNumber(C.String("1.0"), t, env), 0.0);
		assertEquals(1.0, ArgumentValidation.getNumber(C.String("1"), t, env), 0.0);
		assertEquals(1.0, ArgumentValidation.getNumber(C.Int(1), t, env), 0.0);
		assertEquals(1.0, ArgumentValidation.getNumber(C.Double(1.0), t, env), 0.0);
	}

	@Test
	public void testGetDouble() {
		assertEquals(1.0, ArgumentValidation.getDouble(C.String("1.0"), t, env), 0.0);
		assertEquals(1.0, ArgumentValidation.getDouble(C.String("1"), t, env), 0.0);
		assertEquals(1.0, ArgumentValidation.getDouble(C.Int(1), t, env), 0.0);
		assertEquals(1.0, ArgumentValidation.getDouble(C.Double(1.0), t, env), 0.0);
	}

	@Test
	public void testGetInt() {
		assertEquals(1, ArgumentValidation.getInt(C.Int(1), t, env));
		assertEquals(1, ArgumentValidation.getInt(C.String("1"), t, env));
		try {
			ArgumentValidation.getInt(C.Double(1.0), t, env);
			fail("Should not have been able to parse 1.0 as an int");
		} catch (ConfigRuntimeException e) {
			// Test Passed.
		}
	}

	@Test
	public void testGetBoolean() {
		assertTrue(ArgumentValidation.getBooleanish(C.Boolean(true), Target.UNKNOWN, env));
		assertTrue(ArgumentValidation.getBooleanish(C.String("non-empty string"), Target.UNKNOWN, env));
		assertFalse(ArgumentValidation.getBooleanish(C.String(""), Target.UNKNOWN, env));
		assertTrue(ArgumentValidation.getBooleanish(C.Int(1), Target.UNKNOWN, env));
		assertFalse(ArgumentValidation.getBooleanish(C.Int(0), Target.UNKNOWN, env));
	}

	@Test
	public void testAnyDoubles() {
		assertTrue(ArgumentValidation.anyDoubles(env, C.Int(0), C.Int(1), C.Double(1)));
		assertFalse(ArgumentValidation.anyDoubles(env, C.Int(1)));
	}

	@Test
	public void testAnyStrings() {
		assertTrue(ArgumentValidation.anyStrings(env, C.Int(0), C.Int(1), C.String("")));
		assertFalse(ArgumentValidation.anyStrings(env, C.Int(1)));
	}

	@Test
	public void testAnyBooleans() {
		assertTrue(ArgumentValidation.anyBooleans(env, C.Int(0), C.Int(1), C.Boolean(true)));
		assertFalse(ArgumentValidation.anyBooleans(env, C.Int(1)));
	}

	@Test
	public void testGetLogger() {
		assertNotNull(Static.getLogger());
	}

	@Test
	public void testResolveConstruct() {
		assertTrue(Static.resolveConstruct("1", Target.UNKNOWN, env) instanceof CInt);
		assertTrue(Static.resolveConstruct("true", Target.UNKNOWN, env) instanceof CBoolean);
		assertTrue(Static.resolveConstruct("false", Target.UNKNOWN, env) instanceof CBoolean);
		assertTrue(Static.resolveConstruct("null", Target.UNKNOWN, env) instanceof CNull);
		assertTrue(Static.resolveConstruct("1.1", Target.UNKNOWN, env) instanceof CDouble);
		assertTrue(Static.resolveConstruct("astring", Target.UNKNOWN, env) instanceof CString);
		assertTrue(Static.resolveConstruct("string", Target.UNKNOWN, env) instanceof CClassType);
	}

}
