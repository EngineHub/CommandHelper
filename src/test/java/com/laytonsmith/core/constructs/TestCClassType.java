package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.StaticTest;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.junit.Ignore;

/**
 *
 * @author cailin
 */
public class TestCClassType {

	@Before
	public void load() {
		StaticTest.InstallFakeServerFrontend();
	}

	private static CClassType get(String... types) {
		return CClassType.get(types);
	}

	@Test
	public void testInitial() throws Exception {
		NativeTypeList.getNativeClass("array");
		NativeTypeList.getNativeClass("mixed");
	}

	@Test
	public void testEquals() throws Exception {
		assertTrue(get("mixed").equals(get("mixed")));
		assertFalse(get("mixed").equals(get("array")));
	}

	@Test
	public void testEqualsWithTypeUnion() throws Exception {
		assertTrue(get("array", "int").equals(get("int", "array")));
		assertFalse(get("array", "int").equals(get("string", "array")));
	}

	@Test
	public void testDoesExtend() throws Exception {
		assertTrue(get("array").doesExtend(get("mixed")));
		assertTrue(get("array").doesExtend(get("ArrayAccess")));
		assertFalse(get("array").doesExtend(get("string")));
		assertTrue(get("array").doesExtend(get("array")));
	}

	@Test
	@Ignore("Ignored for now, but must come back to this soon")
	public void testGetMostCommonSuperClass() throws Exception {
//	assertTrue(get("double", "int").getMostCommonSuperClass().equals(get("number")));
//	assertTrue(get("double").getMostCommonSuperClass().equals(get("double")));
	}

	@Test
	@Ignore("Ignored for now, but must come back to this soon")
	public void testInterface() throws Exception {
//	assertTrue(get("Sizeable").getUnderlyingClass() == Sizeable.class);
	}

	@Test
	@Ignore("Ignored for now, but must come back to this soon")
	public void testThatNonImplementsReturnsEMPTY_CLASS_ARRAY() throws Exception {
		SortedSet<String> oops = new TreeSet<>();
		Set<Class<? extends Mixed>> cc = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotationThatExtend(typeof.class, Mixed.class);
		for(Class<? extends Mixed> c : cc) {
			Mixed m = ReflectionUtils.instantiateUnsafe(c);
			CClassType[] ct = m.getInterfaces();
			if(ct.length == 0) {
				if(ct != CClassType.EMPTY_CLASS_ARRAY) {
					oops.add(c.getName() + " creates a new empty array in getInterfaces, and needs to be changed to return"
							+ " CClassType.EMPTY_CLASS_ARRAY");
				}
			}
		}
		if(!oops.isEmpty()) {
			fail(StringUtils.Join(oops, "\n"));
		}
	}
}
