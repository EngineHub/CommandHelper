package com.laytonsmith.PureUtilities.Common.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation ties the class to the "super interface" that this class will implement certain methods for. Ideally,
 * we would just have the interfaces implement the methods required by @typeof, but that won't work in Java &lt; 8,
 * which we need to support. Instead, we have a real class that extends {@link MScriptInterfaceRunner} and uses this
 * annotation to link it to the parent interface. Methods that are forced in all subclasses using
 * {@link ForceImplementation} can use this to provide the implementations for interfaces. This also works for abstract
 * classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InterfaceRunnerFor {

	public Class<?> value();
}
