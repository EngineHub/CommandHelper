package com.laytonsmith.PureUtilities.Common.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An aggressively deprecated method is one that is binary compatible, but source incompatible. This means that methods
 * that have been compiled against previously will continue to be available and work, but building against the latest
 * version of the code will cause compile errors. This makes writing code which downstream dependencies use much easier,
 * as this puts the onus completely on the developer and not the end user, but only when they go to make a new build
 * for some other reason anyways.
 * <p>
 * To use this annotation, simply tag the method that would otherwise be deprecated with this annotation in addition to
 * the {@link Deprecated} annotation, {@code @deprecated} javadoc, and {@code @hidden} javadoc.
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface AggressiveDeprecation {
	/**
	 * Returns the minimum version in which this will be actually removed. This is not
	 * strictly a guarantee, depending on external factors, but should inform the future
	 * decision.
	 * @return
	 */
	String removalVersion();

	/**
	 * Returns the date that this was deprecated, in YYYY-MM-DD format. In general, removals
	 * don't happen for at least a year, though that is not always the case, and this date is
	 * simply to inform the future decision about when to do the actual removal.
	 * @return
	 */

	String deprecationDate();
}
