package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.testing.AbstractIntegrationTest;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 *
 */
public class ReflectionUtilsTest extends AbstractIntegrationTest {

	public ReflectionUtilsTest() {
	}

	class A {

		B bObj = new B();
	}

	class B {

		C cObj = new C();
	}

	class C {

		String obj = "string";
	}

	@Test
	public void testRecursiveGet() {
		A a = new A();
		String result = (String) ReflectionUtils.get(A.class, a, "bObj.cObj.obj");
		assertEquals("string", result);
	}

	@Test
	public void testFuzzyLookup() {
		Class expected = ReflectionUtils.class;
		Class actual = ClassDiscovery.getDefaultInstance().forFuzzyName("com.laytonsmith.Pur.*", "ReflectionUtils").loadClass();
		assertEquals(expected, actual);
	}

}
