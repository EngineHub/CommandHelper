package com.laytonsmith.PureUtilities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		
	}
	
	class A { B bObj = new B(); }
	class B { C cObj = new C(); }
	class C { String obj = "string"; }
	@Test public void testRecursiveGet(){
		A a = new A();
		String result = (String)ReflectionUtils.get(A.class, a, "bObj.cObj.obj");
		assertEquals("string", result);
	}

}
