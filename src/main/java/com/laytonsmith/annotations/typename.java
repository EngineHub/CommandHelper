package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This should be used to tag data types, to allow for more efficient retrieval
 * of the type name associated with this class type. Only things that extend Mixed
 * should tag with this. If the value is an empty string, it is assumed that it can only be determined
 * dynamically, and thus typeName() will be called on it, if applicable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface typename {
	String value() default "";
}
