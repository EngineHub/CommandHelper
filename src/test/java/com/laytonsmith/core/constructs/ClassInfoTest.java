package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.ObjectModifier;
import com.laytonsmith.core.objects.ObjectType;
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
		for(FullyQualifiedClassName t : NativeTypeList.getNativeTypeList()) {
			Mixed m = NativeTypeList.getInvalidInstanceForUse(t);
			if(m.getObjectType() == ObjectType.INTERFACE) {
				try {
					if(m.getInterfaces() != null && m.getInterfaces().length > 0) {
						failures.add(m.getClass().getName() + " is an interface, but getInterfaces() has returned a non-zero list");
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
		Set<ObjectModifier> allowed = EnumSet.of(ObjectModifier.FINAL, ObjectModifier.ABSTRACT);
		for(FullyQualifiedClassName t : NativeTypeList.getNativeTypeList()) {
			Mixed m = NativeTypeList.getInvalidInstanceForUse(t);
			if(m.getContainingClass() == null) {
				for(ObjectModifier i : m.getObjectModifiers()) {
					if(!allowed.contains(i)) {
						failures.add(m.getClass().getName() + " contains an illegal modifier (" + i + "), because it is an outer"
								+ " class, but the only allowed modifiers are: " + allowed.toString());
					}
				}
			}
		}
		if(!failures.isEmpty()) {
			fail("One or more failures has occured:\n" + StringUtils.Join(failures, "\n"));
		}
	}

	@Test
	public void testAllTypeofClassesDoNotThrowUnsupportedOperationException() throws Exception {
		List<String> failures = new ArrayList<>();
		for(FullyQualifiedClassName fqcn : NativeTypeList.getNativeTypeList()) {
			if(CVoid.TYPE.getFQCN().equals(fqcn) || CNull.TYPE.getFQCN().equals(fqcn)) {
				continue;
			}
			Mixed m = NativeTypeList.getInvalidInstanceForUse(fqcn);
			try {
				m.getSuperclasses();
			} catch (UnsupportedOperationException e) {
				failures.add("getSuperclasses in " + m.getClass() + " throws an UnsupportedOperationException. This"
						+ " is only allowed in phantom classes.");
			}
			try {
				m.getInterfaces();
			} catch (UnsupportedOperationException e) {
				failures.add("getInterfaces in " + m.getClass() + " throws an UnsupportedOperationException. This"
						+ " is only allowed in phantom classes.");
			}
		}

		if(!failures.isEmpty()) {
			fail("One or more failures has occured:\n" + StringUtils.Join(failures, "\n"));
		}
	}
}
