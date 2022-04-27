package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.compiler.FileOptions;
import com.laytonsmith.core.constructs.Auto;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.IVariable;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.constructs.generics.ConcreteGenericParameter;
import com.laytonsmith.core.constructs.generics.constraints.Constraint;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.constraints.ConstructorConstraint;
import com.laytonsmith.core.constructs.generics.constraints.ExactType;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.constraints.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.constraints.UnboundedConstraint;
import com.laytonsmith.core.constructs.generics.constraints.UpperBoundConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CREGenericConstraintException;
import com.laytonsmith.core.functions.DataHandling.assign;
import com.laytonsmith.testing.StaticTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;

public class GenericsTest {
	public GenericsTest() {

	}

	private Environment env;
	private FileOptions fileOptions = new FileOptions(new HashMap<>());

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
						GenericTypeParameters.nativeBuilder(CArray.TYPE)
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new ExactType(Target.UNKNOWN, CInt.TYPE.asLeftHandSideType()))))
								.build(), env).asLeftHandSideType())));
		assertEquals("? extends ms.lang.array<ms.lang.array<ms.lang.int>>", lhgu.toString());

		// ? extends array<? extends int>

		lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new UpperBoundConstraint(Target.UNKNOWN, "?", CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN,
						GenericTypeParameters.nativeBuilder(CArray.TYPE)
								.addParameter(CArray.TYPE, new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
										new Constraints(Target.UNKNOWN, ConstraintLocation.LHS, new UpperBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE.asLeftHandSideType()))))
								.build(), env).asLeftHandSideType())));
		assertEquals("? extends ms.lang.array<ms.lang.array<? extends ms.lang.int>>", lhgu.toString());

		GenericTypeParameters params = GenericTypeParameters
				.nativeBuilder(CArray.TYPE).addParameter(CArray.TYPE, lhgu).build();

		CClassType array = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, params, env);

		assertEquals("ms.lang.array<ms.lang.array<? extends ms.lang.array<ms.lang.array<? extends ms.lang.int>>>>", array.val());
	}

//	@Test Need to do this type comparison in the compiler, not in the runtime
	public void testIVariableDefinitionErrorsOnBadType() throws Exception {
		// int
		LeftHandGenericUse lhgu = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env, new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
				new ExactType(Target.UNKNOWN, CInt.TYPE.asLeftHandSideType())));
		// double
		CArray array = new CArray(Target.UNKNOWN, GenericParameters.emptyBuilder(CArray.TYPE)
				.addNativeParameter(CDouble.TYPE, null).buildNative(), env);
		try {
			IVariable var = new IVariable(LeftHandSideType.fromCClassType(
					new ConcreteGenericParameter(CArray.TYPE, lhgu, Target.UNKNOWN, env), Target.UNKNOWN, env),
					"@a", array, Target.UNKNOWN, null);
			new assign().exec(Target.UNKNOWN, env, null, var, array);
			fail(); // Shouldn't get here
		} catch (CREGenericConstraintException cce) {
			assertEquals("", cce.getMessage());
		}
	}

	@Test
	public void testBuildFromString1() throws Exception {
		Constraints[] cs = Constraints.BuildFromString(fileOptions,
				"? extends array<? super int> & new ?(int, array<?>), int",
				ConstraintLocation.LHS, Arrays.asList(
						new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
								new UnboundedConstraint(Target.UNKNOWN, "T")),
						new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
								new UnboundedConstraint(Target.UNKNOWN, "U"))),
				Target.UNKNOWN, env);
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
		assertEquals(LeftHandSideType.fromCClassType(new ConcreteGenericParameter(CArray.TYPE, wildcardSuperInt, Target.UNKNOWN, env), Target.UNKNOWN, env), ub.getUpperBound());
		assertTrue(ub.getUpperBound().getTypes().get(0).getLeftHandGenericUse().getConstraints().get(0).getInDefinitionOrder().get(0)
				instanceof LowerBoundConstraint);

		assertTrue(cc.getArgTypes().size() == 2);
		assertEquals(CInt.TYPE.asLeftHandSideType(), cc.getArgTypes().get(0));
		LeftHandGenericUse wildcard = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new UnboundedConstraint(Target.UNKNOWN, "?")));
		assertEquals(LeftHandSideType.fromCClassType(new ConcreteGenericParameter(CArray.TYPE, wildcard, Target.UNKNOWN, env), Target.UNKNOWN, env), cc.getArgTypes().get(1));
		assertEquals("ms.lang.array<?>", cc.getArgTypes().get(1).val());
	}

	private class GenericTestCase {
		List<Constraints> definitionConstraints;
		String definition;
		String lhs;
		LeftHandSideType rhs;
		String rhsGenerics;
		Expected expected;
		boolean skipTest;

		public GenericTestCase(FileOptions fileOptions, String definition, String lhs, CClassType rhs, String rhsGenerics, Expected expected) {
			try {
				Objects.requireNonNull(definition);
				Constraints[] cs = Constraints.BuildFromString(fileOptions, definition, ConstraintLocation.DEFINITION, null, Target.UNKNOWN, env);
				this.definitionConstraints = Arrays.asList(cs);
				LeftHandGenericUse generics = rhsGenerics == null ? null : new LeftHandGenericUse(rhs, Target.UNKNOWN, env,
						Constraints.BuildFromString(fileOptions, rhsGenerics, ConstraintLocation.LHS, definitionConstraints,
								Target.UNKNOWN, env));
				this.definition = definition;
				this.lhs = lhs;
				this.rhs = rhs == null ? null : LeftHandSideType.fromCClassType(new ConcreteGenericParameter(rhs, generics, Target.UNKNOWN, env), Target.UNKNOWN, env);
				this.rhsGenerics = rhsGenerics;
				this.expected = expected;
				skipTest = false;
			} catch(CREGenericConstraintException ex) {
				if(expected == Expected.PASS) {
					throw ex;
				}
				skipTest = true;
			}
		}

		public void test() {
			if(skipTest) {
				return;
			}
			try {
				TestValidationDefinitionAndLHS(definition, lhs);
				if(rhs != null) {
					TestValidationLHSAndRHS(lhs, rhs, definitionConstraints);
				}
			} catch (CREGenericConstraintException e) {
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

	public void TestValidationDefinitionAndLHS(String definition, String lhs) {
		Constraints[] cs = Constraints.BuildFromString(fileOptions, definition, ConstraintLocation.DEFINITION, null, Target.UNKNOWN, env);
		Constraints[] lh = Constraints.BuildFromString(fileOptions, lhs, ConstraintLocation.LHS, Arrays.asList(cs), Target.UNKNOWN, env);
		if(cs.length != lh.length || lh.length != 1) {
			throw new RuntimeException("Invalid lengths, only one template parameter is allowed");
		}
		for(int i = 0; i < cs.length; i++) {
			List<String> errors = new ArrayList<>();
			cs[i].withinBounds(lh[i], errors, env);
			if(!errors.isEmpty()) {
				throw new CREGenericConstraintException(StringUtils.Join(errors, "\n"), Target.UNKNOWN);
			}
		}
	}

	public void TestValidationLHSAndRHS(String lhs, LeftHandSideType rhs, List<Constraints> definitionConstraints) {
		Constraints[] lh = Constraints.BuildFromString(fileOptions, lhs, ConstraintLocation.LHS, definitionConstraints,
				Target.UNKNOWN, env);
		if(lh.length != 1) {
			throw new RuntimeException("Invalid length, only one template parameter is allowed");
		}
		if(!lh[0].withinBounds(rhs, env)) {
			throw new CREGenericConstraintException("Not in bounds", Target.UNKNOWN);
		}
	}

	@Test
	public void testConstraintValidations() throws Exception {
		// TODO: Ensure all permutations of the implemented constraint validators are covered here,
		// both failing and non-failing.
		new GenericTestCase(fileOptions, "T", "int", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T", "number", CInt.TYPE, null, Expected.FAIL).test();
		// LowerBoundConstraint
		new GenericTestCase(fileOptions, "T", "? super number", CInt.TYPE, null, Expected.FAIL).test();
		new GenericTestCase(fileOptions, "T", "? super number", null, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T super int", "int", null, null, Expected.FAIL).test();
		// UpperBoundConstraint
		new GenericTestCase(fileOptions, "T extends number", "? super int", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T extends number", "? super number", CNumber.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T extends number", "? super mixed", null, null, Expected.FAIL).test();
		new GenericTestCase(fileOptions, "T extends number", "? extends number", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T extends number", "number", CNumber.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T extends number", "number", CInt.TYPE, null, Expected.FAIL).test();
		new GenericTestCase(fileOptions, "T extends number", "int", CNumber.TYPE, null, Expected.FAIL).test();
		new GenericTestCase(fileOptions, "T extends primitive", "? super number", CNumber.TYPE, null, Expected.PASS).test();
		// Unbounded Constraint
		new GenericTestCase(fileOptions, "T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends string>", Expected.FAIL).test();
		new GenericTestCase(fileOptions, "T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends number>", Expected.PASS).test();
		new GenericTestCase(fileOptions, "T", "array<? extends array<? extends number>>", CArray.TYPE, "? extends array<? extends int>", Expected.PASS).test();
		// Wildcard on LHS
		new GenericTestCase(fileOptions, "T", "?", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T extends number", "?", CInt.TYPE, null, Expected.PASS).test();
		new GenericTestCase(fileOptions, "T extends number", "?", CString.TYPE, null, Expected.FAIL).test();
		new GenericTestCase(fileOptions, "new T()", "?", Auto.TYPE, null, Expected.FAIL).test(); // This might currently fail for
																					// the wrong reason, but whatever.
																					// It should eventually fail
																					// because wildcard isn't valid LHS
																					// for constructors, but it might
																					// fail because it's hardcoded to
																					// return false for now.
		new GenericTestCase(fileOptions, "new T()", "?", Auto.TYPE, null, Expected.FAIL).test();


		// Wildcard in unacceptable place
		new GenericTestCase(fileOptions, "?", "int", CInt.TYPE, null, Expected.FAIL).test();

		// ExactType in declaration, pass, but warn
		int warningCount = env.getEnv(CompilerEnvironment.class).getCompilerWarnings().size();
		new GenericTestCase(fileOptions, "int", "int", CInt.TYPE, null, Expected.PASS).test();
		assertEquals(warningCount + 2, env.getEnv(CompilerEnvironment.class).getCompilerWarnings().size());
	}

	@Test
	public void testNakedArrayIsAuto() throws Exception {
		String result = StaticTest.SRun("typeof(array())", null);
		assertEquals("ms.lang.array<auto>", result);
	}

}
