
package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to allow for Functions to document other classes that are related to this one.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface seealso {
	/**
	 * A list of classes that should be "seen also". These classes should be tagged with
	 * &#64;api, and implement Function.
	 * @return 
	 */
	Class[] value();
}
