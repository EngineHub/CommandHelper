

package com.laytonsmith.annotations;

import com.laytonsmith.abstraction.Implementation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation denotes that the marked class implements the noted
 * server type's Convertor. Only one class should exist for each type,
 * otherwise the behavior is undefined, but an error will be thrown.
 * @author layton
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface convert {
    Implementation.Type type();
}
