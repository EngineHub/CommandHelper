package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.testing.StaticTest;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author lsmith
 */
public class ReflectionUtilsTest {
	
	public ReflectionUtilsTest() {
	}
	
	
	@Before
	public void setUp() {
		StaticTest.InstallFakeServerFrontend();
	}
	
	class A { B bObj = new B(); }
	class B { C cObj = new C(); }
	class C { String obj = "string"; }
	@Test public void testRecursiveGet(){
		A a = new A();
		String result = (String)ReflectionUtils.get(A.class, a, "bObj.cObj.obj");
		assertEquals("string", result);
	}
	
	@Test public void testFuzzyLookup(){
		Class expected = ReflectionUtils.class;
		Class actual = ClassDiscovery.getDefaultInstance().forFuzzyName("com.laytonsmith.Pur.*", "ReflectionUtils").loadClass();
		assertEquals(expected, actual);
	}

}
