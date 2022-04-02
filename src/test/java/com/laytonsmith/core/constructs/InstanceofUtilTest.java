package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.MapBuilder;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.Implementation.Type;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.Documentation;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.generics.ConstraintLocation;
import com.laytonsmith.core.constructs.generics.Constraints;
import com.laytonsmith.core.constructs.generics.ExactType;
import com.laytonsmith.core.constructs.generics.GenericDeclaration;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.constructs.generics.LeftHandGenericUse;
import com.laytonsmith.core.constructs.generics.LowerBoundConstraint;
import com.laytonsmith.core.constructs.generics.UpperBoundConstraint;
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
import java.util.HashMap;
import java.util.Map;
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
						new UpperBoundConstraint(Target.UNKNOWN, "?", CNumber.TYPE, null)));

		LeftHandGenericUse superIntLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CInt.TYPE, null)));
		LeftHandGenericUse superPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CPrimitive.TYPE, null)));
		LeftHandGenericUse superNumberLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new LowerBoundConstraint(Target.UNKNOWN, "?", CNumber.TYPE, null)));

		LeftHandGenericUse extendsPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new UpperBoundConstraint(Target.UNKNOWN, "?", CPrimitive.TYPE, null)));

		LeftHandGenericUse intExactTypeLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CInt.TYPE, null)));
		LeftHandGenericUse stringExactTypeLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CString.TYPE, null)));

		CClassType arrayInt = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CInt.TYPE, null).build()), env);
		CClassType arrayString = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CString.TYPE, null).build()), env);
		CClassType arrayArrayInt = CClassType.get(CArray.TYPE.getFQCN(), Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, intExactTypeLHGU).build()), env);
		CClassType arrayArrayString = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, stringExactTypeLHGU).build()), env);
		CClassType arrayArrayExtendsNumber = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, extendsNumberLHGU).build()), env);
		CClassType arrayArrayExtendsPrimitive = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, extendsPrimitiveLHGU).build()), env);
		CClassType arrayArraySuperPrimitive = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, superPrimitiveLHGU).build()), env);
		CClassType arrayArraySuperNumber = CClassType.get(CArray.TYPE, Target.UNKNOWN, MapBuilder.start(CArray.TYPE, GenericParameters
				.addParameter(CArray.TYPE, superNumberLHGU).build()), env);

		LeftHandGenericUse stringLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CString.TYPE, null)));
		LeftHandGenericUse intLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CInt.TYPE, null)));

		assertTrue(InstanceofUtil.isInstanceof(arrayInt, CArray.TYPE, intLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayInt, CArray.TYPE, stringLHGU, env));
		assertTrue(InstanceofUtil.isInstanceof(arrayString, CArray.TYPE, stringLHGU, env));

		assertTrue(InstanceofUtil.isInstanceof(arrayInt, CArray.TYPE, extendsNumberLHGU, env));
		assertFalse(InstanceofUtil.isInstanceof(arrayString, CArray.TYPE, extendsNumberLHGU, env));

		LeftHandGenericUse arrayIntLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CArray.TYPE, intLHGU)));

		LeftHandGenericUse arrayExtendsNumberLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CArray.TYPE, extendsNumberLHGU)));
		LeftHandGenericUse arraySuperIntLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CArray.TYPE, superIntLHGU)));
		LeftHandGenericUse arraySuperPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CArray.TYPE, superPrimitiveLHGU)));
		LeftHandGenericUse arrayExtendsPrimitiveLHGU = new LeftHandGenericUse(CArray.TYPE, Target.UNKNOWN, env,
				new Constraints(Target.UNKNOWN, ConstraintLocation.LHS,
						new ExactType(Target.UNKNOWN, CArray.TYPE, extendsPrimitiveLHGU)));

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
		assertEquals("null", StaticTest.SRun("string @s = null; msg(@s);", null));
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
						new UpperBoundConstraint(Target.UNKNOWN, "T", CPrimitive.TYPE, null))));

		private final Map<CClassType, GenericParameters> genericParameters = new HashMap<>();

		public InstanceofUtilTestA(GenericParameters genericParameters) {
			registerGenerics(InstanceofUtilTestA.TYPE, genericParameters);
		}

		protected final void registerGenerics(CClassType type, GenericParameters parameters) {
			genericParameters.put(type, parameters);
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
			return CClassType.get(this.getClass(), Target.UNKNOWN, this.getGenericParameters(), env);
		}

		@Override
		public Map<CClassType, GenericParameters> getGenericParameters() {
			return new HashMap<>(genericParameters);
		}

	}

	@typeof("InstanceofUtilTestB")
	public static class InstanceofUtilTestB extends InstanceofUtilTestA {

		@SuppressWarnings("FieldNameHidesFieldInSuperclass")
		public static final CClassType TYPE = CClassType.getWithGenericDeclaration(InstanceofUtilTestB.class, new GenericDeclaration(Target.UNKNOWN,
				InstanceofUtilTestA.TYPE.getGenericDeclaration().getConstraints().get(0),
				new Constraints(Target.UNKNOWN, ConstraintLocation.DEFINITION,
						new UpperBoundConstraint(Target.UNKNOWN, "U", CNumber.TYPE, null))));

		public InstanceofUtilTestB(GenericParameters genericParameters) {
			super(genericParameters.subset(InstanceofUtilTestA.TYPE.getGenericDeclaration(), "T"));
			registerGenerics(InstanceofUtilTestB.TYPE, genericParameters);
		}

		@Override
		public CClassType[] getSuperclasses() {
			return new CClassType[]{InstanceofUtilTestA.TYPE};
		}

	}

	@Test
	public void testGenericInheritanceInstanceof() throws Exception {
		// B<string, int> extends A<string>
		InstanceofUtilTestB bStringInt = new InstanceofUtilTestB(GenericParameters
				.addParameter(CString.TYPE, null)
				.addParameter(CInt.TYPE, null).build());
		// B<int, int> extends A<int>
		InstanceofUtilTestB bIntInt = new InstanceofUtilTestB(GenericParameters
				.addParameter(CInt.TYPE, null)
				.addParameter(CInt.TYPE, null).build());
		// A<string>
		CClassType aString = CClassType.get(InstanceofUtilTestA.TYPE.getFQCN(), Target.UNKNOWN,
				MapBuilder.start(InstanceofUtilTestA.TYPE, GenericParameters
						.addParameter(CString.TYPE, null).build()).build(), env);
		// A<string>
		CClassType aInt = CClassType.get(InstanceofUtilTestA.TYPE.getFQCN(), Target.UNKNOWN,
				MapBuilder.start(InstanceofUtilTestA.TYPE, GenericParameters
						.addParameter(CInt.TYPE, null).build()).build(), env);

		assertFalse(bStringInt.isInstanceOf(bIntInt.typeof(env), null, env));
		assertTrue(bStringInt.isInstanceOf(bStringInt.typeof(env), null, env));

		assertTrue(bStringInt.isInstanceOf(aString, null, env));
		assertFalse(bStringInt.isInstanceOf(aInt, null, env));

	}
}
