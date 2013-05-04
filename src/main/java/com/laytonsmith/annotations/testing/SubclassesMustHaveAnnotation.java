
package com.laytonsmith.annotations.testing;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes can be tagged with this annotation to denote that subclasses
 * must tag themselves with other annotations. This also includes the parent
 * class in the check. Interfaces may also have this annotation, and classes that implement
 * it must meet this requirement as well.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubclassesMustHaveAnnotation {
	Class<? extends Annotation> [] value();
}
