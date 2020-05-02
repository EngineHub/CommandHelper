package com.laytonsmith.core.telemetry;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Prefs;
import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.channel.TelemetryChannel;
import com.microsoft.applicationinsights.channel.TelemetrySampler;
import com.microsoft.applicationinsights.channel.concrete.inprocess.InProcessTelemetryChannel;
import com.microsoft.applicationinsights.telemetry.JsonTelemetryDataSerializer;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Telemetry {

	private static volatile Telemetry telemetry = null;

	/**
	 * Gets the default {@link Telemetry} object.
	 * @return
	 */
	public static Telemetry GetDefault() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		Telemetry telemetry = Telemetry.telemetry;
		if(telemetry == null) {
			synchronized(Telemetry.class) {
				telemetry = Telemetry.telemetry;
				if(telemetry == null) {
					Telemetry.telemetry = telemetry = new Telemetry();
				}
			}
		}
		return telemetry;
	}

	public static void SetNoOpTelemetry() {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		Telemetry telemetry = Telemetry.telemetry;
		if(telemetry == null) {
			synchronized(Telemetry.class) {
				telemetry = Telemetry.telemetry;
				if(telemetry == null) {
					Telemetry.telemetry = new Telemetry() {
						@Override
						public void initializeTelemetry() {
							// NOOP
						}
					};
				}
			}
		}
	}

	public static String GetNagMessage() {
		return "Help make " + Implementation.GetServerType().getBranding() + " better! Enable telemetry (or disable"
				+ " this message) by changing the telemetry-on setting in preferences.ini to help us understand what"
				+ " features you're using and are most important to you. No personal information is collected.\n";
	}

	// change this, don't hardcode it, but at least make it a server lookup so it can be easily rotated.
	private static final String INSTRUMENTATION_KEY = "asdf";

	private boolean enabled = false;
	private TelemetryClient client;
	private TelemetryChannel stdoutChannel = new TelemetryChannel() {
		@Override
		public boolean isDeveloperMode() {
			return false;
		}

		@Override
		public void setDeveloperMode(boolean value) {

		}

		@Override
		public void send(com.microsoft.applicationinsights.telemetry.Telemetry item) {
			StringWriter writer = new StringWriter();
			try {
				item.serialize(new JsonTelemetryDataSerializer(writer));
			} catch (IOException ex) {
				ex.printStackTrace(StreamUtils.GetSystemErr());
			}
			StreamUtils.GetSystemOut().println("Telemetry data: " + writer.toString());
		}

		@Override
		public void stop(long timeout, TimeUnit timeUnit) {

		}

		@Override
		public void flush() {

		}

		@Override
		public void setSampler(TelemetrySampler telemetrySampler) {

		}

	};

	/**
	 * Nags the user, but only if the preference is set to nag them.
	 */
	public void doNag() {
		try {
			if(Prefs.TelemetryOn() == null) {
				StreamUtils.GetSystemOut().print(Telemetry.GetNagMessage());
			}
		} catch (Throwable t) {
			t.printStackTrace(StreamUtils.GetSystemErr());
			// Don't propogate upwards, so the program continues, but we also don't want
			// to fail completely silently.
		}
	}


	public void initializeTelemetry() {
		try {
			// Always configure this, but we won't load any more if the telemetry-on pref isn't "true".
			File config = MethodScriptFileLocations.getDefault().getTelemetryConfigFile();
			TelemetryPrefs.init(config);
		} catch (Throwable t) {
			StreamUtils.GetSystemErr().println("Could not initialize telemetry config!");
			t.printStackTrace(StreamUtils.GetSystemErr());
			return;
		}

		if(Objects.equals(Boolean.TRUE, Prefs.TelemetryOn())) {
			enabled = true;
		}

		if(enabled) {
			try {
				TelemetryConfiguration configuration = new TelemetryConfiguration();
				configuration.setInstrumentationKey(INSTRUMENTATION_KEY);
				if(Prefs.TelemetryAudit()) {
					configuration.setChannel(stdoutChannel);
				} else {
					configuration.setChannel(new InProcessTelemetryChannel());
				}
				TelemetryClient tc = new TelemetryClient(configuration);
				String session = UUID.randomUUID().toString();
				tc.getContext().getSession().setId(session);
				tc.getContext().getSession().setIsNewSession(true);
				tc.getContext().getCloud().setRoleInstance("");
				tc.getContext().getInternal().setNodeName(session);
				client = tc;
				// Use whatever mechanism works.
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					tc.flush();
				}));
				StaticLayer.GetConvertor().addShutdownHook(new Runnable() {
					@Override
					public void run() {
						tc.flush();
					}
				});
			} catch (Throwable t) {
				StreamUtils.GetSystemErr().println("Could not initialize telemetry!");
				t.printStackTrace(StreamUtils.GetSystemErr());
			}
		}

		metric(DefaultTelemetry.StartupMetric.class);
	}

	/**
	 * Sends a point in time metric.
	 * @param type
	 */
	public void metric(Class<? extends TelemetryValue> type) {
		TelemetryCategory tc = TelemetryValue.Helper.GetCategory(type);
		if(tc.type() != TelemetryType.METRIC) {
			return;
		}

		if(!TelemetryPrefs.GetTelemetryLoggable(type)) {
			return;
		}

		if(client != null) {
			client.trackEvent(tc.type().getPrefix() + "." + tc.name());
		}
	}

	/**
	 * Sends a metric with data.
	 * @param type
	 * @param properties A map of string to string properties. May be null.
	 * @param metrics A map of string to double metrics. May be null.
	 */
	public void log(Class<? extends TelemetryValue> type, Map<String, String> properties, Map<String, Double> metrics) {
		TelemetryCategory tc = TelemetryValue.Helper.GetCategory(type);
		if(tc.type() != TelemetryType.LOG) {
			return;
		}

		if(!TelemetryPrefs.GetTelemetryLoggable(type)) {
			return;
		}

		if(properties == null) {
			properties = new HashMap<>();
		}

		if(metrics == null) {
			metrics = new HashMap<>();
		}

		if(client != null) {
			client.trackEvent(tc.type().getPrefix() + "." + tc.name(), properties, metrics);
		}
	}

}
