package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.testing.AbstractIntegrationTest;
import com.laytonsmith.testing.C;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class TestStatic extends AbstractIntegrationTest {

	Target t = Target.UNKNOWN;

	public TestStatic() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@Test
	public void testGetNumber() {
		assertEquals(1.0, ArgumentValidation.getNumber(C.String("1.0"), t), 0.0);
		assertEquals(1.0, ArgumentValidation.getNumber(C.String("1"), t), 0.0);
		assertEquals(1.0, ArgumentValidation.getNumber(C.Int(1), t), 0.0);
		assertEquals(1.0, ArgumentValidation.getNumber(C.Double(1.0), t), 0.0);
	}

	@Test
	public void testGetDouble() {
		assertEquals(1.0, ArgumentValidation.getDouble(C.String("1.0"), t), 0.0);
		assertEquals(1.0, ArgumentValidation.getDouble(C.String("1"), t), 0.0);
		assertEquals(1.0, ArgumentValidation.getDouble(C.Int(1), t), 0.0);
		assertEquals(1.0, ArgumentValidation.getDouble(C.Double(1.0), t), 0.0);
	}

	@Test
	public void testGetInt() {
		assertEquals(1, ArgumentValidation.getInt(C.Int(1), t));
		assertEquals(1, ArgumentValidation.getInt(C.String("1"), t));
		try {
			ArgumentValidation.getInt(C.Double(1.0), t);
			fail("Should not have been able to parse 1.0 as an int");
		} catch (ConfigRuntimeException e) {
			// Test Passed.
		}
	}

	@Test
	public void testGetBoolean() {
		assertTrue(ArgumentValidation.getBooleanish(C.Boolean(true), Target.UNKNOWN));
		assertTrue(ArgumentValidation.getBooleanish(C.String("non-empty string"), Target.UNKNOWN));
		assertFalse(ArgumentValidation.getBooleanish(C.String(""), Target.UNKNOWN));
		assertTrue(ArgumentValidation.getBooleanish(C.Int(1), Target.UNKNOWN));
		assertFalse(ArgumentValidation.getBooleanish(C.Int(0), Target.UNKNOWN));
	}

	@Test
	public void testAnyDoubles() {
		assertTrue(ArgumentValidation.anyDoubles(C.Int(0), C.Int(1), C.Double(1)));
		assertFalse(ArgumentValidation.anyDoubles(C.Int(1)));
	}

	@Test
	public void testAnyStrings() {
		assertTrue(ArgumentValidation.anyStrings(C.Int(0), C.Int(1), C.String("")));
		assertFalse(ArgumentValidation.anyStrings(C.Int(1)));
	}

	@Test
	public void testAnyBooleans() {
		assertTrue(ArgumentValidation.anyBooleans(C.Int(0), C.Int(1), C.Boolean(true)));
		assertFalse(ArgumentValidation.anyBooleans(C.Int(1)));
	}

	@Test
	public void testGetLogger() {
		assertNotNull(Static.getLogger());
	}

	@Test
	public void testResolveConstruct() {
		assertTrue(Static.resolveConstruct("1", Target.UNKNOWN) instanceof CInt);
		assertTrue(Static.resolveConstruct("true", Target.UNKNOWN) instanceof CBoolean);
		assertTrue(Static.resolveConstruct("false", Target.UNKNOWN) instanceof CBoolean);
		assertTrue(Static.resolveConstruct("null", Target.UNKNOWN) instanceof CNull);
		assertTrue(Static.resolveConstruct("1.1", Target.UNKNOWN) instanceof CDouble);
		assertTrue(Static.resolveConstruct("astring", Target.UNKNOWN) instanceof CString);
		assertTrue(Static.resolveConstruct("string", Target.UNKNOWN) instanceof CClassType);
		assertTrue(getResolveConstructLong("0xFF") == 0xFF);
		assertTrue(getResolveConstructLong("0xABCDEF0123456789") == 0xABCDEF0123456789L); // All chars.
		assertTrue(getResolveConstructLong("0xFFAFFFFFFFF0FFFF") == 0xFFAFFFFFFFF0FFFFL);
		assertTrue(getResolveConstructLong("0xFFFFFFFFFFFFFFFF") == 0xFFFFFFFFFFFFFFFFL); // Max value.
		assertTrue(getResolveConstructLong("0b100") == 0b100);
		assertTrue(getResolveConstructLong("0b1111011111111011111111111011111111111111111111110111111111111110")
				== 0b1111011111111011111111111011111111111111111111110111111111111110L);
		assertTrue(getResolveConstructLong("0b1111111111111111111111111111111111111111111111111111111111111111")
				== 0b1111111111111111111111111111111111111111111111111111111111111111L); // Max value.
		assertTrue(getResolveConstructLong("0o76543210") == 076543210L); // All chars.
		assertTrue(getResolveConstructLong("0o1737745677477125767277") == 01737745677477125767277L);
		assertTrue(getResolveConstructLong("0o1777777777777777777777") == 01777777777777777777777L); // Max value.
	}

	private static long getResolveConstructLong(String val) {
		return ((CInt) Static.resolveConstruct(val, Target.UNKNOWN)).getInt();
	}

}
