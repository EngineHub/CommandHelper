package com.laytonsmith.core.apps;

import io.swagger.client.ApiClient;
import io.swagger.client.Configuration;

/**
 * Provides utility methods for obtaining the ApiClient to interface with apps.methodscript.com. Testing locally?
 * You probably want to start java with something like this:
 * <code>java -Dmethodscript.apps.server="http://localhost:8080"
 * -jar target/commandhelper-3.3.4-SNAPSHOT-full.jar</code>
 */
public class AppsApiUtil {

	private static final String APPS_SERVER_PROPERTY = "methodscript.apps.server";
	private static final String APPS_DEBUG = "methodscript.apps.debug";
	private static final int TIMEOUT = 30000;

	private String baseApiUrl = "https://apps.methodscript.com/";
	private boolean debug = false;


	/**
	 * Configures the default API setup. This defaults to the production server and debug mode off, but can be
	 * overridden with system properties "methodscript.apps.server" and "methodscript.apps.debug".
	 */
	public static void ConfigureDefaults() {
		new AppsApiUtil().registerDefaultClient();
	}

	public AppsApiUtil() {
		baseApiUrl = System.getProperty(APPS_SERVER_PROPERTY, baseApiUrl);
		debug = Boolean.parseBoolean(System.getProperty(APPS_DEBUG, Boolean.toString(debug)));
	}

	public AppsApiUtil(String baseApiUrl, boolean debug) {
		this.baseApiUrl = baseApiUrl;
		this.debug = debug;
	}

	public ApiClient getClient() {
		ApiClient client = new ApiClient();
		client.setBasePath(baseApiUrl);
		client.setConnectTimeout(TIMEOUT);
		client.setDebugging(debug);
		return client;
	}

	public void registerDefaultClient() {
		Configuration.setDefaultApiClient(getClient());
	}

}
