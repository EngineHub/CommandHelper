package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.testing.StaticTest;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class LeftHandSideTypeTest {

	Environment env;

	@Before
	public void setup() throws Exception {
		StaticTest.InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment();
	}

	@Test
	public void testInterfacesAreCorrect() throws Exception {
		LeftHandSideType type = LeftHandSideType.fromCClassTypeUnion(Target.UNKNOWN, CString.TYPE, CArray.TYPE);
		List<CClassType> interfaces = Arrays.asList(type.getTypeInterfaces(env));
		assertTrue(interfaces.contains(com.laytonsmith.core.natives.interfaces.Iterable.TYPE));
		assertFalse(interfaces.contains(Booleanish.TYPE));
	}

	@Test
	public void testUnionsAreNormalized() throws Exception {
		assertTrue(LeftHandSideType.fromCClassTypeUnion(Target.UNKNOWN, CInt.TYPE, CString.TYPE).equals(
				LeftHandSideType.fromCClassTypeUnion(Target.UNKNOWN, CString.TYPE, CInt.TYPE)));
	}

	@Test
	public void testUnionsWithSupertypesAreNormalized() throws Exception {
		assertEquals(CPrimitive.TYPE.asLeftHandSideType(),
				LeftHandSideType.fromCClassTypeUnion(Target.UNKNOWN, CInt.TYPE, CPrimitive.TYPE));
		assertEquals(CPrimitive.TYPE.asLeftHandSideType(),
				LeftHandSideType.fromCClassTypeUnion(Target.UNKNOWN, CPrimitive.TYPE, CInt.TYPE));
	}
}
