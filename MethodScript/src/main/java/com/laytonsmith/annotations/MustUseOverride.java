
package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An interface can be tagged with this to indicate
 * that it requires implementing classes to tag
 * the implemented methods with @Override. This is
 * useful for if an interface has methods that are
 * subject to removal, and leaving the methods in the
 * subclasses would prove problematic, or would make
 * them unused, and therefore a code smell.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface MustUseOverride {

}
