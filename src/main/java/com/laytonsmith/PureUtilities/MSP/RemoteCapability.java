package com.laytonsmith.PureUtilities.MSP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If this element is used, it must have a capability request associated with
 * it. The capability name will be calculated to be the fully qualified name of the class
 * (if annotating a class element) or the fully qualified name of the class "dot" the name
 * of the field (if annotating a field). The value may optionally be hardcoded as a string
 * provided in the value, which defaults to the NULL constant defined here.
 *
 * @author lsmith
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface RemoteCapability {
	/**
	 * Java doesn't allow actual nulls for annotation values, so use this instead, to pick the "default" value,
	 * or simply leave the value blank.
	 */
	public static final String NULL = "~!@#$%^&*()_+NULL value, do not use this, unless you intend on this value being null+_)(*&^%$#@!~";
	String value() default NULL;
}
