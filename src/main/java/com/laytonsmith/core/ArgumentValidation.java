package com.laytonsmith.core;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions;
import com.laytonsmith.core.natives.interfaces.ArrayAccess;

/**
 * This class provides a way to validate, parse, and manipulate arguments passed
 * to functions in a standard, minimalist way, ultimately retrieving java
 * objects from the arguments. Many of these methods were originally from the
 * Static class, but have been moved into this class, which better groups them
 * together.
 */
public class ArgumentValidation {
	private ArgumentValidation() {
		//
	}

	/**
	 * Returns an item from an array, as a generic construct. This provides a
	 * standard way of returning an item from an array. If defaultItem is null,
	 * then it is required that the item be present in the object. If it is not,
	 * a {@link ConfigRuntimeException} is thrown.
	 *
	 * @param object The array to look in.
	 * @param key The key to search for
	 * @param t The code target
	 * @param defaultItem The default item to return if the specified key isn't
	 * present in the array. If this is a java null, and the key isn't present,
	 * a standard error message is thrown.
	 * @return The item in the array, or the defaultItem.
	 * @throws ConfigRuntimeException A FormatException is thrown if it doesn't
	 * contain the appropriate value and the defaultItem is null.
	 */
	public static Construct getItemFromArray(CArray object, String key, Target t, Construct defaultItem) throws ConfigRuntimeException {
		if (object.containsKey(key)) {
			return object.get(key, t);
		} else if (defaultItem == null) {
			throw new ConfigRuntimeException("Expected the key \"" + key + "\" to be present, but it was not found.", Exceptions.ExceptionType.FormatException, t);
		} else {
			return defaultItem;
		}
	}

	/**
	 * Returns a CArray object from a given construct, throwing a common error
	 * message if not.
	 *
	 * @param construct
	 * @param t
	 * @return
	 */
	public static CArray getArray(Construct construct, Target t) {
		if (construct instanceof CArray) {
			return ((CArray) construct);
		} else {
			throw new ConfigRuntimeException("Expecting array, but received " + construct.val(), Exceptions.ExceptionType.CastException, t);
		}
	}

	/**
	 * Works like the other get* methods, but works in a more generic way for
	 * other types of Constructs.
	 *
	 * @param <T> The type expected.
	 * @param construct The generic object
	 * @param t Code target
	 * @param expectedClassName The expected class type, for use in the error
	 * message if the construct is the wrong type.
	 * @param clazz The type expected.
	 * @return The properly cast object.
	 * @deprecated Use
	 * {@link #getObject(com.laytonsmith.core.constructs.Construct, com.laytonsmith.core.constructs.Target, java.lang.Class)}
	 * instead, as that gets the expected class name automatically.
	 */
	@Deprecated
	public static <T extends Construct> T getObject(Construct construct, Target t, String expectedClassName, Class<T> clazz) {
		if (clazz.isAssignableFrom(construct.getClass())) {
			return (T) construct;
		} else {
			throw new ConfigRuntimeException("Expecting " + expectedClassName + " but receieved " + construct.val() + " instead.", Exceptions.ExceptionType.CastException, t);
		}
	}

	/**
	 * Works like the other get* methods, but works in a more generic way for
	 * other types of Constructs. It also assumes that the class specified is
	 * tagged with a typeof annotation, thereby preventing the need for the
	 * expectedClassName like the deprecated version uses.
	 *
	 * @param <T> The type expected.
	 * @param construct The generic object
	 * @param t Code target
	 * @param clazz The type expected.
	 * @return The properly cast object.
	 */
	public static <T extends Construct> T getObject(Construct construct, Target t, Class<T> clazz) {
		if (clazz.isAssignableFrom(construct.getClass())) {
			return (T) construct;
		} else {
			String expectedClassName = clazz.getAnnotation(typeof.class).value();
			String actualClassName = construct.getClass().getAnnotation(typeof.class).value();
			throw new ConfigRuntimeException("Expecting " + expectedClassName + " but receieved " + construct.val()
					+ " (" + actualClassName + ") instead.", Exceptions.ExceptionType.CastException, t);
		}
	}

	/**
	 * This function pulls a numerical equivalent from any given construct. It
	 * throws a ConfigRuntimeException if it cannot be converted, for instance
	 * the string "s" cannot be cast to a number. The number returned will
	 * always be a double.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static double getNumber(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		double d;
		if (c == null || c instanceof CNull) {
			return 0.0;
		}
		if (c instanceof CInt) {
			d = ((CInt) c).getInt();
		} else if (c instanceof CDouble) {
			d = ((CDouble) c).getDouble();
		} else if (c instanceof CString) {
			try {
				d = Double.parseDouble(c.val());
			} catch (NumberFormatException e) {
				throw new ConfigRuntimeException("Expecting a number, but received \"" + c.val() + "\" instead",
						Exceptions.ExceptionType.CastException, t);
			}
		} else if (c instanceof CBoolean) {
			if (((CBoolean) c).getBoolean()) {
				d = 1;
			} else {
				d = 0;
			}
		} else {
			throw new ConfigRuntimeException("Expecting a number, but received \"" + c.val() + "\" instead",
					Exceptions.ExceptionType.CastException, t);
		}
		return d;
	}

	/**
	 * Alias to getNumber
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static double getDouble(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		try {
			return getNumber(c, t);
		} catch (ConfigRuntimeException e) {
			throw new ConfigRuntimeException("Expecting a double, but received " + c.val() + " instead",
					Exceptions.ExceptionType.CastException, t);
		}
	}

	/**
	 * Returns a 32 bit float from the construct. Since the backing value is
	 * actually a double, if the number contained in the construct is not the same
	 * after truncating, an exception is thrown (fail fast). When needing an float
	 * from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static float getDouble32(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		// Use 6 places at most else the imprecisions of float makes this function throw the exception.
		double delta = 0.0000001;
		double l = getDouble(c, t);
		float f = (float) l;
		if (Math.abs(f - l) > delta) {
			throw new Exceptions.RangeException("Expecting a 32 bit float, but a larger value was found: " + l, t);
		}
		return f;
	}

	/**
	 * Returns a long from any given construct.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static long getInt(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		long i;
		if (c == null || c instanceof CNull) {
			return 0;
		}
		if (c instanceof CInt) {
			i = ((CInt) c).getInt();
		} else if (c instanceof CBoolean) {
			if (((CBoolean) c).getBoolean()) {
				i = 1;
			} else {
				i = 0;
			}
		} else {
			try {
				i = Long.parseLong(c.val());
			} catch (NumberFormatException e) {
				throw new ConfigRuntimeException("Expecting an integer, but received \"" + c.val() + "\" instead",
						Exceptions.ExceptionType.CastException, t);
			}
		}
		return i;
	}

	/**
	 * Returns a 32 bit int from the construct. Since the backing value is
	 * actually a long, if the number contained in the construct is not the same
	 * after truncating, an exception is thrown (fail fast). When needing an int
	 * from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static int getInt32(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		long l = getInt(c, t);
		int i = (int) l;
		if (i != l) {
			throw new Exceptions.RangeException("Expecting a 32 bit integer, but a larger value was found: " + l, t);
		}
		return i;
	}

	/**
	 * Returns a 16 bit int from the construct (a short). Since the backing
	 * value is actually a long, if the number contained in the construct is not
	 * the same after truncating, an exception is thrown (fail fast). When
	 * needing an short from a construct, this method is much preferred over
	 * silently truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static short getInt16(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		long l = getInt(c, t);
		short s = (short) l;
		if (s != l) {
			throw new Exceptions.RangeException("Expecting a 16 bit integer, but a larger value was found: " + l, t);
		}
		return s;
	}

	/**
	 * Returns an 8 bit int from the construct (a byte). Since the backing value
	 * is actually a long, if the number contained in the construct is not the
	 * same after truncating, an exception is thrown (fail fast). When needing a
	 * byte from a construct, this method is much preferred over silently
	 * truncating.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static byte getInt8(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		long l = getInt(c, t);
		byte b = (byte) l;
		if (b != l) {
			throw new Exceptions.RangeException("Expecting an 8 bit integer, but a larger value was found: " + l, t);
		}
		return b;
	}

	/**
	 * Returns a boolean from any given construct. Depending on the type of the
	 * construct being converted, it follows the following rules: If it is an
	 * integer or a double, it is false if 0, true otherwise. If it is a string, array,
	 * or other ArrayAccess value, if it is empty, it is false, otherwise it is true.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static boolean getBoolean(Construct c, Target t) {
		if(c instanceof CMutablePrimitive){
			c = ((CMutablePrimitive)c).get();
		}
		boolean b = false;
		if (c == null) {
			return false;
		}
		if (c instanceof CBoolean) {
			b = ((CBoolean) c).getBoolean();
		} else if (c instanceof CString) {
			b = (c.val().length() > 0);
		} else if (c instanceof CInt || c instanceof CDouble) {
			b = !(getNumber(c, t) == 0);
		} else if (c instanceof ArrayAccess) {
			b = !(((ArrayAccess) c).size() == 0);
		}
		return b;
	}

	/**
	 * Returns a CByteArray object from the given construct.
	 * @param c
	 * @param t
	 * @return
	 */
	public static CByteArray getByteArray(Construct c, Target t) {
		if (c instanceof CByteArray) {
			return (CByteArray) c;
		} else if (c instanceof CNull) {
			return new CByteArray(t, 0);
		} else {
			throw new Exceptions.CastException("Expecting byte array, but found " + c.getCType() + " instead.", t);
		}
	}

	/**
	 * Returns a String object from the given construct. Note that no
	 * validation is done, because all Constructs can be toString'd, but
	 * this method is provided for consistency sake.
	 * @param c
	 * @param t
	 * @return
	 */
	public static String getString(Construct c, Target t){
		return c.val();
	}

	/**
	 * Returns true if any of the constructs are a CDouble, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyDoubles(Construct... c) {
		for (Construct c1 : c) {
			if (c1 instanceof CDouble) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if any of the constructs are CStrings, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyStrings(Construct... c) {
		for (Construct c1 : c) {
			if (c1 instanceof CString) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any of the constructs are CBooleans, false otherwise.
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyBooleans(Construct... c) {
		for (Construct c1 : c) {
			if (c1 instanceof CBoolean) {
				return true;
			}
		}
		return false;
	}
}
