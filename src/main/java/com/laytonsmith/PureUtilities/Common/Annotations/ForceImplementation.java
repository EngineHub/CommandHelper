package com.laytonsmith.PureUtilities.Common.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation, when tagging a constructor/method, indicates that ALL subclasses must include a constructor/method
 * with the same parameter signature. This check is enforced at compile time.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface ForceImplementation {

}
