package com.laytonsmith.core.apps;

import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;

/**
 * Provides utility methods for obtaining the ApiClient to interface with apps.methodscript.com.
 */
public class ApiUtil {

	private String baseApiUrl = "https://apps.methodscript.com/";
	private boolean debug = true;

	public ApiUtil() {

	}

	public ApiUtil(String baseApiUrl, boolean debug) {
		this.baseApiUrl = baseApiUrl;
		this.debug = debug;
	}

	public ApiClient getClient() {
		ApiClient client = new ApiClient();
		client.setBasePath(baseApiUrl);
		client.setConnectTimeout(60000);
		client.setDebugging(debug);
		return client;
	}

	public void registerDefaultClient() {
		Configuration.setDefaultApiClient(getClient());
	}

}
