

package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This marks functions that should not be tested by the boilerplate test mechanisms,
 * for instance, the ones that running a blind test with could cause system problems,
 * or other undesired behavior. This only prevents the function execution, the rest
 * of the tests (documentation, etc) will be run, and this will not prevent specific
 * tests from being run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface noboilerplate {
    
}
