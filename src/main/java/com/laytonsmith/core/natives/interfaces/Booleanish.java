package com.laytonsmith.core.natives.interfaces;

import com.laytonsmith.annotations.typeof;
import com.laytonsmith.core.ArgumentValidation;
import com.laytonsmith.core.constructs.CClassType;
import com.laytonsmith.core.constructs.Target;


/**
 * A value that is Booleanish is a non-boolean value, that can be converted to Boolean.
 */
@typeof("ms.lang.Booleanish")
public interface Booleanish extends Mixed {

	@SuppressWarnings("FieldNameHidesFieldInSuperclass")
	public static final CClassType TYPE = CClassType.get("ms.lang.Booleanish");

	/**
	 * Returns true if this value is a trueish value. Each implementation is free to define this as they wish. In
	 * general, code that supports Booleanish values should not use this method directly, use
	 * {@link ArgumentValidation#getBoolean(com.laytonsmith.core.natives.interfaces.Mixed,
	 * com.laytonsmith.core.constructs.Target)}, which ensures that the error message, if this is not an actual
	 * Booleanish value, is standardized. In general, methods should not accept a Booleanish value (with some critical
	 * exceptions, such as if(), for(), etc) as in the future, this will prevent functions from being fully strongly
	 * typed in strict mode. In non-strict mode (or strict mode with the auto keyword) Booleanish types will be cross
	 * cast to a boolean first anyways, so there is no point in accepting Booleanish values.
	 *
	 * @param t The code target, in case there are errors that are thrown, the correct target can be provided in the
	 * error.
	 * @return True if the value is trueish, false if it is falseish.
	 */
	boolean getBooleanValue(Target t);
}
