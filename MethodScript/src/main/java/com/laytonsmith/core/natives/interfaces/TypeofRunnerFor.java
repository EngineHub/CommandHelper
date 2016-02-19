package com.laytonsmith.core.natives.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation ties the class to the "super interface" that this class will implement
 * certain methods for. Ideally, we would just have the interfaces implement the methods required by @typeof,
 * but that won't work in Java &lt; 8, which we need to support. Instead, we have a real class that extends
 * {@link TypeofRunnerIface} and uses this annotation to link it to the parent interface. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TypeofRunnerFor {
	public Class value();
}
