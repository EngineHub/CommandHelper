package com.laytonsmith.PureUtilities.Common.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An aggressively deprecated method is one that is binary compatible, but source incompatible. This means that methods
 * that have been compiled against previously will continue to be available and work, but building against the latest
 * version of the code will cause compile errors.
 * <p>
 * To use this annotation, simply tag the method that would otherwise be deprecated with this annotation in addition to
 * the {@link Deprecated} annotation, {@code @deprecated} javadoc, and {@code @hidden} javadoc.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AggressiveDeprecation {

}
