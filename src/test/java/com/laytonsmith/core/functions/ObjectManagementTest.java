/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.functions;

import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.MethodScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.objects.ObjectDefinition;
import com.laytonsmith.core.objects.ObjectType;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ObjectManagementTest {

	static Set<Class<? extends Environment.EnvironmentImpl>> envs = Environment.getDefaultEnvClasses();

	public static void Run(String code, CompilerEnvironment env) throws Exception {
		GlobalEnv gEnv = Static.GenerateStandaloneEnvironment(false).getEnv(GlobalEnv.class);
		gEnv.SetCustom("define_object.noQualifyClasses", true);
		Environment eenv = Environment.createEnvironment(gEnv, env);
		Run(code, eenv);
	}

	public static void Run(String code, Environment env) throws Exception {
		MethodScriptCompiler.compile(MethodScriptCompiler.lex(code, null, new File("test.ms"), true), env, envs);
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



}
