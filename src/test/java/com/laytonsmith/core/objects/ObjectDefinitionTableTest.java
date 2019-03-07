package com.laytonsmith.core.objects;

import com.laytonsmith.core.constructs.CPrimitive;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.NativeTypeList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ObjectDefinitionTableTest {

	@Test
	public void testNativeTypeListIsProperlyAdded() {
		ObjectDefinitionTable table = ObjectDefinitionTable.GetNewInstance();
		// Should be allowed
		table.addNativeTypes();
		table.addNativeTypes();
		try {
			ObjectDefinition d = table.get(CString.class);
			table.add(d);
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
		ObjectDefinitionTable table1 = ObjectDefinitionTable.GetNewInstance();
		ObjectDefinition string1 = table1.get(CString.class);
		ObjectDefinitionTable table2 = ObjectDefinitionTable.GetNewInstance();
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
