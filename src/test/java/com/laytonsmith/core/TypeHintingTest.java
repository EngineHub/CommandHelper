package com.laytonsmith.core;

import org.junit.After;
import org.junit.Before;

/**
 *
 *
 */
public class TypeHintingTest {

	public TypeHintingTest() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	//Meh. I'll re-investigate this later maybe. It may not be very helpful.
//	@Test public void testSimpleCase1(){
//		assertTrue(TypeHinting.Generate(STRING).matches(STRING));
//	}
//	
//	@Test public void testSimpleCase2(){
//		assertTrue(TypeHinting.Generate(STRING, INT).matches(STRING, INT));
//		assertFalse(TypeHinting.Generate(STRING, INT).matches(ARRAY));
//	}
//	
//	@Test public void testComplex1(){
//		assertTrue(TypeHinting.Generate(STRING, OPTIONAL, STRING).matches(STRING));
//		assertTrue(TypeHinting.Generate(INT, VARARGS, STRING).matches(INT, STRING, STRING, STRING));
//		assertTrue(TypeHinting.Generate(INT, VARARGS, STRING).matches(INT));
//	}
//	
//	@Test public void testComplex2(){
//		TypeHinting complex = TypeHinting.Generate(OPTIONAL, STRING, STRING, INT);
//		assertTrue(complex.matches(STRING, STRING, INT));
//		assertTrue(complex.matches(STRING, INT));
//		assertFalse(complex.matches(STRING, STRING, INT));
//		assertFalse(complex.matches(STRING, INT, INT, INT));
//	}
//	
//	@Test(expected=Error.class) 
//	public void testBadSpecification1(){
//		TypeHinting.Generate(OPTIONAL, STRING, VARARGS, STRING, INT, OPTIONAL, INT);
//	}
}
