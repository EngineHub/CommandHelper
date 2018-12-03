/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;

/**
 * Represents a class that supports passing by value. Value types must meet a few assumptions, they are immutable,
 * it may be faster to pass by value than by reference, and if value a and b are equal, then they could be also the same
 * reference with no ill effects to any programs. The compiler may choose to pass by value or by reference, and it
 * should be the same either way.
 *
 * Primitives are a good example of this, (and in fact, the primitive class implements this interface) but more
 * complex object types may benefit from this as well.
 */
@typeof("ms.lang.ValueType")
public interface ValueType extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.ValueType");

	/**
	 * Returns a duplicated value of this object. The duplicate MUST be equals(), and it MUST NOT be ref_equals(). The
	 * compiler may choose to call this method or not, if not, then the values would be ref_equals.
	 * @return The interface defines the return type as {@code ValueType}, but the actual type returned MUST be of the
	 * type defined in this class. The method is @ForceImplementation'd, to remind the programmer that this must be
	 * overridden in every class, to return the appropriate type.
	 */
	@ForceImplementation
	ValueType duplicate();

}
