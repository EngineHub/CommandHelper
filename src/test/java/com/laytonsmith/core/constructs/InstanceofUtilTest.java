package com.laytonsmith.core.constructs;

import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.AbstractIntegrationTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 *
 */
public class InstanceofUtilTest extends AbstractIntegrationTest {

	@Test
	public void testInstanceofUtil() throws Exception {
		Environment env = Static.GenerateStandaloneEnvironment(false);
		assertTrue(InstanceofUtil.isInstanceof(CBoolean.FALSE, Booleanish.class, env));
		assertTrue(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CInt.class, env));
		assertTrue(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CNumber.class, env));
		assertTrue(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), Mixed.class, env));
		assertFalse(InstanceofUtil.isInstanceof(new CInt(0, Target.UNKNOWN), CString.class, env));
	}
}
