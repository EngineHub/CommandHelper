package com.laytonsmith.core.compiler.signatures;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.signature.FunctionSignatures;
import com.laytonsmith.core.compiler.signature.Param;
import com.laytonsmith.core.compiler.signature.SignatureBuilder;
import com.laytonsmith.core.compiler.signature.Throws;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREIndexOverflowException;
import com.laytonsmith.testing.StaticTest;

public class FunctionSignaturesTest {

	private Environment env;

	@BeforeClass
	public static void setUpClass() throws Exception {
		StaticTest.InstallFakeServerFrontend();
	}

	@Before
	public void setUp() throws Exception {
		this.env = Static.GenerateStandaloneEnvironment(false);
	}

	@Test
	public void testReturnType() {

		// Create "array func()" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).build();

		// Assert that the return type was correctly set.
		assertEquals(CArray.TYPE.asLeftHandSideType(), signatures.getSignatures().get(0).getReturnType().getType());
	}

	@Test
	public void testSingleParam() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert that a parameter was added with the correct type and description.
		assertEquals(1, signatures.getSignatures().size());
		List<Param> paramsList = signatures.getSignatures().get(0).getParams();
		assertEquals(1, paramsList.size());
		assertEquals("param1", paramsList.get(0).getName());
		assertEquals(CInt.TYPE.asLeftHandSideType(), paramsList.get(0).getType());
	}

	@Test
	public void testSingleThrows() {

		// Create "array func(int param1) throws CREIndexOverflowException" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1")
				.throwsEx(CREIndexOverflowException.class, "exDesc").build();

		// Assert that a throws was added with the correct exception class and description.
		assertEquals(1, signatures.getSignatures().size());
		List<Throws> throwsList = signatures.getSignatures().get(0).getThrows();
		assertEquals(1, throwsList.size());
		assertEquals("exDesc", throwsList.get(0).getThrownWhen());
		assertEquals(CREIndexOverflowException.class, throwsList.get(0).getExceptionClass());
	}

	@Test
	public void testDoubleThrows() {

		// Create "array func(int param1) throws CREIndexOverflowException, CRECastException" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1")
				.throwsEx(CREIndexOverflowException.class, "exDesc1")
				.throwsEx(CRECastException.class, "exDesc2").build();

		assertEquals(1, signatures.getSignatures().size());
		List<Throws> throwsList = signatures.getSignatures().get(0).getThrows();
		assertEquals(2, throwsList.size());
		for(Throws throwsObj : throwsList) {
			if(throwsObj.getThrownWhen().equals("exDesc1")) {
				assertEquals(CREIndexOverflowException.class, throwsObj.getExceptionClass());
			} else {
				assertEquals("exDesc2", throwsObj.getThrownWhen());
				assertEquals(CRECastException.class, throwsObj.getExceptionClass());
			}
		}
	}

	@Test
	public void testNoArgsSingleSignatureMatch() {

		// Create "void func()" signature.
		FunctionSignatures signatures = new SignatureBuilder(CVoid.TYPE).build();

		// Assert return type and compile exceptions for zero arguments.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(),
				Arrays.asList(),
				this.env, exceptions);
		assertEquals(CVoid.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testNoArgsSingleSignatureTooManyArgs() {

		// Create "void func()" signature.
		FunctionSignatures signatures = new SignatureBuilder(CVoid.TYPE).build();

		// Assert return type and compile exceptions for one argument.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CClassType.AUTO.asLeftHandSideType(), returnType);
		assertFalse(exceptions.isEmpty());
	}

	@Test
	public void testSingleArgSingleSignatureMatch() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for one argument.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testSingleArgSingleSignatureAutoMatch() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for one argument.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CClassType.AUTO.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testSingleArgSingleSignatureTooFewArgs() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for two arguments.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(),
				Arrays.asList(),
				this.env, exceptions);
		assertEquals(CClassType.AUTO.asLeftHandSideType(), returnType);
		assertFalse(exceptions.isEmpty());
	}

	@Test
	public void testSingleArgSingleSignatureTooManyArgs() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for two arguments.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType(), CBoolean.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN, Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CClassType.AUTO.asLeftHandSideType(), returnType);
		assertFalse(exceptions.isEmpty());
	}

	@Test
	public void testSingleArgSingleSignatureWrongType() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for one wrongly typed argument.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CBoolean.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CClassType.AUTO.asLeftHandSideType(), returnType);
		assertFalse(exceptions.isEmpty());
	}

	@Test
	public void testSingleArgSingleSignatureWrongVoidType() {

		// Create "array func(int param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for one wrongly typed argument.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CVoid.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CClassType.AUTO.asLeftHandSideType(), returnType);
		assertFalse(exceptions.isEmpty());
	}

	@Test
	public void testSingleArgMultiSignatureSingleMatch1() {

		// Create "array func(int param1)|int func(boolean param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1")
				.newSignature(CInt.TYPE).param(CBoolean.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for matching the first signature.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testSingleArgMultiSignatureSingleMatch2() {

		// Create "array func(int param1)|int func(boolean param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1")
				.newSignature(CInt.TYPE).param(CBoolean.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for matching the second signature.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CBoolean.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CInt.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testSingleArgMultiSignatureMultiMatchSameType() {

		// Create "int func(int param1)|int func(boolean param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CInt.TYPE).param(CInt.TYPE, "param1", "desc1")
				.newSignature(CInt.TYPE).param(CBoolean.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for matching the both signatures.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CClassType.AUTO.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CInt.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	@Deprecated // This test should fail as soon as multiple signature matches can return an A OR B type.
	public void testSingleArgMultiSignatureMultiMatchDifferentType() {

		// Create "array func(int param1)|int func(boolean param1)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CInt.TYPE).param(CInt.TYPE, "param1", "desc1")
				.newSignature(CArray.TYPE).param(CBoolean.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for matching the both signatures.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CClassType.AUTO.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CClassType.AUTO.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testOptionalArgSingleSignatureMatch() {

		// Create "array func([int param1])" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).param(CInt.TYPE, "param1", "desc1", true).build();

		// Assert return type and compile exceptions for zero and one arguments.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType1 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(),
				Arrays.asList(),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType1);
		assertArrayEquals(new Object[0], exceptions.toArray());
		LeftHandSideType returnType2 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType2);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testVarArgSingleSignatureMatch() {

		// Create "array func(int param1...)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE).varParam(CInt.TYPE, "param1", "desc1").build();

		// Assert return type and compile exceptions for zero, one and two arguments.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType1 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(),
				Arrays.asList(),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType1);
		assertArrayEquals(new Object[0], exceptions.toArray());
		LeftHandSideType returnType2 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType2);
		assertArrayEquals(new Object[0], exceptions.toArray());
		LeftHandSideType returnType3 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN, Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType3);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testComplexSingleSignatureMatch() {

		// Create "array func(int param1, int param2..., int param3, [array param4], array param5)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE)
				.param(CInt.TYPE, "param1", "desc1").varParam(CInt.TYPE, "param2", "desc2")
				.param(CInt.TYPE, "param3", "desc3").param(CArray.TYPE, "param4", "desc4", true)
				.param(CArray.TYPE, "param5", "desc5").build();

		// Validate signature parameter types through its string version.
		assertEquals("(int, int..., int, [array], array)", signatures.getSignaturesParamTypesString());

		// Assert return type and compile exceptions for matching the signature in multiple ways.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		LeftHandSideType returnType = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CArray.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN, Target.UNKNOWN, Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType);
		assertArrayEquals(new Object[0], exceptions.toArray());
		LeftHandSideType returnType2 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CArray.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN, Target.UNKNOWN, Target.UNKNOWN, Target.UNKNOWN, Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType2);
		assertArrayEquals(new Object[0], exceptions.toArray());
		LeftHandSideType returnType3 = signatures.getReturnType(
				Target.UNKNOWN,
				null,
				Arrays.asList(CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType(), CArray.TYPE.asLeftHandSideType(), CArray.TYPE.asLeftHandSideType()),
				Arrays.asList(Target.UNKNOWN, Target.UNKNOWN,
						Target.UNKNOWN, Target.UNKNOWN, Target.UNKNOWN, Target.UNKNOWN),
				this.env, exceptions);
		assertEquals(CArray.TYPE.asLeftHandSideType(), returnType3);
		assertArrayEquals(new Object[0], exceptions.toArray());
	}

	@Test
	public void testComplexSingleAmbiguousSignatureMatch() {

		// Create "array func(int param1..., int param2..., int param3...,
		// [int param4], int param5, int param6, int param7)" signature.
		FunctionSignatures signatures = new SignatureBuilder(CArray.TYPE)
				.varParam(CInt.TYPE, "param1", "desc1").varParam(CInt.TYPE, "param2", "desc2")
				.varParam(CInt.TYPE, "param3", "desc3").param(CInt.TYPE, "param4", "desc4", true)
				.param(CInt.TYPE, "param5", "desc5").param(CInt.TYPE, "param6", "desc6")
				.param(CInt.TYPE, "param7", "desc7").build();

		// Validate signature parameter types through its string version.
		assertEquals("(int..., int..., int..., [int], int, int, int)", signatures.getSignaturesParamTypesString());

		// Assert return type and compile exceptions for matching the signature from 4 to 10 arguments.
		// The interesting part about this test is that the varargs need to be backtrack-unmatched through multiple
		// other varargs and an optional arg to match the last 3 arguments properly.
		Set<ConfigCompileException> exceptions = new HashSet<>();
		for(int numArgs = 3; numArgs < 10; numArgs++) {
			List<LeftHandSideType> argTypes = new ArrayList<>();
			List<Target> argTargets = new ArrayList<>();
			for(int i = 0; i < numArgs; i++) {
				argTypes.add(CInt.TYPE.asLeftHandSideType());
				argTargets.add(Target.UNKNOWN);
			}
			LeftHandSideType returnType = signatures.getReturnType(
					Target.UNKNOWN, null, argTypes, argTargets, this.env, exceptions);
			assertEquals(CArray.TYPE.asLeftHandSideType(), returnType);
			assertArrayEquals(new Object[0], exceptions.toArray());
		}
	}
}
