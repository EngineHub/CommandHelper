package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
public @interface mobject {

	/**
	 * The name of the object, as usable in source code.
	 *
	 * @return
	 */
	String value();
}
