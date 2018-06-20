package com.laytonsmith.annotations;

import com.laytonsmith.abstraction.Implementation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation denotes that the marked class implements the noted server type's Convertor. Only one class should
 * exist for each type, otherwise the behavior is undefined, but an error will be thrown.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
public @interface convert {

	Implementation.Type type();
}
