package com.laytonsmith.core.constructs;

import com.laytonsmith.core.natives.interfaces.Mixed;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 *
 */
public class InstanceofUtilTest {

	@Test
	public void testInstanceofUtil() {
		assertTrue(InstanceofUtil.isInstanceof(CInt.getFromPool(0, Target.UNKNOWN), CInt.class));
		assertTrue(InstanceofUtil.isInstanceof(CInt.getFromPool(0, Target.UNKNOWN), CNumber.class));
		assertTrue(InstanceofUtil.isInstanceof(CInt.getFromPool(0, Target.UNKNOWN), Mixed.class));
		assertFalse(InstanceofUtil.isInstanceof(CInt.getFromPool(0, Target.UNKNOWN), CString.class));
	}
}
