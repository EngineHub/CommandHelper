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
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ObjectManagementTest {

	public static void Run(String code, CompilerEnvironment env) throws Exception {
		GlobalEnv gEnv = Static.GenerateStandaloneEnvironment(false).getEnv(GlobalEnv.class);
		Environment eenv = Environment.createEnvironment(gEnv, env);
		MethodScriptCompiler.compile(MethodScriptCompiler.lex(code, new File("test.ms"), true), eenv);
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
		System.out.println(env.getObjectDefinitionTable().get(FullyQualifiedClassName.forFullyQualifiedClass("A")));
	}

}
