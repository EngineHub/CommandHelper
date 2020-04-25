package com.laytonsmith.core.telemetry;

/**
 *
 */
public enum TelemetryType {
	/**
	 * Logs are events that contain data, rather than just point events. In general, logs are much more highly
	 * suspect than metrics when it comes to privacy reviews.
	 */
	LOG("logs"),

	/**
	 * Metrics are point in time events, and do not contain any additional data other than the fact that they
	 * happened. The exact time of the event is not necessarily accurately recorded either, but the count of
	 * events is accurate.
	 */
	METRIC("metrics");
	private final String prefix;

	private TelemetryType(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return this.prefix;
	}

}
