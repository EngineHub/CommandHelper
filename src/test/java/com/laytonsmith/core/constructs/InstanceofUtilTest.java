package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.Implementation.Type;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.constraints.ExactType;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.GenericTypeParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.constraints.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.constraints.UpperBoundConstraint;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.DataHandling._instanceof;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.AccessModifier;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.testing.StaticTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class InstanceofUtilTest {

	Environment env;

	@Before
	public void before() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@BeforeClass
	public static void beforeClass() {
		Implementation.setServerType(Type.TEST);
		StaticTest.InstallFakeServerFrontend();
	}

	@Test
	public void testInstanceofUtil() throws Exception {
		Environment env = Static.GenerateStandaloneEnvironment(false);
		assertTrue(InstanceofUtil.isInstanceof(CBoolean.FALSE, Booleanish.class, env));
		assertTrue(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CInt.class, env));
		assertTrue(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CNumber.class, env));
		assertTrue(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), Mixed.class, env));
		assertFalse(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CString.class, env));

		// Generics testing
		LeftHandGenericUse extendsNumberLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new UpperBoundConstraint(Target.UNKNOWN, "?", CNumber.TYPE.asLeftHandSideType())));

		LeftHandGenericUse superIntLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE.asLeftHandSideType())));
		LeftHandGenericUse superPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CPrimitive.TYPE.asLeftHandSideType())));
		LeftHandGenericUse superNumberLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CNumber.TYPE.asLeftHandSideType())));

		LeftHandGenericUse extendsPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new UpperBoundConstraint(Target.UNKNOWN, "?", CPrimitive.TYPE.asLeftHandSideType())));

		LeftHandGenericUse intExactTypeLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CInt.TYPE.asLeftHandSideType())));
		LeftHandGenericUse stringExactTypeLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CString.TYPE.asLeftHandSideType())));

		CClassType arrayInt = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CInt.TYPE, null).build(), env);
		CClassType arrayString = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CString.TYPE, null).build(), env);
		CClassType arrayArrayInt = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CArray.TYPE, intExactTypeLHGU).build(), env);
		CClassType arrayArrayString = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CArray.TYPE, stringExactTypeLHGU).build(), env);
		CClassType arrayArrayExtendsNumber = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CArray.TYPE, extendsNumberLHGU).build(), env);
		CClassType arrayArrayExtendsPrimitive = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CArray.TYPE, extendsPrimitiveLHGU).build(), env);
		CClassType arrayArraySuperPrimitive = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CArray.TYPE, superPrimitiveLHGU).build(), env);
		CClassType arrayArraySuperNumber = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, GenericTypeParameters.nativeBuilder(CArray.TYPE)
				.addParameter(CArray.TYPE, superNumberLHGU).build(), env);

		LeftHandGenericUse stringLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CString.TYPE.asLeftHandSideType())));
		LeftHandGenericUse intLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CInt.TYPE.asLeftHandSideType())));

		assertTrue(InstanceofUtil.isInstanceof(arrayInt, CArray.TYPE, intLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayInt, CArray.TYPE, stringLHGU, env));
		assertTrue(InstanceofUtil.isInstanceof(arrayString, CArray.TYPE, stringLHGU, env));

		assertTrue(InstanceofUtil.isInstanceof(arrayInt, CArray.TYPE, extendsNumberLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayString, CArray.TYPE, extendsNumberLHGU, env));

		LeftHandGenericUse arrayIntLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, LeftHandSideType.fromNativeCClassType(CArray.TYPE, intLHGU))));

		LeftHandGenericUse arrayExtendsNumberLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, LeftHandSideType.fromNativeCClassType(CArray.TYPE, extendsNumberLHGU))));
		LeftHandGenericUse arraySuperIntLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, LeftHandSideType.fromNativeCClassType(CArray.TYPE, superIntLHGU))));
		LeftHandGenericUse arraySuperPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, LeftHandSideType.fromNativeCClassType(CArray.TYPE, superPrimitiveLHGU))));
		LeftHandGenericUse arrayExtendsPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, LeftHandSideType.fromNativeCClassType(CArray.TYPE, extendsPrimitiveLHGU))));

		// Nested LHGU
		assertEquals("ms.lang.array<ms.lang.array<ms.lang.int>>", arrayArrayInt.toString());
		assertEquals("ms.lang.array<ms.lang.int>", arrayIntLHGU.toString());
		assertTrue(InstanceofUtil.isInstanceof(arrayArrayInt, CArray.TYPE, arrayIntLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayArrayString, CArray.TYPE, arrayIntLHGU, env));

		// Nested with extensions
		assertTrue(InstanceofUtil.isInstanceof(arrayArrayExtendsNumber, CArray.TYPE, arrayExtendsNumberLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayArrayExtendsPrimitive, CArray.TYPE, arrayExtendsNumberLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayArrayExtendsPrimitive, CArray.TYPE, arraySuperIntLHGU, env));
		assertTrue(InstanceofUtil.isInstanceof(arrayArraySuperPrimitive, CArray.TYPE, arraySuperIntLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayArraySuperNumber, CArray.TYPE, arraySuperPrimitiveLHGU, env));
		assertTrue(InstanceofUtil.isInstanceof(arrayArrayInt, CArray.TYPE, arrayExtendsNumberLHGU, env));
		assertTrue(InstanceofUtil.isInstanceof(arrayArrayInt, CArray.TYPE, arrayExtendsPrimitiveLHGU, env));

		// Assignment vs instanceof
		assertFalse(InstanceofUtil.isInstanceof(CNull.NULL, arrayInt, env));
		assertEquals("null", StaticTest.SRun("string @s = null; @s", null));
		try {
			StaticTest.SRun("string @s = array();", null);
			fail();
		} catch(ConfigCompileException | ConfigRuntimeException ex) {
			// Pass
		}
		assertFalse(new _instanceof().exec(Target.UNKNOWN, env, null, CNull.NULL, CString.TYPE).getBoolean());
	}

	@typeof("InstanceofUtilTestA")
	public static class InstanceofUtilTestA implements Mixed {

		@SuppressWarnings("FieldNameHidesFieldInSuperclass")
		public static final CClassType TYPE = CClassType.getWithGenericDeclaration(InstanceofUtilTestA.class, new GenericDeclaration(Target.UNKNOWN,
				new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
						new UpperBoundConstraint(Target.UNKNOWN, "T", CPrimitive.TYPE.asLeftHandSideType()))));

		private final GenericParameters genericParameters;

		public InstanceofUtilTestA(GenericParameters genericParameters) {
			this.genericParameters = genericParameters;
		}

		@Override
		public URL getSourceJar() {
			return null;
		}

		@Override
		public Class<? extends Documentation>[] seeAlso() {
			return new Class[0];
		}

		@Override
		public String val() {
			return "";
		}

		@Override
		public void setTarget(Target target) {

		}

		@Override
		public Target getTarget() {
			return null;
		}

		@Override
		public Mixed clone() throws CloneNotSupportedException {
			return null;
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String docs() {
			return null;
		}

		@Override
		public Version since() {
			return null;
		}

		@Override
		public CClassType[] getSuperclasses() {
			return new CClassType[]{Mixed.TYPE};
		}

		@Override
		public CClassType[] getInterfaces() {
			return CClassType.EMPTY_CLASS_ARRAY;
		}

		@Override
		public ObjectType getObjectType() {
			return ObjectType.CLASS;
		}

		@Override
		public Set<ObjectModifier> getObjectModifiers() {
			return EnumSet.noneOf(ObjectModifier.class);
		}

		@Override
		public AccessModifier getAccessModifier() {
			return AccessModifier.PUBLIC;
		}

		@Override
		public CClassType getContainingClass() {
			return null;
		}

		@Override
		public boolean isInstanceOf(CClassType type, LeftHandGenericUse lhsGenericParameters, Environment env) {
			return InstanceofUtil.isInstanceof(this.typeof(env), type, lhsGenericParameters, env);
		}

		@Override
		public CClassType typeof(Environment env) {
			return Construct.typeof(this, env);
//			CClassType type = ReflectionUtils.get(this.getClass(), this, "TYPE");
//			return CClassType.get(this.getClass(), Target.UNKNOWN, this.getGenericParameters()
//					.toGenericTypeParameters(type, Target.UNKNOWN, env), env);
		}

		@Override
		public GenericParameters getGenericParameters() {
			return genericParameters;
		}

	}

	@typeof("InstanceofUtilTestB")
	public static class InstanceofUtilTestB extends InstanceofUtilTestA {

		@SuppressWarnings("FieldNameHidesFieldInSuperclass")
		public static final CClassType TYPE = CClassType.getWithGenericDeclaration(InstanceofUtilTestB.class, new GenericDeclaration(Target.UNKNOWN,
				new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
						new UpperBoundConstraint(Target.UNKNOWN, "T", CPrimitive.TYPE.asLeftHandSideType())),
				new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
						new UpperBoundConstraint(Target.UNKNOWN, "U", CNumber.TYPE.asLeftHandSideType()))))
				.withSuperParameters(GenericTypeParameters.nativeBuilder(InstanceofUtilTestA.TYPE).addParameter("T", null))
				.done();

		private final GenericParameters genericParameters;

		public InstanceofUtilTestB(GenericParameters genericParameters) {
			super(genericParameters.subset(InstanceofUtilTestA.TYPE.getGenericDeclaration(), "T"));
			this.genericParameters = genericParameters;
		}

		@Override
		public CClassType[] getSuperclasses() {
			return new CClassType[]{InstanceofUtilTestA.TYPE};
		}

		@Override
		public GenericParameters getGenericParameters() {
			return genericParameters;
		}

	}

	@Test
	public void testGenericInheritanceInstanceof() throws Exception {
		// B<string, int> extends A<string>
		InstanceofUtilTestB bStringInt = new InstanceofUtilTestB(GenericParameters.emptyBuilder(InstanceofUtilTestB.TYPE)
				.addNativeParameter(CString.TYPE, null)
				.addNativeParameter(CInt.TYPE, null).buildNative());
		assertEquals("InstanceofUtilTestB<ms.lang.string, ms.lang.int>", bStringInt.typeof(null).toString());
		// B<int, int> extends A<int>
		InstanceofUtilTestB bIntInt = new InstanceofUtilTestB(GenericParameters.emptyBuilder(InstanceofUtilTestB.TYPE)
				.addNativeParameter(CInt.TYPE, null)
				.addNativeParameter(CInt.TYPE, null).buildNative());
		assertEquals("InstanceofUtilTestB<ms.lang.int, ms.lang.int>", bIntInt.typeof(null).toString());
		// A<string>
		CClassType aString = CClassType.get(InstanceofUtilTestA.TYPE.getFQCN(), Target.UNKNOWN,
				GenericTypeParameters.nativeBuilder(InstanceofUtilTestA.TYPE)
						.addParameter(CString.TYPE, null).build(), env);
		assertEquals("InstanceofUtilTestA<ms.lang.string>", aString.toString());
		// A<string>
		CClassType aInt = CClassType.get(InstanceofUtilTestA.TYPE.getFQCN(), Target.UNKNOWN,
				GenericTypeParameters.nativeBuilder(InstanceofUtilTestA.TYPE)
						.addParameter(CInt.TYPE, null).build(), env);
		assertEquals("InstanceofUtilTestA<ms.lang.int>", aInt.toString());

		assertFalse(bStringInt.isInstanceOf(bIntInt.typeof(env), null, env));
		assertTrue(bStringInt.isInstanceOf(bStringInt.typeof(env), null, env));

		assertTrue(bStringInt.isInstanceOf(aString, null, env));
		assertFalse(bStringInt.isInstanceOf(aInt, null, env));

	}

	@Test
	public void testUsualInstanceofTypes() throws Exception {
		assertTrue(InstanceofUtil.isAssignableTo(CNull.TYPE, CString.TYPE, null, env));
		assertTrue(InstanceofUtil.isAssignableTo(CString.TYPE, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isAssignableTo(CInt.TYPE, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isAssignableTo(null, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isAssignableTo(CVoid.TYPE, CString.TYPE, null, env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE, CString.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof((CClassType) null, (CClassType) null, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE, CNull.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE, CVoid.TYPE, null, env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE, null, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE, null, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE, null, null, env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE, Auto.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof((CClassType) null, Auto.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE, Auto.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE, Auto.TYPE, null, env));

		assertFalse(InstanceofUtil.isInstanceof(null, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(null, CNull.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(null, CVoid.TYPE, null, env));

		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE, CString.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE, (CClassType) null, null, env));
		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE, CNull.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE, CVoid.TYPE, null, env));

		assertFalse(InstanceofUtil.isInstanceof(CNull.TYPE, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(CNull.TYPE, CVoid.TYPE, null, env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE, Auto.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof((CClassType) null, Auto.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE, Auto.TYPE, null, env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE, Auto.TYPE, null, env));

		assertFalse(InstanceofUtil.isInstanceof(CString.TYPE, CNull.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof((CClassType) null, CNull.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE, CNull.TYPE, null, env));

		assertFalse(InstanceofUtil.isInstanceof(CString.TYPE, CVoid.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof((CClassType) null, CVoid.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(CNull.TYPE, CVoid.TYPE, null, env));

		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE, CNull.TYPE, null, env));

		assertTrue(InstanceofUtil.isInstanceof(new CString("", Target.UNKNOWN), CString.class, env));
		assertFalse(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CString.class, env));
		assertFalse(InstanceofUtil.isInstanceof(CNull.NULL, CString.TYPE, env));
		assertFalse(InstanceofUtil.isInstanceof(null, CString.TYPE, null, env));
		assertFalse(InstanceofUtil.isInstanceof(CVoid.VOID, CString.TYPE, env));
		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE, CString.TYPE, null, env));
	}

	@Test
	public void testUsualInstanceofTypesWithLHSTypes() throws Exception {
		assertTrue(InstanceofUtil.isAssignableTo(CNull.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isAssignableTo(CString.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isAssignableTo(CInt.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isAssignableTo(LeftHandSideType.fromHardCodedType(null), CString.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isAssignableTo(CVoid.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), LeftHandSideType.fromHardCodedType(null), env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), CNull.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), CVoid.TYPE.asLeftHandSideType(), env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE.asLeftHandSideType(), LeftHandSideType.fromHardCodedType(null), env));
		assertTrue(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), LeftHandSideType.fromHardCodedType(null), env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), LeftHandSideType.fromHardCodedType(null), env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), LeftHandSideType.fromHardCodedType(null), env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE.asLeftHandSideType(), Auto.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), Auto.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), Auto.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), Auto.TYPE.asLeftHandSideType(), env));

		assertFalse(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), CString.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), LeftHandSideType.fromHardCodedType(null), env));
		assertFalse(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), CNull.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), CVoid.TYPE.asLeftHandSideType(), env));

		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE.asLeftHandSideType(), LeftHandSideType.fromHardCodedType(null), env));
		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE.asLeftHandSideType(), CNull.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(Auto.TYPE.asLeftHandSideType(), CVoid.TYPE.asLeftHandSideType(), env));

		assertFalse(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), CVoid.TYPE.asLeftHandSideType(), env));

		assertTrue(InstanceofUtil.isInstanceof(CString.TYPE.asLeftHandSideType(), Auto.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), Auto.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), Auto.TYPE.asLeftHandSideType(), env));
		assertTrue(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), Auto.TYPE.asLeftHandSideType(), env));

		assertFalse(InstanceofUtil.isInstanceof(CString.TYPE.asLeftHandSideType(), CNull.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), CNull.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), CNull.TYPE.asLeftHandSideType(), env));

		assertFalse(InstanceofUtil.isInstanceof(CString.TYPE.asLeftHandSideType(), CVoid.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(LeftHandSideType.fromHardCodedType(null), CVoid.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(CNull.TYPE.asLeftHandSideType(), CVoid.TYPE.asLeftHandSideType(), env));

		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType(), env));
		assertFalse(InstanceofUtil.isInstanceof(CVoid.TYPE.asLeftHandSideType(), CNull.TYPE.asLeftHandSideType(), env));

	}

	@Test
	public void testTypeUnions() throws Exception {
		Target t = Target.UNKNOWN;
		assertTrue(InstanceofUtil.isInstanceof(CVoid.LHSTYPE,
				LeftHandSideType.fromNativeTypeUnion(CVoid.LHSTYPE, Booleanish.TYPE.asLeftHandSideType()),
				env));
		assertTrue(InstanceofUtil.isInstanceof(CInt.TYPE.asLeftHandSideType(),
				LeftHandSideType.fromNativeTypeUnion(CInt.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType()),
				env));
		assertFalse(InstanceofUtil.isInstanceof(CArray.TYPE.asLeftHandSideType(),
				LeftHandSideType.fromNativeTypeUnion(CInt.TYPE.asLeftHandSideType(), CString.TYPE.asLeftHandSideType()),
				env));

		assertTrue(InstanceofUtil.isInstanceof(
				LeftHandSideType.fromNativeTypeUnion(CString.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType()),
				LeftHandSideType.fromNativeTypeUnion(CString.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType()),
				env));
		assertFalse(InstanceofUtil.isInstanceof(
				LeftHandSideType.fromNativeTypeUnion(CString.TYPE.asLeftHandSideType(),
						CInt.TYPE.asLeftHandSideType(), CArray.TYPE.asLeftHandSideType()),
				LeftHandSideType.fromNativeTypeUnion(CString.TYPE.asLeftHandSideType(), CInt.TYPE.asLeftHandSideType()),
				env));

		assertFalse(InstanceofUtil.isAssignableTo(CVoid.LHSTYPE, CVoid.LHSTYPE, env));
		assertTrue(InstanceofUtil.isAssignableTo(CVoid.LHSTYPE,
				LeftHandSideType.fromNativeTypeUnion(CVoid.LHSTYPE, Booleanish.TYPE.asLeftHandSideType()),
				env));
	}

	@Test
	public void testSuperClassWithGenericsInstanceof() throws Exception {
		// Both the instance and the type need to be instanceof.
		GenericTypeParameters genericTypeParameters = GenericTypeParameters.addParameter(CArray.TYPE, Target.UNKNOWN, env, CInt.TYPE, null).build();
		CClassType arrayInt = CClassType.get(CArray.TYPE, Target.UNKNOWN, genericTypeParameters, env);
		GenericTypeParameters genericTypeParametersWrong = GenericTypeParameters.addParameter(CArray.TYPE, Target.UNKNOWN, env, CString.TYPE, null).build();
		CClassType arrayString = CClassType.get(CArray.TYPE, Target.UNKNOWN, genericTypeParametersWrong, env);

		assertTrue(InstanceofUtil.isInstanceof(new CByteArray(Target.UNKNOWN, env), arrayInt, env));
		assertFalse(InstanceofUtil.isInstanceof(new CByteArray(Target.UNKNOWN, env), arrayString, env));

		assertTrue(InstanceofUtil.isInstanceof(CByteArray.TYPE, arrayInt, null, env));
		assertFalse(InstanceofUtil.isInstanceof(CByteArray.TYPE, arrayString, null, env));
	}
}
