package com.laytonsmith.core.objects;

import com.laytonsmith.annotations.MEnum;

/**
 * These are the list of modifers that are valid on an Element (i.e. property or method).
 */
@MEnum("ms.lang.ElementModifier")
public enum ElementModifier {
	/**
	 * A final method is one that cannot be overridden in subclasses.
	 */
	FINAL,
	/**
	 * A static element is one that is not tied to the containing class's instance scope, but is tied to the static
	 * scope. It is available by dereferencing the ClassType with the :: operator.
	 */
	STATIC,
	/**
	 * An abstract method is one that is not defined in the class. Only abstract classes may contain these
	 * methods. This is not allowed on fields.
	 */
	ABSTRACT,
	/**
	 * A native element is one whose value is drawn from the native code. This is only valid in a class that is itself
	 * marked as native.
	 */
	NATIVE,
	/**
	 * An immutable object is one in which none of the fields may be set, other than within the constructor.
	 * When used on a variable definition (which creates a type overload, {@code int} is not the same type as
	 * {@code immutable int}), this prevents use of any methods which could directly or
	 * indirectly set a field (or setting of fields directly).
	 * Mutable fields may be cast to immutable fields, but not vice versa.
	 */
	IMMUTABLE;
}
