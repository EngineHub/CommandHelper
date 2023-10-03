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

	/**
	 * The version in which this will (automatically) start being aggressively deprecated. The usual
	 * pattern is to set this to the next version number, and then the removal version to the one
	 * after that. Say that the current version is version 1.0.0. If you say that the deprecationVersion
	 * is 1.1.0, and the removal version is 1.2.0, then for dependencies building against 1.0.0, they
	 * will continue to work completely as is, and can continue to build just fine (though with a deprecation
	 * warning, assuming you also place the Deprecated annotation). Then, automatically, when the current
	 * version bumps to 1.1.0, it will automatically start being aggressively deprecated. Downstream
	 * dependencies built against 1.0.0 will continue to be binary compatible, but cannot be built
	 * against 1.1.0 without changes. Finally, 1.2.0, the method can actually be removed, as the
	 * binary compatibility was no longer guaranteed.
	 * @return
	 */
	String deprecationVersion();
}
