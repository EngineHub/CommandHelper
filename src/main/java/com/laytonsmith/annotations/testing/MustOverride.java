
package com.laytonsmith.annotations.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be tagged to methods that ALL subclasses must implement this method, individually.
 * They may still simply call super.method, but they must at least declare the method in their body. Methods in interfaces
 * can also be tagged with this.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MustOverride {
	/**
	 * If true, only direct subclasses must implement this method. Otherwise, ALL subclasses
	 * must implement this method.
	 * @return 
	 */
	boolean directOnly() default false;
}
