package com.laytonsmith.annotations;

import com.laytonsmith.core.MSVersion;
import com.laytonsmith.core.environments.Environment;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be tagged onto methods and fields that should be exposed to user code. In order for these
 * to succeed, the containing class should ultimately extend Mixed, if a field, it must be a type that extends Mixed,
 * if a method, the return type must extend Mixed, and all the parameters must extend mixed. The access modifier of
 * the method must be public. The exception to the Mixed rule is that various types have special handling, and
 * the system will transparently convert between java types and MethodScript types for you. Even still, you may mix uses
 * of Mixed values and these types.
 *
 * The first two parameters in a method must be {@link Environment} and {@link Target}.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExposedProperty {
	/**
	 * The documentation for the method.
	 * @return
	 */
	String docs();
	/**
	 * When was the method added?
	 * @return
	 */
	MSVersion since();
	/**
	 * Documentation about the return value.
	 * @return
	 */
	String returnDoc();
}
