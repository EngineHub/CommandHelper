package com.laytonsmith.tools.langserv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tagged on the class which should be used for the given method name. The request with the given method name will be
 * deserialized into this class. Alternatively, when tagged onto a method, provides the method that should be
 * run with the associated class sent as a parameter.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LangServMethod {
	/**
	 * The method name.
	 * @return
	 */
	String value();
}
