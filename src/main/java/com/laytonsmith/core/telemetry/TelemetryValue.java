package com.laytonsmith.core.telemetry;

import java.lang.annotation.Annotation;

/**
 * Classes tagged with {@link TelemetryPrefs.TelemetryCategory} should also implement this interface.
 */
public interface TelemetryValue {

	public static class Helper {

		/**
		 * Returns the TelemetryCategory. If the class was not annotated, a default TelemetryCategory object
		 * is returned, which has an empty string as a name, which cannot be registered, and therefore should
		 * always return false.
		 * @param value
		 * @return
		 */
		public static TelemetryCategory GetCategory(Class<? extends TelemetryValue> value) {
			TelemetryCategory tc = value.getAnnotation(TelemetryCategory.class);
			if(tc == null) {
				return new TelemetryCategory() {
					@Override
					public String name() {
						// This will always cause the comparison to always return false.
						return "";
					}

					@Override
					public TelemetryCategoryGroup group() {
						return TelemetryCategoryGroup.GENERAL_GROUP;
					}

					@Override
					public TelemetryType type() {
						return null;
					}

					@Override
					public String purpose() {
						return "";
					}

					@Override
					public Class<? extends Annotation> annotationType() {
						return TelemetryCategory.class;
					}
				};
			} else {
				return tc;
			}
		}
	}
}
