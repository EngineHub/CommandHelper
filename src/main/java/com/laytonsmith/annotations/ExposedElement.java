package com.laytonsmith.annotations;

import com.laytonsmith.core.environments.Environment;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be tagged onto methods and fields that should be exposed to user code through native classes.
 * In order for these
 * to succeed, the containing class should ultimately extend Mixed, if a field, it must be a type that extends Mixed,
 * if a method, the return type must extend Mixed, and all the parameters must extend mixed.
 * The exception to the Mixed rule is that various types have special handling, and
 * the system will transparently convert between java types and MethodScript types for you. Even still, you may mix uses
 * of Mixed values and these types.
 * <p>
 * Meanwhile, in the native methodscript core library, this must be a defined class. The class itself must be marked
 * as native, and so must the method or field. The field may not be assigned a value in the declaration, and the method
 * may not have a body. It is highly recommended that the
 *
 * The first two parameters in a method must be {@link Environment} and {@link Target}, but the rest of the parameters
 * should correspond to the parameters defined in the MethodScript method.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExposedElement {

}
