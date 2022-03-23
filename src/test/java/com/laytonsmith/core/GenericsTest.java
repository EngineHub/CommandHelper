package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.Constraint;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.ConstructorConstraint;
import com.laytonsmith.core.constructs.generics.ExactType;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.UpperBoundConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.functions.DataHandling.assign;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GenericsTest {
	public GenericsTest() {

	}

	private Environment env;

	@BeforeClass
	public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@After
	public void tearDown() {
	}

	@Test(expected = CREGenericConstraintException.class)
	public void testBasicConstraintValidation() throws Exception {
		// Lower bounds cannot be used on declarations
		new GenericDeclaration(Target.UNKNOWN, new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
				new LowerBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE, null)));
	}

	@Test
	public void testConstraintHasCorrectData() throws Exception {
		UpperBoundConstraint ubc = new UpperBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE, null);
		assertEquals("upper bound", ubc.getConstraintName());
		assertEquals("T", ubc.getTypeName());
		assertEquals("T extends ms.lang.number", ubc.toString());
	}

	@Test
	public void testLHSToStringIsCorrect() throws Exception {
		// ? extends array<array<int>>
		LeftHandGenericUse lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new UpperBoundConstraint(Target.UNKNOWN, "?", CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN,
						MapBuilder.start(CArray.TYPE, GenericParameters
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new ExactType(Target.UNKNOWN, CInt.TYPE, null))))
								.build()), env), null)));
		assertEquals("? extends ms.lang.array<ms.lang.array<ms.lang.int>>", lhgu.toString());

		// ? extends array<? extends int>

		lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new UpperBoundConstraint(Target.UNKNOWN, "?", CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN,
						MapBuilder.start(CArray.TYPE, GenericParameters
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new UpperBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE, null))))
								.build()), env), null)));
		assertEquals("? extends ms.lang.array<ms.lang.array<? extends ms.lang.int>>", lhgu.toString());

		Map<CClassType, GenericParameters> params = MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, lhgu).build()).build();

		CClassType array = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, params, env);

		assertEquals("ms.lang.array<ms.lang.array<? extends ms.lang.array<ms.lang.array<? extends ms.lang.int>>>>", array.val());
	}

//	@Test Need to do this type comparison in the compiler, not in the runtime
	public void testIVariableDefinitionErrorsOnBadType() throws Exception {
		// int
		LeftHandGenericUse lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new ExactType(Target.UNKNOWN, CInt.TYPE, null)));
		// double
		CArray array = new CArray(Target.UNKNOWN, GenericParameters
				.addParameter(CDouble.TYPE, null).build(), env);
		try {
			IVariable var = new IVariable(LeftHandSideType.fromCClassType(CArray.TYPE, lhgu, Target.UNKNOWN), "@a", array, Target.UNKNOWN, null);
			new assign().exec(Target.UNKNOWN, env, var, array);
			fail(); // Shouldn't get here
		} catch (CREGenericConstraintException cce) {
			assertEquals("", cce.getMessage());
		}
	}

	@Test
	public void testBuildFromString1() throws Exception {
		Constraints[] cs = Constraints.BuildFromString("? extends array<? super int> & new ?(int, array<?>), int", ConstraintLocation.LHS, Target.UNKNOWN, env);
		assertTrue(cs.length == 2);
		assertTrue(cs[1].getInDefinitionOrder().get(0) instanceof ExactType);
		List<Constraint> constraints = cs[0].getInDefinitionOrder();
		assertTrue(constraints.size() == 2);
		assertTrue(constraints.get(0) instanceof UpperBoundConstraint);
		assertTrue(constraints.get(1) instanceof ConstructorConstraint);

		UpperBoundConstraint ub = (UpperBoundConstraint) constraints.get(0);
		ConstructorConstraint cc = (ConstructorConstraint) constraints.get(1);

		assertEquals(CArray.TYPE, ub.getUpperBound());
		assertTrue(ub.getBoundaryGenerics().getConstraints().get(0).getInDefinitionOrder().get(0) instanceof LowerBoundConstraint);

		assertTrue(cc.getTypes().size() == 2);
		assertEquals(CInt.TYPE, cc.getTypes().get(0).getKey());
		assertEquals(CArray.TYPE, cc.getTypes().get(1).getKey());
		assertEquals("?", cc.getTypes().get(1).getValue().getConstraints().get(0).getTypeName());
	}

	private class GenericTestCase {
		String definition;
		String lhs;
		CClassType rhs;
		String rhsGenerics;
		Expected expected;

		public GenericTestCase(String definition, String lhs, CClassType rhs, String rhsGenerics, Expected expected) {
			this.definition = definition;
			this.lhs = lhs;
			this.rhs = rhs;
			this.rhsGenerics = rhsGenerics;
			this.expected = expected;
		}

		public void test() {
			try {
				TestValidationDefinitionAndLHS(definition, lhs);
				if(rhs != null) {
					TestValidationLHSAndRHS(lhs, rhs, rhsGenerics);
				}
			} catch (Exception e) {
				if(expected == Expected.PASS) {
					e.printStackTrace(System.out);
					fail("Failed on " + this.toString() + " because " + e.toString());
				} else {
					// Good.
					return;
				}
			}
			if(expected == Expected.FAIL) {
				fail("Failed on " + this.toString() + " because no failure occured");
			}
		}

		@Override
		public String toString() {
			return "GenericTestCase{"
					+ "definition='" + definition + '\''
					+ ", lhs='" + lhs + '\''
					+ ", rhs=" + rhs
					+ ", rhsGenerics='" + rhsGenerics + '\''
					+ ", expected=" + expected
					+ '}';
		}
	}

	private enum Expected {
		PASS, FAIL;
	}

	public void TestValidationDefinitionAndLHS(String definition, String lhs) throws Exception {
		Constraints[] cs = Constraints.BuildFromString(definition, ConstraintLocation.DEFINITION, Target.UNKNOWN, env);
		Constraints[] lh = Constraints.BuildFromString(lhs, ConstraintLocation.LHS, Target.UNKNOWN, env);
		if(cs.length != lh.length || lh.length != 1) {
			throw new Exception("Invalid lengths, only one template parameter is allowed");
		}
		for(int i = 0; i < cs.length; i++) {
			List<String> errors = new ArrayList<>();
			cs[i].withinBounds(lh[i], errors, env);
			if(!errors.isEmpty()) {
				throw new Exception(StringUtils.Join(errors, "\n"));
			}
		}
	}

	public void TestValidationLHSAndRHS(String lhs, CClassType rhs, String rhsGenerics) throws Exception {
		Constraints[] lh = Constraints.BuildFromString(lhs, ConstraintLocation.LHS, Target.UNKNOWN, env);
		if(lh.length != 1) {
			throw new Exception("Invalid length, only one template parameter is allowed");
		}
		if(!lh[0].withinBounds(rhs, rhsGenerics == null
				? null
				: new LeftHandGenericUse(rhs, Target.UNKNOWN, env, Constraints.BuildFromString(rhsGenerics, ConstraintLocation.LHS, Target.UNKNOWN, env)), env)) {
			throw new Exception("Not in bounds");
		}
	}

	@Test
	public void testConstraintValidations() throws Exception {
		// TODO: Ensure all permutations of the implemented constraint validators are covered here,
		// both failing and non-failing.
		GenericTestCase[] testCases = new GenericTestCase[]{
			// ConstructorConstraint - once this is generally supported
			// ExactTypeConstraint
			new GenericTestCase("T", "int", CInt.TYPE, null, Expected.PASS),
			new GenericTestCase("T", "number", CInt.TYPE, null, Expected.FAIL),
			// LowerBoundConstraint
			new GenericTestCase("T", "? super number", CInt.TYPE, null, Expected.FAIL),
			new GenericTestCase("T", "? super number", null, null, Expected.PASS),
			new GenericTestCase("T super int", "int", null, null, Expected.FAIL),
			// UpperBoundConstraint
			new GenericTestCase("T extends number", "? super int", CInt.TYPE, null, Expected.PASS),
			new GenericTestCase("T extends number", "? super number", CNumber.TYPE, null, Expected.PASS),
			new GenericTestCase("T extends number", "? super mixed", null, null, Expected.FAIL),
			new GenericTestCase("T extends number", "? extends number", CInt.TYPE, null, Expected.PASS),
			new GenericTestCase("T extends number", "number", CNumber.TYPE, null, Expected.PASS),
			new GenericTestCase("T extends number", "number", CInt.TYPE, null, Expected.FAIL),
			new GenericTestCase("T extends number", "int", CNumber.TYPE, null, Expected.FAIL),
			new GenericTestCase("T extends primitive", "? super number", CNumber.TYPE, null, Expected.PASS),
			// Unbounded Constraint
			new GenericTestCase("T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends string>", Expected.FAIL),
			new GenericTestCase("T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends number>", Expected.PASS),
			new GenericTestCase("T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends int>", Expected.PASS),
		};

		for(int i = 0; i < testCases.length; i++) {
			GenericTestCase test = testCases[i];
			System.out.println(i + " " + test);
			test.test();
		}
	}
}
