package com.laytonsmith.core.telemetry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TelemetryCategory {

	/**
	 * Returns the name of the TelemetryCategory.
	 * @return
	 */
	String name();

	/**
	 * Returns the group that this TelemetryCategory should fit in. Note that this is not extensible.
	 * @return
	 */
	TelemetryCategoryGroup group();

	/**
	 * Returns the type of the telemetry data. If a category is set as {@link TelemetryType#METRIC}, it is
	 * prohibited from uploading data, and if it is type {@link TelemetryType#LOG}, it is subject to a higher
	 * level of scrutiny for addition.
	 * @return
	 */
	TelemetryType type();

	/**
	 * Returns the documentation that accompanies the value.
	 * @return
	 */
	String purpose();

}
