package com.laytonsmith.PureUtilities;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
@SuppressWarnings({"checkstyle:parametername", "checkstyle:localvariablename", "checkstyle:membername"})
public class ObjectHelpersTest {

	@ObjectHelpers.StandardField
	public static class ObjectHelperClass1 {
		Object object = null;
		byte _byte = 0;
		short _short = 1;
		int _int = 2;
		long _long = 3;
		double _double = 4.0;
		float _float = 5.0f;
		boolean _boolean = true;
		char _char = 'c';
		String string = "string";
		Boolean boxedBoolean = Boolean.TRUE;

		@Override
		@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
		public boolean equals(Object obj) {
			return ObjectHelpers.DoEquals(this, obj);
		}

		@Override
		public int hashCode() {
			return ObjectHelpers.DoHashCode(this);
		}

		@Override
		public String toString() {
			return ObjectHelpers.DoToString(this);
		}

	}

	public static class ObjectHelperClass2 {
		@ObjectHelpers.StandardField
		int a = 1;
		// Should not be considered in the calculation for equals
		@ObjectHelpers.HashCode
		int b = 2;

		@ObjectHelpers.HashCode
		Object o = null;

		int c = 5;

		@ObjectHelpers.ToString
		String s = "string";

		@Override
		@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
		public boolean equals(Object obj) {
			return ObjectHelpers.DoEquals(this, obj);
		}

		@Override
		public int hashCode() {
			return ObjectHelpers.DoHashCode(this);
		}

		@Override
		public String toString() {
			return ObjectHelpers.DoToString(this);
		}

	}

	@Test
	@SuppressWarnings("ObjectEqualsNull")
	public void testEquals1() {
		// TODO review the generated test code and remove the default call to fail.
		ObjectHelperClass1 a = new ObjectHelperClass1();
		ObjectHelperClass1 b = new ObjectHelperClass1();
		assertTrue(a.equals(b));
		assertFalse(a.equals(null));
		assertFalse(a.equals(new Object()));
		b._boolean = false;
		assertFalse(a.equals(b));
	}

	@Test
	public void testEquals2() {
		ObjectHelperClass2 a = new ObjectHelperClass2();
		ObjectHelperClass2 b = new ObjectHelperClass2();
		assertTrue(a.equals(b));
		b.b = 33;
		assertTrue(a.equals(b));
		b.a = 66;
		assertFalse(a.equals(b));
	}

	@Test
	public void testHashCode1() {
		ObjectHelperClass1 a = new ObjectHelperClass1();
		ObjectHelperClass1 b = new ObjectHelperClass1();
		assertEquals(a.hashCode(), b.hashCode());
		b.string = null;
		assertNotEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testHashCode2() {
		ObjectHelperClass2 a = new ObjectHelperClass2();
		ObjectHelperClass2 b = new ObjectHelperClass2();
		assertEquals(a.hashCode(), b.hashCode());
		b.c = 66;
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void testToString1() {
		ObjectHelperClass1 a = new ObjectHelperClass1();
		assertEquals("ObjectHelperClass1 {object=null, _byte=0, _short=1, _int=2, _long=3, _double=4.0, _float=5.0,"
				+ " _boolean=true, _char=c, string=string, boxedBoolean=true}", a.toString());
	}

	@Test
	public void testToString2() {
		ObjectHelperClass2 a = new ObjectHelperClass2();
		assertEquals("ObjectHelperClass2 {a=1, s=string}", a.toString());
	}

}
