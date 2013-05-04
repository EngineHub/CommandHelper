
package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is to be tagged to the item that the abstraction wrapper is actually
 * wrapping. Magic methods can then be implemented in the superclasses of those
 * methods, which will allow for much more straightforward code regarding that item.
 * Only one field per class is allowed, and this is enforced with a unit test.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrappedItem {

}
