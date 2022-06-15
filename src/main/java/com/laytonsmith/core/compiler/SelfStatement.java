package com.laytonsmith.core.compiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A SelfStatement is a function which does not return anything, and does not require a semi-colon afterwards, it always
 * terminates itself, thus being a self-statement. In all cases, this function must also return void, the only exception
 * is the if() function, which has special handling in the compiler.
 * <p>
 * In the compiler, all SelfStatements are automatically broken into their own statement.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelfStatement {

}
