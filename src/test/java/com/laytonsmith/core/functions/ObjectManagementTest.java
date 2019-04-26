/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.ParseTree;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.objects.Element;
import com.laytonsmith.core.objects.ObjectDefinition;
import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.laytonsmith.testing.StaticTest.SRun;

/**
 *
 */
public class ObjectManagementTest {

	public static Environment getEnv(CompilerEnvironment cEnv) throws Exception {
		GlobalEnv gEnv = Static.GenerateStandaloneEnvironment(false).getEnv(GlobalEnv.class);
		gEnv.SetCustom("define_object.noQualifyClasses", true);
		Environment eenv = Environment.createEnvironment(gEnv, cEnv);
		return eenv;
	}

	public static ParseTree Run(String code, CompilerEnvironment env) throws Exception {
		Environment eenv = getEnv(env);
		return Run(code, eenv);
	}

	public static ParseTree Run(String code, Environment env) throws Exception {
		return MethodScriptCompiler.compile(MethodScriptCompiler.lex(code, new File("test.ms"), true), env);
	}

	public static void Execute(String code, CompilerEnvironment env) throws Exception {
		Environment eenv = getEnv(env);
		Execute(code, eenv);
	}

	public static void Execute(String code, Environment env) throws Exception {
		MethodScriptCompiler.execute(Run(code, env), env, null, null);
	}

	public ObjectManagementTest() {
		StaticTest.InstallFakeServerFrontend();
	}

	@Test
	public void testDefineEmptyClass() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
	}

	@Test
	public void testDefineEmptyClassWithSingleSuperclass() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		// class B extends A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "B," // 3 - Object name
				+ "array(A)," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		ObjectDefinition B = env.getObjectDefinitionTable().get(FullyQualifiedClassName
				.forFullyQualifiedClass("B"));
		B.qualifyClasses(Environment.createEnvironment(env));
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("A"),
				new ArrayList<>(B.getSuperclasses()).get(0).getFQCN());
	}

	@Test
	public void testDefineEmptyClassWithDoubleSuperclass() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		// class B {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "B," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		// class C extends A, B {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "C," // 3 - Object name
				// The order of this set should be deterministic, though undefined
				+ "array(B, A)," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		ObjectDefinition C = env.getObjectDefinitionTable().get(FullyQualifiedClassName
				.forFullyQualifiedClass("C"));
		C.qualifyClasses(Environment.createEnvironment(env));
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("B"),
				new ArrayList<>(C.getSuperclasses()).get(0).getFQCN());
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("A"),
				new ArrayList<>(C.getSuperclasses()).get(1).getFQCN());
	}

	@Test
	public void testDefineEmptyInterface() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// interface A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "INTERFACE," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		ObjectDefinition A = env.getObjectDefinitionTable().get(FullyQualifiedClassName
				.forFullyQualifiedClass("A"));
		A.qualifyClasses(Environment.createEnvironment(env));
		assertEquals(ObjectType.INTERFACE, A.getObjectType());
	}

	@Test
	public void testDefineEmptyClassWithSingleInterface() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "INTERFACE," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		// class B extends A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "INTERFACE," // 2 - Object type
				+ "B," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array(A)," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		ObjectDefinition B = env.getObjectDefinitionTable().get(FullyQualifiedClassName
				.forFullyQualifiedClass("B"));
		B.qualifyClasses(Environment.createEnvironment(env));
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("A"),
				new ArrayList<>(B.getInterfaces()).get(0).getFQCN());
	}

	@Test
	public void testDefineEmptyClassWithDoubleInterface() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "INTERFACE," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		// class B {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "INTERFACE," // 2 - Object type
				+ "B," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		// class C extends A, B {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "C," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array(B, A)," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		ObjectDefinition C = env.getObjectDefinitionTable().get(FullyQualifiedClassName
				.forFullyQualifiedClass("C"));
		C.qualifyClasses(Environment.createEnvironment(env));
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("B"),
				new ArrayList<>(C.getInterfaces()).get(0).getFQCN());
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("A"),
				new ArrayList<>(C.getInterfaces()).get(1).getFQCN());
	}

	@Test(expected = ConfigCompileException.class)
	public void testThatAtRuntimeClassesAreImmediatelyQualified() throws Exception {
		CompilerEnvironment cEnv = new CompilerEnvironment();
		GlobalEnv gEnv = Static.GenerateStandaloneEnvironment(false).getEnv(GlobalEnv.class);
		Environment env = Environment.createEnvironment(gEnv, cEnv);
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "C," // 3 - Object name
				+ "array()," // 4 - Superclasses
				// B and A are not defined yet
				+ "array(B, A)," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", env);
	}

	@Test
	public void testDefineEmptyClassWithLongerName() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "'test.Test'," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
		assertEquals(FullyQualifiedClassName.forFullyQualifiedClass("test.Test"),
				env.getObjectDefinitionTable().get(FullyQualifiedClassName.forFullyQualifiedClass("test.Test"))
						.getFQCN());
	}

	@Test(expected = ConfigCompileException.class)
	public void testNativeClassDefinitionWithoutNativeClassFails() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array(NATIVE)," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "'test.Test'," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", env);
	}

//	@Test
//	@Ignore("Need to be able to provide a constructor before this test is possible")
//	public void testNativeClassDefinitionWithNativeClassSucceeds() throws Exception {
//		CompilerEnvironment env = new CompilerEnvironment();
//		assertTrue(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
//		// class A {}
//		Run("define_object(DEFAULT," // 0 - Access modifier
//				+ "array(NATIVE)," // 1 - Object modifier
//				+ "CLASS," // 2 - Object type
//				+ "'ms.lang.string'," // 3 - Object name
//				+ "array()," // 4 - Superclasses
//				+ "array()," // 5 - Interfaces
//				+ "null," // 6 - Enum list
//				+ "associative_array()," // 7 - element definitions
//				+ "array()," // 8 - annotations
//				+ "null," // 9 - containing class
//				+ "''," // 10 - class comment
//				+ "null)", // 11 - Generic parameters
//				env);
//		assertFalse(env.getObjectDefinitionTable().getObjectDefinitionSet().isEmpty());
//		// TODO: None of the other information is right. This test needs a bit of updating to ensure
//		// everything is defined properly, once the native classes are complete.
//	}

	@Test(expected = ConfigCompileException.class)
	public void testDuplicateDefinitionsCauseErrors() throws Exception {
		CompilerEnvironment env = new CompilerEnvironment();
		// class A {}
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		Run("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "associative_array()," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
	}

	@Test
	public void testFieldCreation() throws Exception {
		assertEquals("DEFAULT TestType @test", SRun("create_field(DEFAULT, array(), TestType, '@test', null);", null));
		assertEquals("DEFAULT STATIC TestType @test",
				SRun("create_field(DEFAULT, array(STATIC), TestType, '@test', null);", null));
		assertEquals("ms.lang.FieldDefinition",
				SRun("typeof(create_field(DEFAULT, array(), TestType, '@test', null));", null));
		try {
			// Invalid variable name, needs @ sign
			SRun("create_field(DEFAULT, array(), TestType, 'test', null)", null);
			fail();
		} catch (Exception ex) {
			// pass
		}
	}

	@Test
	public void testFieldCreationInClass() throws Exception {
		CompilerEnvironment cEnv = new CompilerEnvironment();
		Environment env = getEnv(cEnv);
		// class A {}
		Execute("define_object(DEFAULT," // 0 - Access modifier
				+ "array()," // 1 - Object modifier
				+ "CLASS," // 2 - Object type
				+ "A," // 3 - Object name
				+ "array()," // 4 - Superclasses
				+ "array()," // 5 - Interfaces
				+ "null," // 6 - Enum list
				+ "array("
				+ "	create_field(DEFAULT, array(), string, '@test', null)"
				+ ")," // 7 - element definitions
				+ "array()," // 8 - annotations
				+ "null," // 9 - containing class
				+ "''," // 10 - class comment
				+ "null)", // 11 - Generic parameters
				env);
		ObjectDefinition obj = cEnv.getObjectDefinitionTable().get(FullyQualifiedClassName.forFullyQualifiedClass("A"));
		obj.qualifyClasses(env);
		assertFalse(obj.getElements().isEmpty());
		Element e = obj.getElements().get(0);
		assertEquals("@test", e.getElementName());
	}

}
