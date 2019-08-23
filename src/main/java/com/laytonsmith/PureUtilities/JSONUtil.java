package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.ClassUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Wraps the JSONObject class, and parses the input into the given Bean-like class. The bean class has some
 * simple rules it must follow. It must have a no-arg constructor, though it may just be the default constructor.
 * The fields in the class do not need to be public, but they can only be composed of booleans, ints (as well as shorts
 * and bytes), doubles (as well as floats),
 * and strings, as well as arrays of those values (including multi dimensional arrays), and other objects that are only
 * composed of objects that follow
 * these same rules, and enums. All fields intended to be deserialized must be public, though they are allowed to be
 * final as well.
 * <p>
 * Both primitive values and Object versions may be used (boolean/Boolean, int/Integer, double/Double, etc).
 * <p>
 * Classes may extend other classes that follow the rules, and the object inheritance will be respected. Generic
 * inheritance is supported as well. A super class may define a field with a generic type, such as:
 * <code><pre>
 * class A&lt;T> {
 *   int id;
 *   T obj;
 * }
 * </pre></code>
 * And then a subclass can extend that, providing a concrete type for T as well. Unfortunately, the object itself
 * also needs to be overridden with the concrete type of T, due to the type erasure effect in Java. However, other
 * fields in the superclass do not need to be overridden, and anyways elsewhere in the Java code, you will retain
 * type safety.
 * <code><pre>
 * class B extends A&lt;String> {
 *   String obj;
 * }
 * // Now elsewhere in the code
 * B b = deserialize(json, B.class);
 * System.out.println(b.id); // id is inherited properly from A
 * System.out.println(b.obj instanceof String); // true, because we've overridden the type of Object
 * </pre></code>
 * <p>
 * Enums will serialize and deserialize as integers, based on their ordinal value, though enums may implement
 * the {@link CustomEnum} interface, and they are then allowed to map to any long value they wish.
 * @author Cailin
 */
public class JSONUtil {

	public static class JSONException extends Exception {
		public JSONException(String message) {
			super(message);
		}
	}

	/**
	 * Normally enums are serialized and deserialized based on their ordinal. However, this is not always desirable,
	 * if the enum represents something such as an error code, rather than a true enum. To facilitate these custom
	 * enum ordinals, the enum may implement this interface, which manages going back and forth through the custom
	 * values to represent ordinals.
	 * @param <T> The enum class
	 */
	static interface CustomEnum<T extends Enum, M> {
		/**
		 * Returns the enum given the associated value. It is up to the implementation to decide what to do if the
		 * value can't be found, but it may choose to return a default value, return null, or throw an exception.
		 * @param value
		 * @return
		 */
		T getFromValue(M value);

		/**
		 * Gets the value that this enum represents.
		 * @return
		 */
		M getValue();
	}

	/**
	 * Normally enums are serialized and deserialized based on their ordinal. However, this is not always desirable,
	 * if the enum represents something such as an error code, rather than a true enum. To facilitate these custom
	 * enum ordinals, the enum may implement this interface, which manages going back and forth through the custom
	 * ordinals.
	 * @param <T> The enum class
	 */
	public static interface CustomLongEnum<T extends Enum> extends CustomEnum<T, Long> {

	}

	/**
	 * Normally enums are serialized and deserialized based on their ordinal. However, this is not always desirable,
	 * if the enum represents something such as an error code, rather than a true enum. To facilitate these custom
	 * enum ordinals, the enum may implement this interface, which manages going back and forth through the custom
	 * ordinals.
	 * @param <T> The enum class
	 */
	public static interface CustomStringEnum<T extends Enum> extends CustomEnum<T, String> {

	}



	public JSONUtil() {

	}

	/**
	 * Parses the input into the given Bean-like class. The bean class has some
	 * simple rules it must follow. It must have a no-arg constructor, though it may just be the default constructor.
	 * The fields in the class do not need to be public, but they can only be composed of booleans, ints, doubles,
	 * and strings, as well as arrays of those values (including multi dimensional arrays), and other objects that
	 * follow
	 * these same rules.
	 * <p>
	 * Both primitive values and Object versions may be used (boolean/Boolean, int/Integer, double/Double).
	 * @param <T> An instance of the specified bean class
	 * @param json The json string
	 * @param bean The bean class to deserialize into
	 * @return An instance of the specified bean
	 * @throws com.laytonsmith.PureUtilities.JSONUtil.JSONException If there was an exception parsing the value.
	 * This can happen if a value is of the wrong type, or there were other parsing errors in the json itself.
	 * Extra values in the object are not an error.
	 */
	public <T> T deserialize(String json, Class<T> bean) throws JSONException {
		JSONObject obj;
		try {
			obj = (JSONObject) JSONValue.parse(json);
		} catch (ClassCastException ex) {
			throw new JSONException("Value is not an object!");
		}

		T t = getType(obj, bean);
		return t;
	}

	/**
	 * Parses the input into the given supported bean class. The input class type may only be one of Integer, Double,
	 * Boolean, String, or arrays of these types (including multi dimensional arrays) and objects that follow the rules
	 * described in {@link #deserialize(java.lang.String, java.lang.Class)}.
	 *
	 * @param <T> An instance of the array type
	 * @param json The json string
	 * @param bean The array class to deserialize into. Note that this is not the array type. If you have an array of
	 * strings, the appropriate value to pass in here is {@code string.class}, not {@code string[].class}.
	 * @return An instance of the specified bean
	 * @throws com.laytonsmith.PureUtilities.JSONUtil.JSONException If there was an exception parsing the value.
	 * This can happen if a value is of the wrong type, or there were other parsing errors in the json itself.
	 */
	public <T> T[] deserializeArray(String json, Class<T> bean) throws JSONException {
		JSONArray obj;
		try {
			obj = (JSONArray) JSONValue.parse(json);
		} catch (ClassCastException ex) {
			throw new JSONException("Value is not an array!");
		}
		Class<?> arrayClass = ClassUtils.getArrayClassFromType(bean);
		return (T[]) getType(obj, arrayClass);
	}

	@SuppressWarnings("UnnecessaryBoxing") // Actually is necessary
	private <T> T getType(Object o, Class<T> c) {
		if(o == null) {
			if(c == int.class) {
				return (T) new Integer(0);
			} else if(c == double.class) {
				return (T) new Double(0.0);
			} else if(c == boolean.class) {
				return (T) Boolean.FALSE;
			}
			return null;
		}
		// TODO Support enums
		if(c.isArray()) {
			JSONArray a = (JSONArray) o;
			Object array = Array.newInstance(c.getComponentType(), a.size());
			for(int i = 0; i < a.size(); i++) {
				Object subValue = getType(a.get(i), c.getComponentType());
				Array.set(array, i, subValue);
			}
			return (T) array;
		} else if(double.class.isAssignableFrom(c) || Double.class.isAssignableFrom(c)) {
			return (T) (Double) Double.parseDouble(o.toString());
		} else if(float.class.isAssignableFrom(c) || Float.class.isAssignableFrom(c)) {
			return (T) (Float) Float.parseFloat(o.toString());
		} else if(byte.class.isAssignableFrom(c) || Byte.class.isAssignableFrom(c)) {
			return (T) (Byte) Byte.parseByte(o.toString());
		} else if(short.class.isAssignableFrom(c) || Short.class.isAssignableFrom(c)) {
			return (T) (Short) Short.parseShort(o.toString());
		} else if(int.class.isAssignableFrom(c) || Integer.class.isAssignableFrom(c)) {
			return (T) (Integer) Integer.parseInt(o.toString());
		} else if(long.class.isAssignableFrom(c) || Long.class.isAssignableFrom(c)) {
			return (T) (Long) Long.parseLong(o.toString());
		} else if(String.class.isAssignableFrom(c)) {
			return (T) o.toString();
		} else if(boolean.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c)) {
			return (T) Boolean.valueOf(o.toString());
		} else if(Enum.class.isAssignableFrom(c)) {
			// Enum values. We need to see if c implements CustomEnum. If so, use that to deserialize. If not,
			// just use the ordinal.
			if(CustomEnum.class.isAssignableFrom(c)) {
				return (T) ((CustomEnum) c.getEnumConstants()[0]).getFromValue((T) o);
			} else {
				Object e = c.getEnumConstants()[Integer.parseInt(o.toString())];
				return (T) e;
			}
		} else {
			// Another bean, we need to loop through it and recurse
			JSONObject obj = (JSONObject) o;
			T t = ReflectionUtils.newInstance(c);
			Class clz = c;
			Set<String> setFields = new HashSet<>();
			do {
				// Walk up the object inheritance chain, but don't set already set fields, because this is
				// how we override generic types within the superclasses, by giving priority to the type
				// of the lower class.
				for(Field f : clz.getDeclaredFields()) {
					if(setFields.contains(f.getName())) {
						continue;
					}
					ReflectionUtils.set(clz, t, f.getName(), getType(obj.get(f.getName()), f.getType()));
					setFields.add(f.getName());
				}
				clz = clz.getSuperclass();
			} while(clz != Object.class);
			return t;
		}
	}

	public String serialize(Object obj) {
		Object r;
		if(obj.getClass().isArray()) {
			r = fromArrayType(obj);
		} else {
			r = fromType(obj);
		}
		return JSONValue.toJSONString(r);
	}

	private <T> JSONArray fromArrayType(Object array) {
		JSONArray r = new JSONArray();
		for(int i = 0; i < Array.getLength(array); i++) {
			r.add(fromType(Array.get(array, i)));
		}
		return r;
	}

	private Object fromType(Object obj) {
		if(obj instanceof Double || obj instanceof Float
				|| obj instanceof Integer || obj instanceof Long
				|| obj instanceof Short || obj instanceof Byte
				|| obj instanceof String || obj instanceof Boolean) {
			// If it's already a primitive, just return that. Don't need to check for actual primitives, because
			// java boxes them for us in instanceof operations.
			return obj;
		}
		if(Enum.class.isAssignableFrom(obj.getClass())) {
			if(CustomEnum.class.isAssignableFrom(obj.getClass())) {
				return ((CustomEnum) obj).getValue();
			} else {
				return ((Enum) obj).ordinal();
			}
		}
		JSONObject r = new JSONObject();
		Class clz = obj.getClass();
		do {
			for(Field f : obj.getClass().getDeclaredFields()) {
				String name = f.getName();
				Object o = ReflectionUtils.get(clz, obj, name);
				Class c = o.getClass();
				if(c.isArray()) {
					o = fromArrayType(o);
				} else if(!(
						double.class.isAssignableFrom(c) || Double.class.isAssignableFrom(c)
						|| float.class.isAssignableFrom(c) || Float.class.isAssignableFrom(c)
						|| byte.class.isAssignableFrom(c) || Byte.class.isAssignableFrom(c)
						|| short.class.isAssignableFrom(c) || Short.class.isAssignableFrom(c)
						|| int.class.isAssignableFrom(c) || Integer.class.isAssignableFrom(c)
						|| long.class.isAssignableFrom(c) || Long.class.isAssignableFrom(c)
						|| String.class.isAssignableFrom(c)
						|| boolean.class.isAssignableFrom(c) || Boolean.class.isAssignableFrom(c))) {
					o = fromType(o);
				}
				// TODO support enums
				r.put(name, o);
			}
			clz = clz.getSuperclass();
		} while(clz != Object.class);
		return r;
	}
}
