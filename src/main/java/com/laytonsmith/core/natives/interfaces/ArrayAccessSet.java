package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;

/**
 * Things that implement this can set the value via square bracket notation. The generic type provided is the index
 * type. IMPORTANT NOTE: If CNull is passed in, that will be converted to a Java null before calling the set method,
 * since CNull won't extend whatever type T you pass in here, but is a valid input parameter (in general).
 *
 * @author Cailin
 */
@typeof("ms.lang.ArrayAccessSet")
public interface ArrayAccessSet extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get(ArrayAccessSet.class);

	/**
	 * Sets the value at the specified index in the object.
	 *
	 * @param index The zero-based index.
	 * @param value The value to set.
	 * @param t The code target.
	 */
	public void set(Mixed index, Mixed value, Target t);
}
