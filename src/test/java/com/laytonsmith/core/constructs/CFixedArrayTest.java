package com.laytonsmith.core.constructs;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CFixedArrayTest {

	public CFixedArrayTest() {
	}

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
	public void testBasic1() {
		CFixedArray fa = new CFixedArray(Target.UNKNOWN, CInt.TYPE, 10);
		assertEquals(10, fa.size());
		assertEquals(false, fa.canBeAssociative());
		assertEquals(CNull.NULL, fa.get(0, Target.UNKNOWN));
		fa.set(0, new CInt(10, Target.UNKNOWN), Target.UNKNOWN);
		assertEquals(10, ((CInt) fa.get(0, Target.UNKNOWN)).val);
	}
}
