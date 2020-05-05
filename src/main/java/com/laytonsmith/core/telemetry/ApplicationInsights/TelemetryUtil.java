package com.laytonsmith.core.telemetry.ApplicationInsights;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.core.functions.Meta;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This package replicates as small a portion of the Application Insights SDK as possible, since we only use a tiny
 * fraction of the total package. The Bean classes are ripped from the Java SDK, and in general, are based on the
 * bond spec found here https://github.com/microsoft/ApplicationInsights-Home/tree/master/EndpointSpecs/Schemas/Bond
 */
public class TelemetryUtil {

	private final String instrumentationKey;
	private final String iKeyDashless;

	public TelemetryUtil(String instrumentationKey) {
		this.instrumentationKey = instrumentationKey;
		this.iKeyDashless = instrumentationKey.replaceAll("-", "");
	}

	private String sessionName = "untracked";
	private boolean newSession = false;

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public void setNewSession(boolean newSession) {
		this.newSession = newSession;
	}

	/**
	 * Returns a new Envelope object, which can be serialized and sent.
	 * @param metricName The name of the metric.
	 * @param properties The properties associated with the metric, may be null.
	 * @param measurements The measurements associated with the metric, may be null.
	 * @return
	 */
	public Envelope newEvent(String metricName,
			Map<String, String> properties,
			Map<String, Double> measurements) {
		Envelope env = new Envelope();
		env.setVer(1);
		env.setName("Microsoft.ApplicationInsights." + iKeyDashless + ".Event");
		env.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));
		env.setSampleRate(100);
		env.setIKey(instrumentationKey);
		Map<String, String> tags = generateStandardTags();
		tags.put("ai.internal.nodeName", sessionName);
		tags.put("ai.session.id", sessionName);
		tags.put("ai.session.isNew", Boolean.toString(newSession));
		env.setTags(tags);

		EventData eventData = new EventData();

		eventData.setVer(2);
		eventData.setName(metricName);
		if(properties != null && !properties.isEmpty()) {
			eventData.setProperties(properties);
		}

		if(measurements != null && !measurements.isEmpty()) {
			eventData.setMeasurements(measurements);
		}

		Data<EventData> data = new Data<>();
		data.setBaseType("EventData");
		data.setBaseData(eventData);

		env.setData(data);

		return env;
	}

	// https://github.com/microsoft/ApplicationInsights-Home/blob/master/EndpointSpecs/Schemas/Bond/ContextTagKeys.bond
	private ConcurrentMap<String, String> generateStandardTags() {
		ConcurrentMap<String, String> tags = new ConcurrentHashMap<>();
		tags.put("ai.application.ver", Long.toString(Meta.engine_build_date.GetEngineBuildDate()));
		tags.put("ai.device.locale", Locale.getDefault().toString());
		tags.put("ai.device.osVersion", OSUtils.GetOS().name());
		tags.put("ai.location.country", "Untracked");
		tags.put("ai.location.province", "Untracked");
		tags.put("ai.location.city", "Untracked");
		return tags;
	}
}
