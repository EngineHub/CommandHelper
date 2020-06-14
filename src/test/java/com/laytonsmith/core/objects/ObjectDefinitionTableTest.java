package com.laytonsmith.core.objects;

import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.environments.RuntimeMode;
import com.laytonsmith.core.environments.StaticRuntimeEnv;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 */
public class ObjectDefinitionTableTest {

	Environment env;
	static Set<Class<? extends Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();

	private void doCompile(String script) throws Exception {
		MethodScriptCompiler.compile(MethodScriptCompiler.lex(script, null, new File("Test.ms"), true), env,
				Environment.getDefaultEnvClasses());
	}

	private ObjectDefinition getObjectDefinition(String fqcn) throws Exception {
		return env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable()
				.get(FullyQualifiedClassName.forFullyQualifiedClass(fqcn));
	}

	private void addNatives() {
		env.getEnv(CompilerEnvironment.class).getObjectDefinitionTable().addNativeTypes(env, envs);
	}

	@Before
	public void Before() {
		StaticTest.InstallFakeServerFrontend();
		env = Environment.createEnvironment(new CompilerEnvironment(),
			new GlobalEnv(GlobalEnv.NO_OP_EXECUTION_QUEUE, new File("."), EnumSet.of(RuntimeMode.CMDLINE)),
			new StaticRuntimeEnv(StaticRuntimeEnv.NO_OP_PROFILER,
					StaticRuntimeEnv.NO_OP_PN, StaticRuntimeEnv.NO_OP_PROFILES, StaticRuntimeEnv.NO_OP_TASK_MANAGER));
	}

	@Test
	@Ignore("Ignored for now, while the features are being slowly rolled out")
	public void testNativeTypeListIsProperlyAdded() {
		ObjectDefinitionTable table = ObjectDefinitionTable.GetNewInstance(env, envs);
		try {
			ObjectDefinition d = table.get(CString.class);
			table.add(d, Target.UNKNOWN);
			fail();
		} catch (DuplicateObjectDefinitionException e) {
			// pass
		}
		// Sanity check
		assertFalse(NativeTypeList.getNativeTypeList().isEmpty());
		// -2 is for null and void
		assertEquals(NativeTypeList.getNativeTypeList().size() - 2, table.getObjectDefinitionSet().size());
	}

	@Test
	@Ignore("Ignored for now, while the features are being slowly rolled out")
	public void testStringIsProperlyDefined() {
		ObjectDefinitionTable table1 = ObjectDefinitionTable.GetNewInstance(env, envs);
		ObjectDefinition string1 = table1.get(CString.class);
		ObjectDefinitionTable table2 = ObjectDefinitionTable.GetNewInstance(env, envs);
		ObjectDefinition string2 = table2.get(CString.class);
		assertTrue(string1.equals(string2));
		assertTrue(string2.exactlyEquals(string2));
		System.out.println(string1.toString());
		assertEquals(1, string1.getSuperclasses().size());
		assertEquals(Arrays.asList(CPrimitive.TYPE), string1.getSuperclasses());
		// Shortcut to deciding if it populated properly
		assertEquals("ObjectDefinition {annotations=null, accessModifier=PUBLIC, objectModifiers=[],"
				+ " objectType=CLASS, type=ms.lang.string, superclasses=CClassType[] {ms.lang.primitive},"
				+ " interfaces=CClassType[] {ms.lang.Iterable}}", string1.toString());
	}

	@Test
	@Ignore("Ignored for now, while the features are being slowly rolled out")
	public void testExposedPropertiesAreProperlyDefined() {

	}

	@Test
	@Ignore
	public void testEmptyClassIsDefined() throws Exception {
		String clazz = "class Test implements A, B {}";
		doCompile(clazz);
		getObjectDefinition("Test");
	}

	@Test
	public void testNoSyntaxSugarBasic() throws Exception {
		String script = "define_object("
				+ "'PUBLIC'," // accessModifier
				+ "array()," // objectModifier
				+ "'CLASS'," // objectType
				+ "'test'," // name
				+ "array()," // superclasses
				+ "array()," // interfaces
				+ "null," // enum list
				+ "array()," // element list
				+ "array()," // annotations
				+ "null," // containing class
				+ "'class comment'," // class comment
				+ "array()" // generic parameters
				+ ")"; //
		doCompile(script);
		ObjectDefinition clazz = getObjectDefinition("test");
		assertEquals(AccessModifier.PUBLIC, clazz.getAccessModifier());
		assertTrue(clazz.getObjectModifiers().isEmpty());
		assertEquals(ObjectType.CLASS, clazz.getObjectType());
		assertEquals("test", clazz.getFQCN().getFQCN());
		assertEquals(new HashSet<>(Arrays.asList(Mixed.TYPE)), clazz.getSuperclasses());
		assertEquals(new HashSet<CClassType>(), clazz.getInterfaces());
		// TODO: Check enum list, once it's implemented
		assertTrue(clazz.getElements().isEmpty());
		assertNull(clazz.getContainingClass());
		// TODO: This is currently skipped
		// assertEquals("class comment", clazz.getElementComment().getBody());
		// TODO: Generic parameters aren't properly implemented yet
		assertEquals(new ArrayList<Object>(), clazz.getGenericParameters());
	}

	@Test
	public void testNoSyntaxSugarWithContainingClass() throws Exception {
		String script = "define_object("
				+ "'PUBLIC'," // accessModifier
				+ "array()," // objectModifier
				+ "'CLASS'," // objectType
				+ "'outer'," // name
				+ "array()," // superclasses
				+ "array()," // interfaces
				+ "null," // enum list
				+ "array()," // element list
				+ "array()," // annotations
				+ "null," // containing class
				+ "'class comment'," // class comment
				+ "array()" // generic parameters
				+ ");\n"
				+ "define_object("
				+ "'PUBLIC'," // accessModifier
				+ "array()," // objectModifier
				+ "'CLASS'," // objectType
				+ "'inner'," // name
				+ "array()," // superclasses
				+ "array()," // interfaces
				+ "null," // enum list
				+ "array()," // element list
				+ "array()," // annotations
				+ "__to_class_reference__('outer')," // containing class
				+ "'class comment'," // class comment
				+ "array()" // generic parameters
				+ ")"; //
		doCompile(script);
		ObjectDefinition clazz = getObjectDefinition("inner");
		assertEquals(CClassType.get(FullyQualifiedClassName.forFullyQualifiedClass("outer")),
				clazz.getContainingClass());
	}

	@Test
	public void testNoSyntaxSugarPrivateClass() throws Exception {
		String script = "define_object("
				+ "'PRIVATE'," // accessModifier
				+ "array()," // objectModifier
				+ "'CLASS'," // objectType
				+ "'test'," // name
				+ "array()," // superclasses
				+ "array()," // interfaces
				+ "null," // enum list
				+ "array()," // element list
				+ "array()," // annotations
				+ "null," // containing class
				+ "'class comment'," // class comment
				+ "array()" // generic parameters
				+ ")"; //
		doCompile(script);
		ObjectDefinition clazz = getObjectDefinition("test");
		assertEquals(AccessModifier.PRIVATE, clazz.getAccessModifier());
	}

	@Test
	public void testNoSyntaxSugarObjectModifiers() throws Exception {
		String script = "define_object("
				+ "'PUBLIC'," // accessModifier
				+ "array('STATIC', 'FINAL')," // objectModifier
				+ "'CLASS'," // objectType
				+ "'test'," // name
				+ "array()," // superclasses
				+ "array()," // interfaces
				+ "null," // enum list
				+ "array()," // element list
				+ "array()," // annotations
				+ "null," // containing class
				+ "'class comment'," // class comment
				+ "array()" // generic parameters
				+ ")"; //
		doCompile(script);
		ObjectDefinition clazz = getObjectDefinition("test");
		assertEquals(EnumSet.of(ObjectModifier.STATIC, ObjectModifier.FINAL), clazz.getObjectModifiers());
	}

}
