package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CFixedArrayTest {

	Environment env;

	public CFixedArrayTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() throws Exception{
		env = Static.GenerateStandaloneEnvironment();
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testBasic1() {
		CFixedArray fa = new CFixedArray(Target.UNKNOWN, CInt.TYPE, 10);
		assertEquals(10, fa.size());
		assertEquals(false, fa.canBeAssociative());
		assertEquals(CNull.NULL, fa.get(0, Target.UNKNOWN, env));
		fa.set(0, new CInt(10, Target.UNKNOWN), Target.UNKNOWN);
		assertEquals(10, ((CInt) fa.get(0, Target.UNKNOWN, env)).val);
	}
}
