package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to tag methods that will always throw a CRE,
 * and if possible, the compiler will use this information to turn it into
 * a compile error instead.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface unsupported {
	/**
	 * The error message that would be thrown.
	 * @return 
	 */
	public String value();
}
