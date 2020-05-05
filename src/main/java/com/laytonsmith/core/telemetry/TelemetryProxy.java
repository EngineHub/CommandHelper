package com.laytonsmith.core.telemetry;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.RunnableQueue;
import io.swagger.client.ApiException;
import io.swagger.client.api.TelemetryApi;

/**
 * This class manages submitting telemetry data to apps.methodscript.com.
 */
public class TelemetryProxy {

	private String key = null;
	private final TelemetryApi api = new TelemetryApi();
	private final RunnableQueue queue;
	private final DaemonManager dm = new DaemonManager();
	private boolean enabled = true;


	public TelemetryProxy() {
		queue = new RunnableQueue("TelemetrySubmitter");
	}

	public void submit(String data) {
		if(!enabled) {
			return;
		}
		queue.invokeLater(dm, () -> {
			submit0(data);
		});
	}

	private void submit0(String data) {
		if(key == null) {
			regenKey();
			if(key == null || !enabled) {
				// We've been disabled.
				return;
			}
		}
		try {
			api.telemetryKeyPost(data, key);
		} catch (ApiException ex) {
			int code = ex.getCode();
			if(code == 403) {
				// Retry after key regen
				regenKey();
				if(!enabled) {
					// We've been disabled.
					return;
				}
				try {
					api.telemetryKeyPost(data, key);
				} catch (ApiException ex1) {
					// Give up permanently
					enabled = false;
				}
			} else if(code == 502) {
				// Hmm, this is unfortunate, but let's not permanently disable.
			}
		}
	}

	private void regenKey() {
		try {
			key = api.telemetryGet();
		} catch (ApiException ex) {
			int code = ex.getCode();
			if(code == 403) {
				// We've been blocked! Give up permanently.
				enabled = false;
			}
		}
	}
}
