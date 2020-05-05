package com.laytonsmith.core.telemetry;

/**
 * If you want to add a new telemetry object, you also have to add it to apps.methodscript.com, or the server
 * will reject it.
 */
public class DefaultTelemetry {

	@TelemetryCategory(name = "methodscript.startup",
			group = TelemetryCategoryGroup.GENERAL_GROUP,
			type = TelemetryType.METRIC,
			purpose = "This category logs startup of the program.")
	public static class StartupMetric implements MetricTelemetryValue {}

	@TelemetryCategory(name = "methodscript.startupMode",
			group = TelemetryCategoryGroup.GENERAL_GROUP,
			type = TelemetryType.LOG,
			purpose = "This category logs the startup mode. For instance, if you run this from the command line,"
					+ " this is a different mode than running it as a plugin.")
	public static class StartupModeMetric implements LogTelemetryValue {}

}
