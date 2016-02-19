
package com.laytonsmith.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to tag functions that should not be profile-able, when the profiler
 * is set to granularity 5. Generally, functions should always be profilable,
 * but in the event that isn't desirable, the class can be tagged with this
 * annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface noprofile {
	
}
