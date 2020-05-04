package com.laytonsmith.core.telemetry;

import com.laytonsmith.core.telemetry.ApplicationInsights.Envelope;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.telemetry.ApplicationInsights.TelemetryUtil;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class Telemetry {

	private static volatile Telemetry telemetry = null;
	// This is not the real instrumentation key, it is replaced with the real one on the server side.
	private static final String INSTRUMENTATION_KEY = "28cb72ef-45fe-4634-b7e3-ea672db27cf0";

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

	private boolean enabled = false;
	private TelemetryChannel channel;
	private TelemetryUtil client;

	private static final TelemetryChannel STDOUT_CHANNEL = new TelemetryChannel() {

		@Override
		public void send(Envelope item) {
			StreamUtils.GetSystemOut().println("Telemetry data: " + item.serialize());
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
				client = new TelemetryUtil(INSTRUMENTATION_KEY);
				if(Prefs.TelemetryAudit()) {
					channel = STDOUT_CHANNEL;
				} else {
					channel = new ProxyTelemetryChannel(new TelemetryProxy());
				}
				String session = UUID.randomUUID().toString();
				client.setSessionName(session);
				client.setNewSession(true);

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
			channel.send(client.newEvent(tc.type().getPrefix() + "." + tc.name(), null, null));
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
			channel.send(client.newEvent(tc.type().getPrefix() + "." + tc.name(),
					new ConcurrentHashMap<>(properties),
					new ConcurrentHashMap<>(metrics)));
		}
	}

	private interface TelemetryChannel {
		/**
		 * Sends the envelope to the correct channel.
		 * @param envelope
		 */
		void send(Envelope envelope);
	}

	class ProxyTelemetryChannel implements TelemetryChannel {

		private final TelemetryProxy proxy;

		public ProxyTelemetryChannel(TelemetryProxy proxy) {
			this.proxy = proxy;
		}

		@Override
		public void send(Envelope item) {
			String body = item.toString();
			proxy.submit(body);
		}
	}
}
