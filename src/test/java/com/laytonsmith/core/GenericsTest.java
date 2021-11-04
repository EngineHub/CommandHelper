package com.laytonsmith.core;

import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.ExactType;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.UpperBoundConstraint;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
		new GenericDeclaration(Target.UNKNOWN, new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
				new LowerBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE)));
	}

	@Test
	public void testConstraintHasCorrectData() throws Exception {
		UpperBoundConstraint ubc = new UpperBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE);
		assertEquals("upper bound", ubc.getConstraintName());
		assertEquals("T", ubc.getTypeName());
		assertEquals("T extends ms.lang.number", ubc.toString());
	}

	@Test
	public void testLHSToStringIsCorrect() throws Exception {
		// ? extends array<int>
		LeftHandGenericUse lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new UpperBoundConstraint(Target.UNKNOWN, "?", CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN,
						GenericParameters.start(CArray.TYPE.getGenericDeclaration())
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new ExactType(Target.UNKNOWN, CInt.TYPE))))
								.build()))));
		assertEquals("? extends ms.lang.array<ms.lang.int>", lhgu.toString());

		// ? extends array<? extends int>

		lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new UpperBoundConstraint(Target.UNKNOWN, "?", CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN,
						GenericParameters.start(CArray.TYPE.getGenericDeclaration())
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new UpperBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE))))
								.build()))));
		assertEquals("? extends ms.lang.array<? extends ms.lang.int>", lhgu.toString());

		GenericParameters params = GenericParameters.start(CArray.TYPE.getGenericDeclaration())
				.addParameter(CArray.TYPE, lhgu).build();

		CClassType array = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, params);

		assertEquals("ms.lang.array<? extends ms.lang.array<? extends ms.lang.int>>", array.val());
	}

	@Test//(expected = ConfigCompileException.class)
	public void testIVariableDefinitionErrorsOnBadType() throws Exception {
		// int
		LeftHandGenericUse lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new ExactType(Target.UNKNOWN, CInt.TYPE)));
		// double
		CArray array = new CArray(Target.UNKNOWN, GenericParameters.start(CArray.TYPE.getGenericDeclaration())
				.addParameter(CDouble.TYPE, null).build());
		try {
			new IVariable(CArray.TYPE, "@a", array, Target.UNKNOWN, lhgu, null);
			fail(); // Shouldn't get here
		} catch (ConfigCompileException cce) {
			assertEquals("", cce.getMessage());
		}
	}
}
