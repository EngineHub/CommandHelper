package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is annotated on classes of functions, or individual functions to indicate that they are part of the core API.
 * This annotation has both a practical meaning, and a legal meaning. Functions tagged with this annotation are unable
 * to be overridden by external code, both for consistency reasons and security reasons. Additionally, any
 * implementation of MethodScript must correctly implement all of the functions labelled as core in order to be
 * considered a valid implementation.
 *
 * This also has a legal purpose. All functions tagged as part of the core fall under the special contribution license,
 * and any modifications by third parties must be released of all copyrights.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface core {

}
