package com.laytonsmith.core.objects;

import com.laytonsmith.core.compiler.CompilerEnvironment;
import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.NativeTypeList;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.testing.StaticTest;
import java.io.File;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 */
@Ignore("Ignored for now, while the features are being slowly rolled out")
public class ObjectDefinitionTableTest {

	Environment env;

	@Before
	public void Before() {
		StaticTest.InstallFakeServerFrontend();
		env = Environment.createEnvironment(new CompilerEnvironment(),
			new GlobalEnv(GlobalEnv.NO_OP_EXECUTION_QUEUE, GlobalEnv.NO_OP_PROFILER, GlobalEnv.NO_OP_PN,
					new File("."), GlobalEnv.NO_OP_PROFILES, GlobalEnv.NO_OP_TASK_MANAGER));
	}

	@Test
	public void testNativeTypeListIsProperlyAdded() {
		ObjectDefinitionTable table = ObjectDefinitionTable.GetNewInstance(env);
		try {
			ObjectDefinition d = table.get(CString.class);
			table.add(d, Target.UNKNOWN);
			fail();
		} catch (DuplicateObjectDefintionException e) {
			// pass
		}
		// Sanity check
		assertFalse(NativeTypeList.getNativeTypeList().isEmpty());
		// -2 is for null and void
		assertEquals(NativeTypeList.getNativeTypeList().size() - 2, table.getObjectDefinitionSet().size());
	}

	@Test
	public void testStringIsProperlyDefined() {
		ObjectDefinitionTable table1 = ObjectDefinitionTable.GetNewInstance(env);
		ObjectDefinition string1 = table1.get(CString.class);
		ObjectDefinitionTable table2 = ObjectDefinitionTable.GetNewInstance(env);
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
	public void testExposedPropertiesAreProperlyDefined() {

	}

}
