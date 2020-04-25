package com.laytonsmith.core.telemetry;

/**
 *
 */
public class DefaultTelemetry {

	@TelemetryCategory(name = "methodscript.startup",
			group = TelemetryCategoryGroup.GENERAL_GROUP,
			type = TelemetryType.METRIC,
			purpose = "This category logs startup of the program.")
	public static class StartupMetric implements TelemetryValue {}

	@TelemetryCategory(name = "methodscript.startupMode",
			group = TelemetryCategoryGroup.GENERAL_GROUP,
			type = TelemetryType.LOG,
			purpose = "This category logs the startup mode. For instance, if you run this from the command line,"
					+ " this is a different mode than running it as a plugin.")
	public static class StartupModeMetric implements TelemetryValue {}

}
