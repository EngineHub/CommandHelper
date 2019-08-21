package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tagged onto "enum" classes to allow the reflection mechanism to list out all the values in the enum. It is important
 * to note that the underlying real enum should not be tagged with the {@link MEnum} annotation. The type in
 * MethodScript will be inherited from this annotation, and the values that are listed in the "values" method in this
 * class will be used.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MDynamicEnum {

	/**
	 * The name of the enum, in MethodScript.
	 *
	 * @return
	 */
	String value();
}
