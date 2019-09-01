package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Functions that have operator or keyword support should be marked with the annotation, and in strict mode, if the
 * function is used directly, it becomes a compiler warning (or error in UltraStrict mode).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperatorPreferred {
	/**
	 * The value is the operator that should be used in place of the function, which is used in the error message.
	 * @return
	 */
	String value();
}
