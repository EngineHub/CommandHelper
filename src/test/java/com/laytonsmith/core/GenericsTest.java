package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.UpperBoundConstraint;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GenericsTest {
	public GenericsTest() {

	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() {
	}

	@Test(expected = CREGenericConstraintException.class)
	public void testBasicConstraintValidation() throws Exception {
		// Lower bounds cannot be used on declarations
		new GenericDeclaration(Target.UNKNOWN, new Constraints(ConstraintLocation.DEFINITION,
				new LowerBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE)));
	}

	@Test
	public void testConstraintHasCorrectData() throws Exception {
		UpperBoundConstraint ubc = new UpperBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE);
		assertEquals("upper bound", ubc.getConstraintName());
		assertEquals("T", ubc.getTypeName());
		assertEquals("T extends ms.lang.number", ubc.toString());
	}
}
