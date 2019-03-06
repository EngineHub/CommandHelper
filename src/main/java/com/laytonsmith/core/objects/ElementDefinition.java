package com.laytonsmith.core.objects;

import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.CNull;
import com.laytonsmith.core.natives.interfaces.Mixed;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * An ElementDefinition is the definition of elements within an object. These can be either properties or methods, as
 * they are handled the same in most ways, this superclass can safely represent both. For methods, the defaultValue
 * should always be provided, a Callable. There is special support for native types, in which a Method/Field is
 * provided.
 */
public class ElementDefinition {
	private final AccessModifier accessModifier;
	private final Set<ElementModifier> elementModifiers;
	private final CClassType type;
	private final String name;
	private final Mixed defaultValue;

	private Method nativeMethod = null;
	private Field nativeField = null;

	public ElementDefinition(
			AccessModifier accessModifier,
			Set<ElementModifier> elementModifiers,
			CClassType type,
			String name,
			Mixed defaultValue
	) {
		this.accessModifier = accessModifier;
		this.elementModifiers = elementModifiers;
		this.type = type;
		this.name = name;
		this.defaultValue = defaultValue;
	}

	/**
	 * If the underlying class is a native class, the actual Method can be provided here. No
	 * checks are done at this point, but it MUST be true that the method return type is castable to {@link Mixed}, as
	 * well as all arguments.
	 * @param m
	 */
	public void setNativeMethod(Method m) {
		this.nativeMethod = m;
	}

	/**
	 * If the underlying class is a native class, the actual Field can be provided here. No
	 * checks are done at this point, but it MUST be true that the field type is castable to {@link Mixed}.
	 * @param f
	 */
	public void setNativeField(Field f) {
		this.nativeField = f;
	}

	/**
	 * Returns true if the underlying item is a reference to a native class.
	 * @return
	 */
	public boolean isNative() {
		return this.isNativeMethod() || this.isNativeField();
	}

	/**
	 * Returns true if the underlying item is a reference to a native class method.
	 * @return
	 */
	public boolean isNativeMethod() {
		return this.nativeMethod != null;
	}

	/**
	 * Returns true if the underlying item is a reference to a native class field.
	 * @return
	 */
	public boolean isNativeField() {
		return this.nativeField != null;
	}

	/**
	 * The access modifier of the element.
	 * @return
	 */
	public AccessModifier getAccessModifier() {
		return accessModifier;
	}

	/**
	 * The element modifiers.
	 * @return
	 */
	public Set<ElementModifier> getElementModifiers() {
		return elementModifiers;
	}

	/**
	 * The type of the element. For methods, this is the return type.
	 * @return
	 */
	public CClassType getType() {
		return type;
	}

	/**
	 * The name of the element.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * The default value of the element. This will be {@link CNull#UNDEFINED} if this was a property of the class with
	 * no assignment at all, and {@link CNull#NULL} if it was defined as null. For methods, this should be the actual
	 * Callable instance.
	 *
	 * Iff this is a native type, this will this return (java) null, though that fact should not be relied on,
	 * use {@link #isNative()} to determine that for sure.
	 * @return
	 */
	public Mixed getDefaultValue() {
		return defaultValue;
	}

	/**
	 * If this is a native class, and it represents a method, this should return a reference to the Java Method.
	 * It will return null if this is not a native class, but that
	 * fact should not be relied on, use {@link #isNative()} to determine that for sure.
	 * @return
	 */
	public Method getNativeMethod() {
		return nativeMethod;
	}

	/**
	 * If this is a native class, and it represents a field, this should return a reference to the Java Field.
	 * It will return null if this is not a native class, but
	 * that fact should not be relied on, use {@link #isNative()} to determine that for sure.
	 * @return
	 */
	public Field getNativeField() {
		return nativeField;
	}

}
