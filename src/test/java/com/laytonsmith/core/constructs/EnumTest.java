/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.constructs;

import com.laytonsmith.core.FullyQualifiedClassName;
import com.laytonsmith.core.natives.interfaces.MEnumType;
import com.laytonsmith.core.natives.interfaces.MEnumTypeValue;
import com.laytonsmith.testing.AbstractIntegrationTest;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Cailin
 */
public class EnumTest extends AbstractIntegrationTest {

	public EnumTest() {
	}

	@Test(expected = ClassNotFoundException.class)
	public void testEnumIsNotFound() throws Exception {
		NativeTypeList.getNativeEnum(FullyQualifiedClassName
				.forFullyQualifiedClass("ms.lang.oaijweoifjaoiwejfoiajoiwefjiaojwefjoiajweoifjaowjefjaowef"));
	}

	@Test
	public void testEnumIsFound() throws ClassNotFoundException {
		NativeTypeList.getNativeEnum(FullyQualifiedClassName.forFullyQualifiedClass("ms.lang.ArraySortType"));
	}

	@Test
	public void testEnumTypeIsFound() throws ClassNotFoundException {
		NativeTypeList.getNativeEnumType(FullyQualifiedClassName.forFullyQualifiedClass("ms.lang.ArraySortType"));
	}

	@Test
	public void testEnumTypeValueIsCorrect() throws ClassNotFoundException {
		MEnumType t = NativeTypeList.getNativeEnumType(FullyQualifiedClassName
				.forNativeEnum(CArray.ArraySortType.class));
		MEnumTypeValue v = t.values().get(0);
		assertEquals(0, v.ordinal());
		assertEquals("REGULAR", v.name());
		try {
			t.values().set(0, v);
			fail();
		} catch (Exception e) {
			// pass
		}
	}

//	@MEnum("ms.lang.TestEnum")
//	public static enum TestEnum implements SimpleDocumentation {
//		ONE,
//		TWO,
//		THREE;
//
//
//		public static String enumDocs() {
//			return "enum docs";
//		}
//
//		public static Version enumSince() {
//			return MSVersion.V0_0_0;
//		}
//
//		@Override
//		public String docs() {
//			return "docs for value";
//		}
//
//		@Override
//		public String getName() {
//			return name();
//		}
//
//		@Override
//		public Version since() {
//			return MSVersion.V0_0_0;
//		}
//
//	}
}
