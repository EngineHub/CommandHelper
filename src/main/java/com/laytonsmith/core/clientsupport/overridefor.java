package com.laytonsmith.core.clientsupport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Cailin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename")
public @interface overridefor {
	/**
	 * Returns the Function class that this is overriding. The class should also be an instanceof Function.
	 * @return
	 */
	Class value();
}
