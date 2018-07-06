package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.natives.interfaces.ObjectModifier;
import com.laytonsmith.core.natives.interfaces.ObjectType;
import com.laytonsmith.testing.StaticTest;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author cailin
 */
public class ClassInfoTest {

	@Before
	public void before() {
		StaticTest.InstallFakeServerFrontend();
	}

	@Test
	public void testAllInterfacesReturnNothingForGetInterfaces() throws Exception {
		List<String> failures = new ArrayList<>();
		for(String t : NativeTypeList.getNativeTypeList()) {
			Mixed m = ReflectionUtils.instantiateUnsafe(NativeTypeList.getNativeClassOrInterfaceRunner(t));
			if(m.getObjectType() == ObjectType.INTERFACE) {
				try {
					if(m.getInterfaces() != null && m.getInterfaces().length > 0) {
						failures.add(m.getClass().getName()
								+ " is an interface, but getInterfaces() has returned a non-zero list");
					}
				} catch (UnsupportedOperationException ex) {
					failures.add(m.getClass().getName() + " cannot throw an exception for getInterfaces()");
				}
			}
		}
		if(!failures.isEmpty()) {
			fail("One or more failures has occured:\n" + StringUtils.Join(failures, "\n"));
		}
	}

	@Test
	public void testOnlyContainedClassesHaveVariousModifiers() throws Exception {
		List<String> failures = new ArrayList<>();
		Set<ObjectModifier> allowed = EnumSet.of(ObjectModifier.PUBLIC, ObjectModifier.PACKAGE, ObjectModifier.FINAL);
		for(String t : NativeTypeList.getNativeTypeList()) {
			Mixed m = ReflectionUtils.instantiateUnsafe(NativeTypeList.getNativeClassOrInterfaceRunner(t));
			if(m.getContainingClass() == null) {
				for(ObjectModifier i : m.getObjectModifiers()) {
					if(!allowed.contains(i)) {
						failures.add(m.getClass().getName() + " contains an illegal modifier, because it is an outer"
								+ " class, but the only allowed modifiers are: " + allowed.toString());
					}
				}
			}
		}
		if(!failures.isEmpty()) {
			fail("One or more failures has occured:\n" + StringUtils.Join(failures, "\n"));
		}
	}
}
