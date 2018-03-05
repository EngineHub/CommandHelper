package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.LogicUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 *
 */
public class LogicUtilsTest {

	public LogicUtilsTest() {
	}

	Object a = new Object();
	Object b = new Object();
	Object c = new Object();
	Object d = new Object();

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testEqualsAny() {
		assertTrue(LogicUtils.get(a).equalsAny(a));
		assertTrue(LogicUtils.get(null).equalsAny(a, b, c, d, null));
		assertTrue(LogicUtils.get(a).equalsAny(d, c, b, a));
		assertFalse(LogicUtils.get(a).equalsAny(b, c, d));
	}

	@Test
	public void testEqualsNone() {
		assertFalse(LogicUtils.get(a).equalsNone(a));
		assertFalse(LogicUtils.get(null).equalsNone(a, b, c, d, null));
		assertFalse(LogicUtils.get(a).equalsNone(d, c, b, a));
		assertTrue(LogicUtils.get(a).equalsNone(b, c, d));
	}
}
