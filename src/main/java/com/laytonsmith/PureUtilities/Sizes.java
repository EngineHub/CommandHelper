package com.laytonsmith.PureUtilities;

/**
 * This class simply provides a method to getting the size of java primitives, without having magic numbers everywhere.
 *
 *
 */
public final class Sizes {

	public static final int BOOLEAN_SIZE_BITS = 1;
	public static final int BYTE_SIZE = 1;
	public static final int BYTE_SIZE_BITS = BYTE_SIZE * 8;
	public static final int SHORT_SIZE = 2;
	public static final int SHORT_SIZE_BITS = SHORT_SIZE * BYTE_SIZE_BITS;
	public static final int INT_SIZE = 4;
	public static final int INT_SIZE_BITS = INT_SIZE * BYTE_SIZE_BITS;
	public static final int LONG_SIZE = 8;
	public static final int LONG_SIZE_BITS = LONG_SIZE * BYTE_SIZE_BITS;
	public static final int FLOAT_SIZE = 4;
	public static final int FLOAT_SIZE_BITS = FLOAT_SIZE * BYTE_SIZE_BITS;
	public static final int DOUBLE_SIZE = 8;
	public static final int DOUBLE_SIZE_BITS = DOUBLE_SIZE * BYTE_SIZE_BITS;
	public static final int CHAR_SIZE = 2;
	public static final int CHAR_SIZE_BITS = CHAR_SIZE * BYTE_SIZE_BITS;

	// bytes
	public static int sizeof(byte b) {
		return BYTE_SIZE;
	}

	public static int sizeof(short s) {
		return SHORT_SIZE;
	}

	public static int sizeof(int i) {
		return INT_SIZE;
	}

	public static int sizeof(long l) {
		return LONG_SIZE;
	}

	public static int sizeof(float f) {
		return FLOAT_SIZE;
	}

	public static int sizeof(double d) {
		return DOUBLE_SIZE;
	}

	public static int sizeof(char c) {
		return CHAR_SIZE;
	}

	// array bytes
	public static long sizeof(byte[] b) {
		return BYTE_SIZE * b.length;
	}

	public static long sizeof(short[] s) {
		return SHORT_SIZE * s.length;
	}

	public static long sizeof(int[] i) {
		return INT_SIZE * i.length;
	}

	public static long sizeof(long[] l) {
		return LONG_SIZE * l.length;
	}

	public static long sizeof(float[] f) {
		return FLOAT_SIZE * f.length;
	}

	public static long sizeof(double[] d) {
		return DOUBLE_SIZE * d.length;
	}

	public static long sizeof(char[] c) {
		return CHAR_SIZE * c.length;
	}

	// Class types
	public static int sizeof(Class<?> c) {
		if(c.isPrimitive()) {
			if(c == byte.class) {
				return BYTE_SIZE;
			} else if(c == short.class) {
				return SHORT_SIZE;
			} else if(c == int.class) {
				return INT_SIZE;
			} else if(c == long.class) {
				return LONG_SIZE;
			} else if(c == float.class) {
				return FLOAT_SIZE;
			} else if(c == double.class) {
				return DOUBLE_SIZE;
			} else if(c == char.class) {
				return CHAR_SIZE;
			}
		}
		throw new RuntimeException("Only non-boolean primitives are supported");
	}

	// NOTE: no sizeof for boolean, only sizeofBits
	// bits
	public static int sizeofBits(byte b) {
		return BYTE_SIZE_BITS;
	}

	public static int sizeofBits(short s) {
		return SHORT_SIZE_BITS;
	}

	public static int sizeofBits(int i) {
		return INT_SIZE_BITS;
	}

	public static int sizeofBits(long l) {
		return LONG_SIZE_BITS;
	}

	public static int sizeofBits(float f) {
		return FLOAT_SIZE_BITS;
	}

	public static int sizeofBits(double d) {
		return DOUBLE_SIZE_BITS;
	}

	public static int sizeofBits(char c) {
		return CHAR_SIZE_BITS;
	}

	public static int sizeofBits(boolean b) {
		return BOOLEAN_SIZE_BITS;
	}

	// array bits
	public static long sizeofBits(byte[] b) {
		return BYTE_SIZE_BITS * b.length;
	}

	public static long sizeofBits(short[] s) {
		return SHORT_SIZE_BITS * s.length;
	}

	public static long sizeofBits(int[] i) {
		return INT_SIZE_BITS * i.length;
	}

	public static long sizeofBits(long[] l) {
		return LONG_SIZE_BITS * l.length;
	}

	public static long sizeofBits(float[] f) {
		return FLOAT_SIZE_BITS * f.length;
	}

	public static long sizeofBits(double[] d) {
		return DOUBLE_SIZE_BITS * d.length;
	}

	public static long sizeofBits(char[] c) {
		return CHAR_SIZE_BITS * c.length;
	}

	public static long sizeofBits(boolean[] b) {
		return BOOLEAN_SIZE_BITS * b.length;
	}

	// Class types
	public static int sizeofBits(Class<?> c) {
		if(c.isPrimitive()) {
			if(c == byte.class) {
				return BYTE_SIZE_BITS;
			} else if(c == short.class) {
				return SHORT_SIZE_BITS;
			} else if(c == int.class) {
				return INT_SIZE_BITS;
			} else if(c == long.class) {
				return LONG_SIZE_BITS;
			} else if(c == float.class) {
				return FLOAT_SIZE_BITS;
			} else if(c == double.class) {
				return DOUBLE_SIZE_BITS;
			} else if(c == char.class) {
				return CHAR_SIZE_BITS;
			} else if(c == boolean.class) {
				return BOOLEAN_SIZE_BITS;
			}
		}
		throw new RuntimeException("Only primitives are supported");
	}
}
