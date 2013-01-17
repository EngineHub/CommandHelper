package com.laytonsmith.PureUtilities;

/**
 * This class simply provides a method to getting the size of java primitives,
 * without having magic numbers everywhere.
 *
 * @author lsmith
 */
public final class Sizes {

	public static final int booleanSizeBits = 1;
	public static final int byteSize = 1;
	public static final int byteSizeBits = byteSize * 8;
	public static final int shortSize = 2;
	public static final int shortSizeBits = shortSize * byteSizeBits;
	public static final int intSize = 4;
	public static final int intSizeBits = intSize * byteSizeBits;
	public static final int longSize = 8;
	public static final int longSizeBits = longSize * byteSizeBits;
	public static final int floatSize = 4;
	public static final int floatSizeBits = floatSize * byteSizeBits;
	public static final int doubleSize = 8;
	public static final int doubleSizeBits = doubleSize * byteSizeBits;
	public static final int charSize = 2;
	public static final int charSizeBits = charSize * byteSizeBits;

	// bytes
	public static int sizeof(byte b) {
		return byteSize;
	}

	public static int sizeof(short s) {
		return shortSize;
	}

	public static int sizeof(int i) {
		return intSize;
	}

	public static int sizeof(long l) {
		return longSize;
	}

	public static int sizeof(float f) {
		return floatSize;
	}

	public static int sizeof(double d) {
		return doubleSize;
	}

	public static int sizeof(char c) {
		return charSize;
	}

	// NOTE: no sizeof for boolean, only sizeofBits
	// bits
	public static int sizeofBits(byte b) {
		return byteSizeBits;
	}

	public static int sizeofBits(short s) {
		return shortSizeBits;
	}

	public static int sizeofBits(int i) {
		return intSizeBits;
	}

	public static int sizeofBits(long l) {
		return longSizeBits;
	}

	public static int sizeofBits(float f) {
		return floatSizeBits;
	}

	public static int sizeofBits(double d) {
		return doubleSizeBits;
	}

	public static int sizeofBits(char c) {
		return charSizeBits;
	}

	public static int sizeofBits(boolean b) {
		return booleanSizeBits;
	}

	// array bytes
	public static long sizeof(byte[] b) {
		return byteSize * b.length;
	}

	public static long sizeof(short[] s) {
		return shortSize * s.length;
	}

	public static long sizeof(int[] i) {
		return intSize * i.length;
	}

	public static long sizeof(long[] l) {
		return longSize * l.length;
	}

	public static long sizeof(float[] f) {
		return floatSize * f.length;
	}

	public static long sizeof(double[] d) {
		return doubleSize * d.length;
	}

	public static long sizeof(char[] c) {
		return charSize * c.length;
	}

	// array bits
	public static long sizeofBits(byte[] b) {
		return byteSizeBits * b.length;
	}

	public static long sizeofBits(short[] s) {
		return shortSizeBits * s.length;
	}

	public static long sizeofBits(int[] i) {
		return intSizeBits * i.length;
	}

	public static long sizeofBits(long[] l) {
		return longSizeBits * l.length;
	}

	public static long sizeofBits(float[] f) {
		return floatSizeBits * f.length;
	}

	public static long sizeofBits(double[] d) {
		return doubleSizeBits * d.length;
	}

	public static long sizeofBits(char[] c) {
		return charSizeBits * c.length;
	}

	public static long sizeofBits(boolean[] b) {
		return booleanSizeBits * b.length;
	}

	//Class types
	public static int sizeof(Class<?> c) {
		if (c.isPrimitive()) {
			if (c == byte.class) {
				return byteSize;
			} else if (c == short.class) {
				return shortSize;
			} else if (c == int.class) {
				return intSize;
			} else if (c == long.class) {
				return longSize;
			} else if (c == float.class) {
				return floatSize;
			} else if (c == double.class) {
				return doubleSize;
			} else if (c == char.class) {
				return charSize;
			}
		}
		throw new RuntimeException("Only non-boolean primitives are supported");
	}

	public static int sizeofBits(Class<?> c) {
		if (c.isPrimitive()) {
			if (c == byte.class) {
				return byteSizeBits;
			} else if (c == short.class) {
				return shortSizeBits;
			} else if (c == int.class) {
				return intSizeBits;
			} else if (c == long.class) {
				return longSizeBits;
			} else if (c == float.class) {
				return floatSizeBits;
			} else if (c == double.class) {
				return doubleSizeBits;
			} else if (c == char.class) {
				return charSizeBits;
			} else if (c == boolean.class) {
				return booleanSizeBits;
			}
		}
		throw new RuntimeException("Only primitives are supported");
	}
}
