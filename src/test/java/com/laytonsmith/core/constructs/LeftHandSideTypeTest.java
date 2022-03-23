package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class LeftHandSideTypeTest {

	Environment env;

	@Before
	public void setup() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@Test
	public void testInterfacesAreCorrect() throws Exception {
		LeftHandSideType type = LeftHandSideType.fromCClassTypeUnion(Target.UNKNOWN, CString.TYPE, CArray.TYPE);
		List<CClassType> interfaces = Arrays.asList(type.getTypeInterfaces(env));
		Assert.assertTrue(interfaces.contains(com.laytonsmith.core.natives.interfaces.Iterable.TYPE));
		Assert.assertFalse(interfaces.contains(Booleanish.TYPE));
	}
}
