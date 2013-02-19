package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class that is immutable, that is, it would be tagged with the immutable keyword in mscript.
 * This is used by the compiler to move many operations into compile time, where otherwise possible. If a superclass is
 * immutable, all subclasses must also be immutable.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface immutable {
	
}
