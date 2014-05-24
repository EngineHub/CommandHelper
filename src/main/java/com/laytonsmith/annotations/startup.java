package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods with this annotation are triggered at startup. Note that "startup"
 * is relative. This may or may not be a restart or an initial start. See {@link shutdown}.
 * The method annotated with this annotation should take 0 parameters, and return void.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated // Use LifeCycle classes instead.
public @interface startup {
	
}
