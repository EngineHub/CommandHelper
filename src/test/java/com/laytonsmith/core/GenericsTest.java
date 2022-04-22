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
import com.laytonsmith.core.constructs.generics.UnboundedConstraint;
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
	@SuppressWarnings("ResultOfObjectAllocationIgnored")
	public void testBasicConstraintValidation() throws Exception {
		// Lower bounds cannot be used on declarations
		new GenericDeclaration(Target.UNKNOWN, new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
				new LowerBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE.asLeftHandSideType())));
	}

	@Test
	public void testConstraintHasCorrectData() throws Exception {
		UpperBoundConstraint ubc = new UpperBoundConstraint(Target.UNKNOWN, "T", CNumber.TYPE.asLeftHandSideType());
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
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new ExactType(Target.UNKNOWN, CInt.TYPE.asLeftHandSideType()))))
								.build()), env).asLeftHandSideType())));
		assertEquals("? extends ms.lang.array<ms.lang.array<ms.lang.int>>", lhgu.toString());

		// ? extends array<? extends int>

		lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new UpperBoundConstraint(Target.UNKNOWN, "?", CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN,
						MapBuilder.start(CArray.TYPE, GenericParameters
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new UpperBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE.asLeftHandSideType()))))
								.build()), env).asLeftHandSideType())));
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
				new ExactType(Target.UNKNOWN, CInt.TYPE.asLeftHandSideType())));
		// double
		CArray array = new CArray(Target.UNKNOWN, GenericParameters
				.addParameter(CDouble.TYPE, null).build(), env);
		try {
			IVariable var = new IVariable(LeftHandSideType.fromCClassType(CArray.TYPE, lhgu, Target.UNKNOWN), "@a", array, Target.UNKNOWN, null);
			new assign().exec(Target.UNKNOWN, env, null, var, array);
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

		LeftHandGenericUse wildcardSuperInt = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE.asLeftHandSideType())));
		assertEquals(LeftHandSideType.fromCClassType(CArray.TYPE, wildcardSuperInt, Target.UNKNOWN), ub.getUpperBound());
		assertTrue(ub.getUpperBound().getTypes().get(0).getValue().getConstraints().get(0).getInDefinitionOrder().get(0)
				instanceof LowerBoundConstraint);

		assertTrue(cc.getArgTypes().size() == 2);
		assertEquals(CInt.TYPE.asLeftHandSideType(), cc.getArgTypes().get(0));
		LeftHandGenericUse wildcard = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new UnboundedConstraint(Target.UNKNOWN, "?")));
		assertEquals(LeftHandSideType.fromCClassType(CArray.TYPE, wildcard, Target.UNKNOWN), cc.getArgTypes().get(1));
		assertEquals("ms.lang.array<?>", cc.getArgTypes().get(1).val());
	}

	private class GenericTestCase {
		String definition;
		String lhs;
		LeftHandSideType rhs;
		String rhsGenerics;
		Expected expected;

		public GenericTestCase(String definition, String lhs, CClassType rhs, String rhsGenerics, Expected expected) {
			LeftHandGenericUse generics = rhsGenerics == null ? null : new LeftHandGenericUse(rhs, Target.UNKNOWN, env,
					Constraints.BuildFromString(rhsGenerics, ConstraintLocation.LHS, Target.UNKNOWN, env));
			this.definition = definition;
			this.lhs = lhs;
			this.rhs = rhs == null ? null : LeftHandSideType.fromCClassType(rhs, generics, Target.UNKNOWN);
			this.rhsGenerics = rhsGenerics;
			this.expected = expected;
		}

		public void test() {
			try {
				TestValidationDefinitionAndLHS(definition, lhs);
				if(rhs != null) {
					TestValidationLHSAndRHS(lhs, rhs);
				}
			} catch (Exception e) {
				if(expected == Expected.PASS) {
					e.printStackTrace(System.out);
					throw new RuntimeException("Failed on " + this.toString() + " because " + e.toString(), e);
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

	public void TestValidationLHSAndRHS(String lhs, LeftHandSideType rhs) throws Exception {
		Constraints[] lh = Constraints.BuildFromString(lhs, ConstraintLocation.LHS, Target.UNKNOWN, env);
		if(lh.length != 1) {
			throw new Exception("Invalid length, only one template parameter is allowed");
		}
		if(!lh[0].withinBounds(rhs, env)) {
			throw new Exception("Not in bounds");
		}
	}

	@Test
	public void testConstraintValidations() throws Exception {
		// TODO: Ensure all permutations of the implemented constraint validators are covered here,
		// both failing and non-failing.
		new GenericTestCase("T", "int", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase("T", "number", CInt.TYPE, null, Expected.FAIL).test();
		// LowerBoundConstraint
		new GenericTestCase("T", "? super number", CInt.TYPE, null, Expected.FAIL).test();
		new GenericTestCase("T", "? super number", null, null, Expected.PASS).test();
		new GenericTestCase("T super int", "int", null, null, Expected.FAIL).test();
		// UpperBoundConstraint
		new GenericTestCase("T extends number", "? super int", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase("T extends number", "? super number", CNumber.TYPE, null, Expected.PASS).test();
		new GenericTestCase("T extends number", "? super mixed", null, null, Expected.FAIL).test();
		new GenericTestCase("T extends number", "? extends number", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase("T extends number", "number", CNumber.TYPE, null, Expected.PASS).test();
		new GenericTestCase("T extends number", "number", CInt.TYPE, null, Expected.FAIL).test();
		new GenericTestCase("T extends number", "int", CNumber.TYPE, null, Expected.FAIL).test();
		new GenericTestCase("T extends primitive", "? super number", CNumber.TYPE, null, Expected.PASS).test();
		// Unbounded Constraint
		new GenericTestCase("T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends string>", Expected.FAIL).test();
		new GenericTestCase("T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends number>", Expected.PASS).test();
		new GenericTestCase("T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends int>", Expected.PASS).test();
	}

	@Test
	public void testNakedArrayIsAuto() throws Exception {
		String result = StaticTest.SRun("typeof(array())", null);
		assertEquals("ms.lang.array<auto>", result);
	}
}
