
package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tagged onto enum classes to allow the reflection mechanism to list out all the values
 * in the enum. This is semi-temporary.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MEnum {
	/**
	 * The name of the enum, in MethodScript.
	 * @return 
	 */
	String value();
}
