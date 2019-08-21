package com.laytonsmith.core.constructs;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.testing.StaticTest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/**
 *
 * @author cailin
 */
public class TestCClassType {

	static Environment env;

	@Before
	public void load() throws Exception {
		StaticTest.InstallFakeServerFrontend();
		env = Static.GenerateStandaloneEnvironment(false);
		env = env.cloneAndAdd(new CommandHelperEnvironment());
	}

	private static CClassType get(String... types) throws ClassNotFoundException {
		return CClassType.get(Stream.of(types).map(e -> FullyQualifiedClassName.forName(e, Target.UNKNOWN, env))
				.collect(Collectors.toList()).toArray(new FullyQualifiedClassName[0]));
	}

	@Test
	public void testInitial() throws Exception {
		NativeTypeList.getNativeClass(FullyQualifiedClassName.forFullyQualifiedClass("ms.lang.array"));
		NativeTypeList.getNativeClass(FullyQualifiedClassName.forFullyQualifiedClass("ms.lang.mixed"));
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
		assertTrue(get("array").doesExtend(get("Booleanish")));
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
	public void testThatNonImplementsReturnsEMPTY_CLASS_ARRAY() throws Exception {
		SortedSet<String> oops = new TreeSet<>();
		for(FullyQualifiedClassName fqcn : NativeTypeList.getNativeTypeList()) {
			if("void".equals(fqcn.getFQCN()) || "null".equals(fqcn.getFQCN())) {
				continue;
			}
			Mixed m = NativeTypeList.getInvalidInstanceForUse(fqcn);
			CClassType[] cti = m.getInterfaces();
			if(cti.length == 0) {
				if(cti != CClassType.EMPTY_CLASS_ARRAY) {
					oops.add(fqcn + "(" + m.getClass() + ") creates a new empty array in getInterfaces,"
							+ " and needs to be changed to return"
							+ " CClassType.EMPTY_CLASS_ARRAY");
				}
			}
			CClassType[] cts = m.getSuperclasses();
			if(cts.length == 0) {
				if(cts != CClassType.EMPTY_CLASS_ARRAY) {
					oops.add(fqcn + "(" + m.getClass() + ") creates a new empty array in getSuperclasses,"
							+ " and needs to be changed to return"
							+ " CClassType.EMPTY_CLASS_ARRAY");
				}
			}
		}
		if(!oops.isEmpty()) {
			fail(StringUtils.Join(oops, "\n"));
		}
	}


	@Test
	public void testEnumDereference() throws Exception {
//		assertEquals("REGULAR", StaticTest.SRun("ms.lang.ArraySortType[0]", null));
//		assertEquals("REGULAR", StaticTest.SRun("ms.lang.ArraySortType['REGULAR']", null));
		assertEquals("REGULAR", StaticTest.SRun("ArraySortType[0]", null));
		assertEquals("REGULAR", StaticTest.SRun("ArraySortType['REGULAR']", null));
		assertEquals("ms.lang.ClassType", StaticTest.SRun("typeof(ArraySortType)", null));
		assertEquals("ms.lang.ArraySortType", StaticTest.SRun("typeof(ArraySortType['REGULAR'])", null));
	}
}
