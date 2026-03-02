package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.generics.GenericParameters;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.testing.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CFixedArrayTest extends AbstractIntegrationTest {

	Environment env;

	@Before
	public void setUp() throws Exception {
		env = Static.GenerateStandaloneEnvironment();
	}

	@Test
	public void testBasic1() {
		CFixedArray fa = new CFixedArray(Target.UNKNOWN, GenericParameters.emptyBuilder(CFixedArray.TYPE).addNativeParameter(CInt.TYPE, null).buildNative(), 10);
		assertEquals(10, fa.size(env));
		assertEquals(false, fa.canBeAssociative());
		assertEquals(CNull.NULL, fa.get(0, Target.UNKNOWN, env));
		fa.set(0, new CInt(10, Target.UNKNOWN), Target.UNKNOWN, env);
		assertEquals(10, ((CInt) fa.get(0, Target.UNKNOWN, env)).val);
	}
}
