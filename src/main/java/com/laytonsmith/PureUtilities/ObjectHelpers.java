package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class contains methods for assisting in dealing with .equals, .hashCode, and .toString in objects, in a
 * maintainable and scalable way.
 * <p>
 * To use this class, implement .equals, .hashCode and .toString, and simply implement them as
 * {@code return ObjectHelper.DoEquals(this, that);}, {@code return ObjectHelper.DoHashCode(this, that);}, and
 * {@code return ObjectHelper.DoToString(this, that);} respectively. Then, for the fields that you wish to include in
 * these calculations, add the {@code @Equals}, {@code @HashCode}, or {@code @ToString} annotations to them (or
 * {@code @StandardField} if you wish to add all three.) You can also apply the annotation to the containing class,
 * which is the same as placing the annotation on ALL the fields.
 */
@SuppressWarnings({"checkstyle:parametername", "checkstyle:localvariablename"})
public class ObjectHelpers {

	/**
	 * Works the same as adding @Equals @HashCode and @ToString to this field/class.
	 */
	@Target({ElementType.FIELD, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface StandardField {}

	/**
	 * When tagged with this annotation or {@link StandardField}, includes this field in the .equals calculation.
	 */
	@Target({ElementType.FIELD, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Equals {}

	/**
	 * When tagged with this annotation or {@link StandardField}, includes this field in the .hashCode calculation.
	 */
	@Target({ElementType.FIELD, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface HashCode {}

	/**
	 * When tagged with this annotation or {@link StandardField}, includes this field in the .equals calculation.
	 */
	@Target({ElementType.FIELD, ElementType.TYPE})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ToString {}

	/**
	 * Implement the .equals method in the class as such:
	 * <pre>
	 * &#64;Override
	 * &#64;SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	 * public boolean equals(Object obj) {
	 * 	return ObjectHelpers.DoEquals(this, obj);
	 * }
	 * </pre>
	 * Then add the {@code @Equals} or {@code @StandardField} annotation to the fields you wish to be used in the
	 * calculation, or to the class itself to use all fields.
	 * @param _this {@code this} object
	 * @param that The other object being compared, i.e. the one being passed in to .equals
	 * @return True if the specified fields are equal.
	 */
	public static boolean DoEquals(Object _this, Object that) {
		if(_this == that) {
			return true;
		}
		if(that == null) {
			return false;
		}
		if(_this.getClass() != that.getClass()) {
			return false;
		}
		for(Field f : _this.getClass().getDeclaredFields()) {
			if(
				f.getDeclaringClass().getAnnotation(Equals.class) == null
				&& f.getDeclaringClass().getAnnotation(StandardField.class) == null
				&& f.getAnnotation(Equals.class) == null
				&& f.getAnnotation(StandardField.class) == null
			) {
				continue;
			}
			if(f.getName().startsWith("$")) {
				// If the field starts with this name, it's a dynamic field that was inserted by some
				// dynamic code. While it may be nice to know these in some cases, this is not the
				// general use case supported by this code, so we skip these, so our results are
				// deterministic.
				continue;
			}
			Object _thisO = ReflectionUtils.get(_this.getClass(), _this, f.getName());
			Object thatO = ReflectionUtils.get(_this.getClass(), that, f.getName());
			if(!Objects.equals(_thisO, thatO)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Implement the .hashCode method in the class as such:
	 * <pre>
	 * &#64;Override
	 * public int hashCode() {
	 * 	return ObjectHelpers.DoHashCode(this);
	 * }
	 * </pre>
	 * Then add the {@code @HashCode} or {@code @StandardField} annotation to the fields you wish to be used in the
	 * calculation, or to the class itself to use all fields.
	 * @param _this {@code this} object
	 * @return The calculated hash code, which at its core, uses {@link Arrays#hashCode(java.lang.Object[])}.
	 */
	public static int DoHashCode(Object _this) {
		List<Object> calculatedFields = new ArrayList<>();
		for(Field f : _this.getClass().getDeclaredFields()) {
			if(
				f.getDeclaringClass().getAnnotation(HashCode.class) == null
				&& f.getDeclaringClass().getAnnotation(StandardField.class) == null
				&& f.getAnnotation(HashCode.class) == null
				&& f.getAnnotation(StandardField.class) == null
			) {
				continue;
			}
			if(f.getName().startsWith("$")) {
				// If the field starts with this name, it's a dynamic field that was inserted by some
				// dynamic code. While it may be nice to know these in some cases, this is not the
				// general use case supported by this code, so we skip these, so our results are
				// deterministic.
				continue;
			}
			Object _thisO = ReflectionUtils.get(_this.getClass(), _this, f.getName());
			calculatedFields.add(_thisO);
		}
		if(calculatedFields.isEmpty()) {
			return 0;
		}
		return Arrays.hashCode(calculatedFields.toArray(new Object[calculatedFields.size()]));
	}

	/**
	 * Implement the .toString method in the class as such:
	 * <pre>
	 * &#64;Override
	 * public String toString() {
	 * 	return ObjectHelpers.DoToString(this);
	 * }
	 * </pre>
	 * Then add the {@code @ToString} or {@code @StandardField} annotation to the fields you wish to be used in the
	 * calculation, or to the class itself to use all fields.
	 * @param _this {@code this} object
	 * @return The toString of the object, using all the provided field's toString as part of the resulting string.
	 */
	public static String DoToString(Object _this) {
		if(_this == null) {
			return "null";
		}
		List<String> values = new ArrayList<>();
		boolean hasAnnotations = false;
		for(Field f : _this.getClass().getDeclaredFields()) {
			if(
				f.getDeclaringClass().getAnnotation(ToString.class) == null
				&& f.getDeclaringClass().getAnnotation(StandardField.class) == null
				&& f.getAnnotation(ToString.class) == null
				&& f.getAnnotation(StandardField.class) == null
			) {
				continue;
			}
			if(f.getName().startsWith("$")) {
				// If the field starts with this name, it's a dynamic field that was inserted by some
				// dynamic code. While it may be nice to know these in some cases, this is not the
				// general use case supported by this code, so we skip these, so our results are
				// deterministic.
				continue;
			}
			hasAnnotations = true;
			Object _thisO = ReflectionUtils.get(_this.getClass(), _this, f.getName());
			values.add(f.getName() + "=" + DoToString(_thisO));
		}
		if(_this.getClass().isArray() || _this.getClass().getDeclaredFields().length > 0 && !hasAnnotations) {
			// Use the default toString on this object. This can happen, because we call ourselves recursively.
			if(_this.getClass().isArray()) {
				int length = Array.getLength(_this);
				StringBuilder b = new StringBuilder();
				b.append(_this.getClass().getSimpleName()).append(" {");
				for(int i = 0; i < length; i++) {
					if(i > 0) {
						b.append(", ");
					}
					// Do not recurse further, as self referential arrays would cause a SOE, and to properly
					// avoid that would require a much more complex system.
					b.append(Objects.toString(Array.get(_this, i)));
				}
				b.append('}');
				return b.toString();
			} else {
				return _this.toString();
			}
		}
		return _this.getClass().getSimpleName() + " {" + StringUtils.Join(values, ", ") + '}';
	}

}
