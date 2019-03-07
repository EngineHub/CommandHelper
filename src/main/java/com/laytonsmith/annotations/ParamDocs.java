package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used in combination with {@link ExposedProperty} for methods that have parameters. Each
 * parameter should be tagged with this annotation, and provide the documentation about that parameter.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamDocs {
	/**
	 * Documentation about this parameter.
	 * @return
	 */
	String value();
}
