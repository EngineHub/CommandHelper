package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be tagged onto the various task handlers. They should also extend {@link TaskHandler}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
public @interface taskhandler {

	/**
	 * Returns a string list of task property names. The handler should also have a list of get* methods, where *
	 * represents each property name.
	 *
	 * @return
	 */
	String[] properties();
}
