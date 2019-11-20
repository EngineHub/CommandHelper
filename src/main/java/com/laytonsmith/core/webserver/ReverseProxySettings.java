package com.laytonsmith.core.webserver;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.GroupData;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Static;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 */
public class ReverseProxySettings {

	private static final GroupData GENERAL_GROUP = new GroupData("General");

	private static Preferences prefs;

	/**
	 * The path to the preferences file. By default, this is not created, unlike most pref files.
	 * @return
	 */
	public static File getPrefsFile() {
		return new File(MethodScriptFileLocations.getDefault().getPreferencesDirectory(), "webserver.ini");
	}

	/**
	 *
	 * @return
	 */
	public static File getCtrlFolder() {
		return new File(MethodScriptFileLocations.getDefault().getCacheDirectory(), "webserverctrl");
	}

	public static void init(final File f) throws IOException {
		ArrayList<Preferences.Preference> a = new ArrayList<>();
        a.add(new Preferences.Preference("port", "16438",
                Preferences.Type.NUMBER,
                "The port to bind to.", GENERAL_GROUP));
		a.add(new Preferences.Preference("root", OSUtils.GetOS().isUnixLike() ? "/var/www" : "C:/inetpub/wwwroot",
				Preferences.Type.FILE,
				"The root of the web server.", GENERAL_GROUP));
		a.add(new Preferences.Preference("threads", "10",
				Preferences.Type.INT,
				"The number of concurrent requests to handle. The server will spin up this many threads at most"
						+ " to handle the incoming requests.", GENERAL_GROUP));

		prefs = new Preferences(Implementation.GetServerType().getBranding() + " webserver", Static.getLogger(), a);
        prefs.init(f);
	}

	public static int getPort() {
		return prefs.getIntegerPreference("port");
	}

	public static File getRoot() {
		return prefs.getFilePreference("root");
	}

	public static int getThreads() {
		return prefs.getIntegerPreference("threads");
	}
}
