package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the typeof an object in MethodScript. Defined as an annotation, to force overriding and static values.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
public @interface typeof {

	/**
	 * The name of this object, as defined in MethodScript.
	 *
	 * @return
	 */
	String value();
}
