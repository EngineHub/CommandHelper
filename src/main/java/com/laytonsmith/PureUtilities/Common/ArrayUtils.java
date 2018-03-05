package com.laytonsmith.PureUtilities.Common;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * Provides various utility methods for working with arrays.
 */
@SuppressWarnings({"UnnecessaryUnboxing", "unchecked"})
public class ArrayUtils {

	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final char[] EMPTY_CHAR_ARRAY = new char[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final short[] EMPTY_SHORT_ARRAY = new short[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final long[] EMPTY_LONG_ARRAY = new long[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];

	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Character[] EMPTY_CHAR_OBJ_ARRAY = new Character[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Byte[] EMPTY_BYTE_OBJ_ARRAY = new Byte[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Short[] EMPTY_SHORT_OBJ_ARRAY = new Short[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Integer[] EMPTY_INT_OBJ_ARRAY = new Integer[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Long[] EMPTY_LONG_OBJ_ARRAY = new Long[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Float[] EMPTY_FLOAT_OBJ_ARRAY = new Float[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Double[] EMPTY_DOUBLE_OBJ_ARRAY = new Double[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final Boolean[] EMPTY_BOOLEAN_OBJ_ARRAY = new Boolean[0];
	/**
	 * Instantiating a new 0 length array is *usually* inefficient, unless you are doing reference comparisons later. If
	 * you are generating it simply to use as a "default" value for an array, consider using this instead to increase
	 * performance.
	 */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * *************************************************************************
	 * Slices *************************************************************************
	 */
	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param <T> The array type
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] slice(T[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		Object newArray = Array.newInstance(array.getClass().getComponentType(), size);
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				Array.set(newArray, counter++, array[i]);
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				Array.set(newArray, counter++, array[i]);
			}
		}
		return (T[]) newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static char[] slice(char[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		char[] newArray = new char[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static byte[] slice(byte[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		byte[] newArray = new byte[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static short[] slice(short[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		short[] newArray = new short[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static int[] slice(int[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		int[] newArray = new int[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static long[] slice(long[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		long[] newArray = new long[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static float[] slice(float[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		float[] newArray = new float[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static double[] slice(double[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		double[] newArray = new double[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * Slices an array, where the array [0, 1, 2] sliced from 0 to 2 would return the whole array. That is, start and
	 * finish are inclusive. Finish may be less than start, in which case the slice will also be backwards, so slicing
	 * from 1 to 0 would return [1, 0]. If start and finish are equal, an array with one result is returned.
	 *
	 * @param array The array to be sliced. Note that the original array remains unchanged.
	 * @param start The starting node.
	 * @param finish The ending node (inclusive).
	 * @return
	 */
	public static boolean[] slice(boolean[] array, int start, int finish) {
		int size = Math.abs(start - finish) + 1;
		boolean[] newArray = new boolean[size];
		if(start <= finish) {
			int counter = 0;
			for(int i = start; i <= finish; i++) {
				newArray[counter++] = array[i];
			}
		} else {
			int counter = 0;
			for(int i = start; i >= finish; i--) {
				newArray[counter++] = array[i];
			}
		}
		return newArray;
	}

	/**
	 * *************************************************************************
	 * Unboxes *************************************************************************
	 */
	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static char[] unbox(Character[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_CHAR_ARRAY;
		}
		final char[] newArray = new char[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].charValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static byte[] unbox(Byte[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_BYTE_ARRAY;
		}
		final byte[] newArray = new byte[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].byteValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static short[] unbox(Short[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_SHORT_ARRAY;
		}
		final short[] newArray = new short[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].shortValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static int[] unbox(Integer[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_INT_ARRAY;
		}
		final int[] newArray = new int[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].intValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static long[] unbox(Long[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_LONG_ARRAY;
		}
		final long[] newArray = new long[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].longValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static float[] unbox(Float[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_FLOAT_ARRAY;
		}
		final float[] newArray = new float[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].floatValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static double[] unbox(Double[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_DOUBLE_ARRAY;
		}
		final double[] newArray = new double[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].doubleValue();
		}
		return newArray;
	}

	/**
	 * "Unboxes" an array, that is, unboxes all the primitives in this array, and returns a primitive array.
	 *
	 * @param array The "boxed" array
	 * @return The "unboxed" array
	 */
	@SuppressWarnings("UnnecessaryUnboxing")
	public static boolean[] unbox(Boolean[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_BOOLEAN_ARRAY;
		}
		final boolean[] newArray = new boolean[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i].booleanValue();
		}
		return newArray;
	}

	/**
	 * *************************************************************************
	 * Boxes *************************************************************************
	 */
	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Character[] box(char[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_CHAR_OBJ_ARRAY;
		}
		final Character[] newArray = new Character[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Byte[] box(byte[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_BYTE_OBJ_ARRAY;
		}
		final Byte[] newArray = new Byte[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Short[] box(short[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_SHORT_OBJ_ARRAY;
		}
		final Short[] newArray = new Short[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Integer[] box(int[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_INT_OBJ_ARRAY;
		}
		final Integer[] newArray = new Integer[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Long[] box(long[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_LONG_OBJ_ARRAY;
		}
		final Long[] newArray = new Long[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Float[] box(float[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_FLOAT_OBJ_ARRAY;
		}
		final Float[] newArray = new Float[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Double[] box(double[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_DOUBLE_OBJ_ARRAY;
		}
		final Double[] newArray = new Double[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * "Boxes" an array, that is, boxes all the primitives in the given array, and returns a new "boxed" array.
	 *
	 * @param array The primitive array
	 * @return The "boxed" array
	 */
	public static Boolean[] box(boolean[] array) {
		if(array == null) {
			return null;
		} else if(array.length == 0) {
			return EMPTY_BOOLEAN_OBJ_ARRAY;
		}
		final Boolean[] newArray = new Boolean[array.length];
		for(int i = 0; i < array.length; i++) {
			newArray[i] = array[i];
		}
		return newArray;
	}

	/**
	 * *************************************************************************
	 * Misc *************************************************************************
	 */
	/**
	 * Returns a new array, based on the runtime type of the list.
	 *
	 * @param <T>
	 * @param list
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] asArray(Class<T> clazz, List<T> list) {
		T[] obj = (T[]) Array.newInstance(clazz, list.size());
		for(int i = 0; i < list.size(); i++) {
			obj[i] = list.get(i);
		}
		return obj;
	}

	/**
	 * Returns a new array, where each item has been cast to the specified class, and the returned array is an array
	 * type based on that class.
	 *
	 * @param <T>
	 * @param array Despite being an Object, instead of an Object[], this will throw a ClassCastException if it is not
	 * an array type.
	 * @param toClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object array, Class<T> toArrayClass) {
		if(!array.getClass().isArray()) {
			throw new ClassCastException();
		}
		Object obj;
		Class<?> toClass = toArrayClass.getComponentType();

		obj = toArrayClass.cast(Array.newInstance(toClass, Array.getLength(array)));
		for(int i = 0; i < Array.getLength(array); i++) {
			doSet(obj, i, Array.get(array, i));
		}
		return (T) obj;
	}

	@SuppressWarnings("UnnecessaryUnboxing")
	private static void doSet(Object array, int index, Object o) {
		Class<?> componentType = array.getClass().getComponentType();
		if(componentType.isPrimitive()) {
			if(componentType == char.class) {
				Array.setChar(array, index, ((Character) o).charValue());
			} else if(componentType == byte.class) {
				Array.setByte(array, index, ((Number) o).byteValue());
			} else if(componentType == short.class) {
				Array.setShort(array, index, ((Number) o).shortValue());
			} else if(componentType == int.class) {
				Array.setInt(array, index, ((Number) o).intValue());
			} else if(componentType == long.class) {
				Array.setLong(array, index, ((Number) o).longValue());
			} else if(componentType == float.class) {
				Array.setFloat(array, index, ((Number) o).floatValue());
			} else if(componentType == double.class) {
				Array.setDouble(array, index, ((Number) o).doubleValue());
			} else if(componentType == boolean.class) {
				Array.setBoolean(array, index, ((Boolean) o).booleanValue());
			}
		} else {
			Array.set(array, index, o);
		}
	}

	/**
	 * Converts a char array to a byte array, assuming UTF-8 encoding.
	 *
	 * {@link #charToBytes(char[], java.lang.String)} for the documentation on this method other than the encoding used.
	 *
	 * @param chars The char array to convert.
	 * @return
	 */
	public static byte[] charToBytes(char[] chars) {
		return charToBytes(chars, "UTF-8");
	}

	/**
	 * Converts a char array to a byte array, assuming the given encoding. This is done in a secure manner, and
	 * potentially sensitive data is cleared from memory after the encoding is done.
	 *
	 * @param chars The char array to convert.
	 * @param encoding The encoding to use.
	 * @return
	 */
	@SuppressWarnings("UnusedAssignment")
	public static byte[] charToBytes(char[] chars, String encoding) {
		CharBuffer charBuffer = CharBuffer.wrap(chars);
		ByteBuffer byteBuffer = Charset.forName(encoding).encode(charBuffer);
		byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
				byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		charBuffer = null; // faster GC
		byteBuffer = null; // faster GC
		System.gc();
		return bytes;
	}

	/**
	 * Converts a byte array to a char array, assuming UTF-8 encoding.
	 *
	 * {@link #bytesToChar(byte[], java.lang.String)} for the documentation on this method other than the encoding used.
	 *
	 * @param bytes The byte array to convert.
	 * @return
	 */
	public static char[] bytesToChar(byte[] bytes) {
		return bytesToChar(bytes, "UTF-8");
	}

	/**
	 * Converts a byte aray to a char array, assuming the given encoding. This is done in a secure manner, and
	 * potentially sensitive data is cleared from memory after the encoding isdone.
	 *
	 * @param bytes The byte array to convert
	 * @param encoding The encoding to use
	 * @return
	 */
	@SuppressWarnings("UnusedAssignment")
	public static char[] bytesToChar(byte[] bytes, String encoding) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = Charset.forName(encoding).decode(byteBuffer);
		char[] chars = Arrays.copyOfRange(charBuffer.array(),
				charBuffer.position(), charBuffer.limit());
		Arrays.fill(byteBuffer.array(), (byte) 0);
		Arrays.fill(charBuffer.array(), '\u0000');
		charBuffer = null;
		byteBuffer = null;
		System.gc();
		return chars;
	}
}
