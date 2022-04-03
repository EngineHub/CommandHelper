package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.Annotations.AggressiveDeprecation;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.MEnum;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.CRE.CRECastException;
import com.laytonsmith.core.exceptions.CRE.CREFormatException;
import com.laytonsmith.core.exceptions.CRE.CRERangeException;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CBoolean;
import com.laytonsmith.core.constructs.CByteArray;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CDecimal;
import com.laytonsmith.core.constructs.CDouble;
import com.laytonsmith.core.constructs.CInt;
import com.laytonsmith.core.constructs.CMutablePrimitive;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.constructs.CNumber;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.LeftHandSideType;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.natives.interfaces.Booleanish;
import com.laytonsmith.core.natives.interfaces.Mixed;
import com.laytonsmith.core.objects.UserObject;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.Objects;

import java.util.regex.Pattern;
import java.util.Set;

/**
 * This class provides a way to validate, parse, and manipulate arguments passed to functions in a standard, minimalist
 * way, ultimately retrieving java objects from the arguments. Many of these methods were originally from the Static
 * class, but have been moved into this class, which better groups them together.
 */
public final class ArgumentValidation {

	private ArgumentValidation() {
		//
	}

	@AggressiveDeprecation
	@Deprecated
	public static Mixed getItemFromArray(CArray object, String key, Target t, Mixed defaultItem) throws ConfigRuntimeException {
		return getItemFromArray(object, key, t, defaultItem, null);
	}

	/**
	 * Returns an item from an array, as a generic construct.This provides a standard way of returning an item from an
	 * array. If defaultItem is null, then it is required that the item be present in the object. If it is not, a
	 * {@link ConfigRuntimeException} is thrown.
	 *
	 * @param object The array to look in.
	 * @param key The key to search for
	 * @param t The code target
	 * @param defaultItem The default item to return if the specified key isn't present in the array. If this is a java
	 * null, and the key isn't present, a standard error message is thrown.
	 * @param env The environment
	 * @return The item in the array, or the defaultItem.
	 * @throws ConfigRuntimeException A FormatException is thrown if it doesn't contain the appropriate value and the
	 * defaultItem is null.
	 */
	public static Mixed getItemFromArray(CArray object, String key, Target t, Mixed defaultItem, Environment env) throws ConfigRuntimeException {
		if(object.containsKey(key)) {
			return object.get(key, t, env);
		} else if(defaultItem == null) {
			throw new CREFormatException("Expected the key \"" + key + "\" to be present, but it was not found.", t);
		} else {
			return defaultItem;
		}
	}

	@AggressiveDeprecation
	@Deprecated
	public static CArray getArray(Mixed construct, Target t) {
		return getArray(construct, t, null);
	}

	/**
	 * Returns a CArray object from a given construct, throwing a common error message if not.
	 *
	 * @param construct
	 * @param t
	 * @param env
	 * @return
	 */
	@SuppressWarnings("null")
	public static CArray getArray(Mixed construct, Target t, Environment env) {
		Objects.requireNonNull(construct);
		if(construct instanceof CArray || construct.isInstanceOf(CArray.TYPE, null, env)) {
			return ((CArray) construct);
		} else {
			throw new CRECastException("Expecting array, but received " + construct.val(), t);
		}
	}

	/**
	 * Works like the other get* methods, but works in a more generic way for other types of Mixeds. It also assumes
	 * that the class specified is tagged with a typeof annotation, thereby preventing the need for the
	 * expectedClassName like the deprecated version uses.
	 *
	 * This will work if the value is a subtype of the expected value.
	 *
	 * User classes are not supported here, because user classes cannot be managed directly in the java, it must be
	 * castable to an actual Java class to work, though it can work with classes that are defined in extensions.
	 *
	 * @param <T> The type expected.
	 * @param construct The generic object
	 * @param t Code target
	 * @param clazz The type expected.
	 * @return The properly cast object.
	 */
	public static <T extends Mixed> T getObject(Mixed construct, Target t, Class<T> clazz) {
		if(clazz.isAssignableFrom(construct.getClass())) {
			return (T) construct;
		} else {
			// TODO: For user objects, return a proxy
			String expectedClassName = ClassDiscovery.GetClassAnnotation(clazz, typeof.class).value();
			String actualClassName = ClassDiscovery.GetClassAnnotation(construct.getClass(), typeof.class).value();
			throw new CRECastException("Expecting " + expectedClassName + " but received " + construct.val()
					+ " (" + actualClassName + ") instead.", t);
		}
	}

	@AggressiveDeprecation
	@Deprecated
	public static double getNumber(Mixed c, Target t) {
		return getNumber(c, t, null);
	}

	/**
	 * This function pulls a numerical equivalent from any given construct. It throws a ConfigRuntimeException if it
	 * cannot be converted, for instance the string "s" cannot be cast to a number. The number returned will always be a
	 * double.
	 *
	 * @param c The (potential) number
	 * @param t The code target
	 * @param env The environment
	 * @return
	 */
	public static double getNumber(Mixed c, Target t, Environment env) {
		// TODO: Formalize this in the same way that Booleanish is formalized.
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		double d;
		if(c == null || c instanceof CNull) {
			return 0.0;
		}
		if(c instanceof CNumber || c.isInstanceOf(CNumber.TYPE, null, env)) {
			d = ((CNumber) c).getNumber();
		} else if(c instanceof CString || c.isInstanceOf(CString.TYPE, null, env)) {
			try {
				d = Double.parseDouble(c.val());
			} catch(NumberFormatException e) {
				throw new CRECastException("Expecting a number, but received \"" + c.val() + "\" instead", t);
			}
		} else if(c instanceof CBoolean || c.isInstanceOf(CBoolean.TYPE, null, env)) {
			if(((CBoolean) c).getBoolean()) {
				d = 1;
			} else {
				d = 0;
			}
		} else if(c instanceof CDecimal || c.isInstanceOf(CDecimal.TYPE, null, env)) {
			throw new CRECastException("Expecting a number, but received a decimal value instead. This cannot be"
					+ " automatically cast, please use double(@decimal) to manually cast down to a double.", t);
		} else {
			throw new CRECastException("Expecting a number, but received \"" + c.val() + "\" instead", t);
		}
		return d;
	}

	// Matches a string that will be successfully parsed by Double.parseDouble(String)
	// Based on https://docs.oracle.com/javase/8/docs/api/java/lang/Double.html#valueOf-java.lang.String-
	private static final Pattern VALID_DOUBLE = Pattern.compile(
			"[\\x00-\\x20]*"
			+ // leading whitespace
			"[+-]?("
			+ "("
			+ "((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)([eE][+-]?(\\p{Digit}+))?)"
			+ "|"
			+ "(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)"
			+ "|"
			+ // Hexadecimal strings
			"(("
			+ "(0[xX](\\p{XDigit}+)(\\.)?)"
			+ "|"
			+ "(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+))"
			+ ")[pP][+-]?(\\p{Digit}+))"
			+ ")[fFdD]?"
			+ ")[\\x00-\\x20]*" // trailing whitespace
	);

	@AggressiveDeprecation
	@Deprecated
	public static boolean isNumber(Mixed c) {
		return isNumber(c, null);
	}

	/**
	 * Validates that a construct's value is a number or string that can be returned by GetNumber()
	 *
	 * @param c Mixed
	 * @param env
	 * @return boolean
	 */
	@SuppressWarnings("null")
	public static boolean isNumber(Mixed c, Environment env) {
		Objects.requireNonNull(c);
		return c instanceof CNumber || VALID_DOUBLE.matcher(c.val()).matches();
	}

	@AggressiveDeprecation
	@Deprecated
	public static double getDouble(Mixed c, Target t) {
		return getDouble(c, t, null);
	}

	/**
	 * Alias to getNumber
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 */
	@SuppressWarnings("null")
	public static double getDouble(Mixed c, Target t, Environment env) {
		Objects.requireNonNull(c);
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		try {
			return getNumber(c, t, env);
		} catch(ConfigRuntimeException e) {
			throw new CRECastException("Expecting a double, but received " + c.val() + " instead", t);
		}
	}

	@AggressiveDeprecation
	@Deprecated
	public static float getDouble32(Mixed c, Target t) {
		return getDouble32(c, t, null);
	}

	/**
	 * Returns a 32 bit float from the construct. Since the backing value is actually a double, if the number contained
	 * in the construct is not the same after truncating, an exception is thrown (fail fast). When needing an float from
	 * a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 */
	public static float getDouble32(Mixed c, Target t, Environment env) {
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		// Use 6 places at most else the imprecisions of float makes this function throw the exception.
		double delta = 0.0000001;
		double l = getDouble(c, t, env);
		float f = (float) l;
		if(Math.abs(f - l) > delta) {
			throw new CRERangeException("Expecting a 32 bit float, but a larger value was found: " + l, t);
		}
		return f;
	}

	@AggressiveDeprecation
	@Deprecated
	public static long getInt(Mixed c, Target t) {
		return getInt(c, t, null);
	}

	/**
	 * Returns a long from any given construct.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @throws CRECastException If the value cannot be converted to a long
	 * @return
	 */
	public static long getInt(Mixed c, Target t, Environment env) {
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		long i;
		if(c == null || c instanceof CNull) {
			return 0;
		}
		if(c instanceof CInt || c.isInstanceOf(CInt.TYPE, null, env)) {
			i = getObject(c, t, CInt.class).getInt();
		} else if(c.isInstanceOf(CBoolean.TYPE, null, env)) {
			if(getObject(c, t, CBoolean.class).getBoolean()) {
				i = 1;
			} else {
				i = 0;
			}
		} else {
			try {
				i = Long.parseLong(c.val());
			} catch(NumberFormatException e) {
				throw new CRECastException("Expecting an integer, but received \"" + c.val() + "\" instead", t);
			}
		}
		return i;
	}

	@AggressiveDeprecation
	@Deprecated
	public static int getInt32(Mixed c, Target t) {
		return getInt32(c, t, null);
	}

	/**
	 * Returns a 32 bit int from the construct.Since the backing value is actually a long, if the number contained in
	 * the construct is not the same after truncating, an exception is thrown (fail fast). When needing an int from a
	 * construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static int getInt32(Mixed c, Target t, Environment env) {
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		long l = getInt(c, t, env);
		int i = (int) l;
		if(i != l) {
			throw new CRERangeException("Expecting a 32 bit integer, but a larger value was found: " + l, t);
		}
		return i;
	}

	@AggressiveDeprecation
	@Deprecated
	public static short getInt16(Mixed c, Target t) {
		return getInt16(c, t, null);
	}

	/**
	 * Returns a 16 bit int from the construct (a short).Since the backing value is actually a long, if the number
	 * contained in the construct is not the same after truncating, an exception is thrown (fail fast). When needing an
	 * short from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static short getInt16(Mixed c, Target t, Environment env) {
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		long l = getInt(c, t, env);
		short s = (short) l;
		if(s != l) {
			throw new CRERangeException("Expecting a 16 bit integer, but a larger value was found: " + l, t);
		}
		return s;
	}

	@AggressiveDeprecation
	@Deprecated
	public static byte getInt8(Mixed c, Target t) {
		return getInt8(c, t, null);
	}

	/**
	 * Returns an 8 bit int from the construct (a byte).Since the backing value is actually a long, if the number
	 * contained in the construct is not the same after truncating, an exception is thrown (fail fast). When needing a
	 * byte from a construct, this method is much preferred over silently truncating.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @throws CRERangeException If the value would be truncated
	 * @throws CRECastException If the value cannot be cast to an int
	 * @return
	 */
	public static byte getInt8(Mixed c, Target t, Environment env) {
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		long l = getInt(c, t, env);
		byte b = (byte) l;
		if(b != l) {
			throw new CRERangeException("Expecting an 8 bit integer, but a larger value was found: " + l, t);
		}
		return b;
	}

	@AggressiveDeprecation
	@Deprecated
	public static boolean getBooleanObject(Mixed c, Target t) {
		return getBooleanObject(c, t, null);
	}

	/**
	 * <s>Returns a the boolean value from the underlying CBoolean, or throws a CastException if the underlying type is
	 * not a CBoolean.</s>
	 * <p>
	 * Until auto cross casting is implemented, this has the same behavior as {@link #getBooleanish}, however, once
	 * strong typing is implemented, this will have the behavior described above. In the meantime, if you truly wish to
	 * validate the type, use {@link #getObject(Mixed, Target, Class)}
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 */
	public static boolean getBooleanObject(Mixed c, Target t, Environment env) {
		return getBooleanish(c, t, env);
	}

	@AggressiveDeprecation
	@Deprecated
	public static boolean getBoolean(Mixed c, Target t) {
		return getBoolean(c, t, null);
	}

	/**
	 * Currently forwards the call to {@link #getBooleanish}, to keep backwards compatible behavior, but will be removed
	 * in a future release.Explicitly use either {@link #getBooleanish} or {@link #getBooleanObject}.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 * @deprecated Use {@link #getBooleanish} for current behavior, or {@link #getBooleanObject} for strict behavior.
	 * Generally speaking, if it seems reasonable for the user to send a non-boolean data type in this parameter, then
	 * getBooleanish should be used. If it indicates a probable error, getBooleanObject should be used.
	 */
	@Deprecated
	public static boolean getBoolean(Mixed c, Target t, Environment env) {
		return getBooleanish(c, t, env);
	}

	@AggressiveDeprecation
	@Deprecated
	public static boolean getBooleanish(Mixed c, Target t) {
		return getBooleanish(c, t, null);
	}

	/**
	 * Returns a boolean from any given construct.Depending on the type of the construct being converted, it will return
	 * true or false. For actual booleans, the value is returned, but for Booleanish values, the value itself determines
	 * the rules for if it is determined to be trueish or falseish.
	 *
	 * @param c The value to convert
	 * @param t The code target
	 * @param env
	 * @return
	 */
	public static boolean getBooleanish(Mixed c, Target t, Environment env) {
		if(c instanceof CVoid) {
			// Eventually, we may want to turn this into a warning, maybe even a compiler error, but for now,
			// to keep backwards compatibility, we want to keep this as is.
			return false;
		}
		if(c instanceof CMutablePrimitive cMutablePrimitive) {
			c = cMutablePrimitive.get();
		}
		if(c == null) {
			return false;
		}
		if(c.isInstanceOf(Booleanish.TYPE, null, env)) {
			return ((Booleanish) c).getBooleanValue(env, t);
		}
		throw new CRECastException("Could not convert value of type " + c.typeof(env) + " to a " + Booleanish.TYPE, t);
	}

	@AggressiveDeprecation
	@Deprecated
	public static CByteArray getByteArray(Mixed c, Target t) {
		return getByteArray(c, t, null);
	}

	/**
	 * Returns a CByteArray object from the given construct.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 */
	@SuppressWarnings("null")
	public static CByteArray getByteArray(Mixed c, Target t, Environment env) {
		Objects.requireNonNull(c);
		if(c instanceof CByteArray cByteArray) {
			return cByteArray;
		} else if(c instanceof CNull) {
			return new CByteArray(t, 0, env);
		} else {
			throw new CRECastException("Expecting byte array, but found " + c.typeof(env) + " instead.", t);
		}
	}

	/**
	 * Returns the LeftHandSideType from the given Construct. This accepts either a LeftHandSideType directly, or a
	 * CClassType, which is converted to a LeftHandSideType.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 */
	@SuppressWarnings("null")
	public static LeftHandSideType getClassType(Mixed c, Target t, Environment env) {
		if(c instanceof CClassType cct) {
			return cct.asLeftHandSideType();
		} else if(c instanceof LeftHandSideType lhst) {
			return lhst;
		} else {
			throw new CRECastException("Expecting a ClassType, but found " + c.typeof(env) + " instead.", t);
		}
	}

	/**
	 * Returns a String object from the given construct. Note that no validation is done, because all Mixeds can be
	 * toString'd, but this method is provided for consistency sake.
	 *
	 * @param c
	 * @param t
	 * @return
	 */
	public static String getString(Mixed c, Target t) {
		return c.val();
	}

	@AggressiveDeprecation
	@Deprecated
	public static String getStringObject(Mixed c, Target t) {
		return getStringObject(c, t, null);
	}

	/**
	 * Returns a String object from the given construct. Note that unlike {@link #getString}, this strictly expects a
	 * string object (or subtype), and will throw a CRECastException if it is not a string.
	 *
	 * @param c
	 * @param t
	 * @param env
	 * @return
	 */
	public static String getStringObject(Mixed c, Target t, Environment env) {
		if(!c.isInstanceOf(CString.TYPE, null, env)) {
			throw new CRECastException("Expected a string, but found " + c.typeof(env) + " instead.", t);
		}
		return c.val();
	}

	@AggressiveDeprecation
	@Deprecated
	public static boolean anyDoubles(Mixed... c) {
		return anyDoubles(null, c);
	}

	/**
	 * Returns true if any of the constructs are a CDouble, false otherwise
	 *
	 * @param env
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyDoubles(Environment env, Mixed... c) {
		for(Mixed c1 : c) {
			if(c1 instanceof CDouble || c1 instanceof CString && c1.val().indexOf(".", 1) > -1) {
				return true;
			}
		}
		return false;
	}

	@AggressiveDeprecation
	@Deprecated
	public static boolean anyStrings(Mixed... c) {
		return anyStrings(null, c);
	}

	/**
	 * Return true if any of the constructs are CStrings, false otherwise.
	 *
	 * @param env
	 * @param c
	 * @return
	 */
	public static boolean anyStrings(Environment env, Mixed... c) {
		for(Mixed c1 : c) {
			if(c1 instanceof CString) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any of the constructs are null
	 *
	 * @param c
	 * @return
	 */
	public static boolean anyNulls(Mixed... c) {
		return anyNulls(null, c);
	}

	/**
	 * Returns true if any of the constructs are null
	 *
	 * @param env
	 * @param c
	 * @return
	 */
	public static boolean anyNulls(Environment env, Mixed... c) {
		for(Mixed c1 : c) {
			if(c1 instanceof CNull) {
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
	public static boolean anyBooleans(Mixed... c) {
		return anyBooleans(null, c);
	}

	/**
	 * Returns true if any of the constructs are CBooleans, false otherwise.
	 *
	 * @param env
	 * @param c
	 * @return
	 */
	public static boolean anyBooleans(Environment env, Mixed... c) {
		for(Mixed c1 : c) {
			if(c1 instanceof CBoolean) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the Enum value for the specified Enum value. While it doesn't technically have to be an MEnum, non MEnum
	 * values should generally not be exposed to users, as they are not visible to the rest of the ecosystem.
	 *
	 * @param <T> The Enum type
	 * @param c The construct passed in by the user
	 * @param enumClass The desired enum class
	 * @param t The code target
	 * @return The Java enum value
	 * @throws CRECastException If the user input is not a valid value, this is thrown, with a proper error message that
	 * describes the valid options. If the value is an MEnum, then the name defined there will be used, otherwise the
	 * class name will be used.
	 */
	public static <T extends Enum<T>> T getEnum(Mixed c, Class<T> enumClass, Target t) {
		// TODO: Can't proxy an enum, need to return an interface instead which can be proxied.
		String val = c.val();
		try {
			return Enum.valueOf(enumClass, val);
		} catch(IllegalArgumentException e) {
			String name = "java:" + enumClass.getName();
			MEnum menum = enumClass.getAnnotation(MEnum.class);
			if(menum != null) {
				name = menum.value();
			}
			throw new CRECastException("Cannot find enum of type " + name + " with value \"" + val + "\"."
					+ " Valid options are: " + StringUtils.Join(enumClass.getEnumConstants(), ", ", ", or "), t);
		}
	}

	/**
	 * Returns a set of the given enum value.The input value may be either a single value or an array. If it is a single
	 * value, the set will be of size 1. Null is also supported, and will return a set of size 0. Internally, uses
	 * {@link #getEnum}, so the behavior will generally be consistent with that.
	 *
	 * @param <T>
	 * @param c
	 * @param enumClass
	 * @param t
	 * @param env
	 * @return
	 */
	@SuppressWarnings("null")
	public static <T extends Enum<T>> Set<T> getEnumSet(Mixed c, Class<T> enumClass, Target t, Environment env) {
		Objects.requireNonNull(c);
		if(c instanceof CNull) {
			return EnumSet.noneOf(enumClass);
		}
		if(!c.isInstanceOf(CArray.TYPE, null, env)) {
			Mixed val = c;
			c = new CArray(t, null, env);
			((CArray) c).push(val, t, env);
		}
		CArray ca = (CArray) c;
		Set<T> set = EnumSet.noneOf(enumClass);
		for(Mixed v : ca.asList(env)) {
			set.add(getEnum(v, enumClass, t));
		}
		return set;
	}

	/**
	 * Proxies the given interface, to allow direct Java access to any type that implements the given interface,
	 * including user classes. Note that if the type natively implements the given interface, no proxying is done, it is
	 * just cast and returned.
	 * <p>
	 * For user classes, however, it is detected if they implement the given interface, and if so, the calls to the
	 * interface which are generally available in the api are correctly proxied. NOTE! Only methods which are part of
	 * the actual API can be used this way, otherwise, an Error will be thrown for user types, since they will not
	 * have been required to implement the given method.
	 *
	 * @param <T> The interface type to proxy
	 * @param iface The interface type to proxy
	 * @param value The value to proxy
	 * @param env The environment.
	 * @return The same value, but cast to the desired interface.
	 * @throws ClassCastException If the underlying type does not implement the given interface.
	 */
	public static <T extends Mixed> T getFromProxy(Class<T> iface, Mixed value, Environment env) {
		if(value.getClass().isAssignableFrom(iface)) {
			return (T) value;
		}
		if(value instanceof UserObject obj) {
			CClassType ifaceCT = CClassType.get(iface);
			if(!value.typeof(env).doesExtend(env, ifaceCT)) {
				throw new ClassCastException("Type does not extend the desired type");
			}

			return (T) Proxy.newProxyInstance(ArgumentValidation.class.getClassLoader(), new Class[]{iface},
					(Object proxy, Method method, Object[] args) -> {
						// TODO
						throw new UnsupportedOperationException("User object proxying not yet supported");
					});
		}
		throw new ClassCastException("Type does not extend the desired type");
	}

	/**
	 * Coerces the type according to cross casting rules. If the type is already correct, it is simply returned.
	 * For now, only a limited set of types is supported, but this will be expanded later to support proper cross
	 * casting.
	 * @param value The value to potentially coerce.
	 * @param expected The expected type.
	 * @param env The environment.
	 * @param t The code target.
	 * @return The coerced type, or the original value if it's already the proper type.
	 */
	public static Mixed typeCoerce(Mixed value, CClassType expected, Environment env, Target t) {
		if(CNull.NULL.equals(value)) {
			return value;
		}
		CClassType actual = value.typeof(env);
		if(actual.equals(expected)) {
			return value;
		}
		if(expected.equals(CInt.TYPE)) {
			return new CInt(getInt(value, t, env), t);
		} else if(expected.equals(CDouble.TYPE)) {
			return new CDouble(getDouble(value, t, env), t);
		} else if(expected.equals(CString.TYPE)) {
			return new CString(getString(value, t), t);
		} else if(expected.equals(CBoolean.TYPE)) {
			return CBoolean.get(getBooleanish(value, t, env));
		} else {
			throw new CRECastException("Cannot cast from " + actual.getName() + " to " + expected.getName(), t);
		}
	}

}
