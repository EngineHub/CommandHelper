
package com.laytonsmith.annotations.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to tag constructors in classes, and all known
 * subclasses will be unit tested to ensure that they each have the specified
 * constructors, so that factory type patterns will work, and have slightly better
 * error checks. The constructor must match the pattern EXACTLY. For instance, if our superclass
 * had a constructor defined as such:
 * <pre>
 * &#64;AbstractConstructor
 * protected AbstractClass(String s, Object o) {
 *   ...
 * }
 * </pre>
 * 
 * Then any class that extends AbstractConstructor (directly or indirectly) must also define
 * a constructor exactly like this:
 * <pre>
 * public ConcreteClass(String s, Object o){
 *    super(s, o);
 * }
 * </pre>
 * 
 * Other constructors may exist in either the superclass or subclasses, and the subclass
 * doesn't necessarily need to call super(...) in that particular constructor.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface AbstractConstructor {

}
