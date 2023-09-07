package com.laytonsmith.core.compiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A ConditionalSelfStatement is a function which is sometimes a self statement, but others not, depending on
 * the argument list. This list should remain small and non-growing, however, this requires special handling in the
 * compiler to determine if a statement is missing or not.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConditionalSelfStatement {

}
