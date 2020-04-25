package com.laytonsmith.core.telemetry;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.Preferences.Type;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

/**
 *
 */
public class TelemetryPrefs {


	private static Preferences prefs;

	public static void init(final File f) throws IOException {
		String header = StreamUtils.GetResource("prefs-header.txt");
		ArrayList<Preference> a = new ArrayList<>();
		String def = Prefs.TelemetryCollectByDefault() ? "true" : "nag";
		Set<Class<?>> vs = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(TelemetryCategory.class);
		for(Class<?> v : vs) {
			TelemetryCategory tc = v.getAnnotation(TelemetryCategory.class);
			String prefix = tc.type().getPrefix();
			a.add(new Preference(prefix + "." + tc.name(), def, Type.STRING, tc.purpose(), tc.group().getGroupData()));
		}

		prefs = new Preferences(Implementation.GetServerType().getBranding(), Static.getLogger(), a, header);
		prefs.init(f);
		boolean doNag = false;
		for(Class<?> v : vs) {
			// Walk through the prefs again, seeing if there are any set to "nag", and nag at this point.
			TelemetryCategory tc = v.getAnnotation(TelemetryCategory.class);
			String p = prefs.getStringPreference(tc.type().getPrefix() + "." + tc.name());
			if("nag".equalsIgnoreCase(p)) {
				doNag = true;
				break;
			}
		}

		if(doNag) {
			StreamUtils.GetSystemOut().println("There is one or more new Telemetry Category defined in "
					+ f.getAbsolutePath() + ". Please take a look at the file to opt in or out of the collection"
							+ " category.");
		}
	}

	public static boolean GetTelemetryLoggable(Class<? extends TelemetryValue> type) {
		TelemetryCategory tc = type.getAnnotation(TelemetryCategory.class);
		if(tc == null) {
			// This is an error, but we should notice it because it's never getting logged. This case
			// means that the @TelemetryCategory is missing from the class.
			return false;
		}
		String name = tc.name();
		String prefix = tc.type().getPrefix();
		String p = prefs.getStringPreference(prefix + "." + name);
		if("nag".equalsIgnoreCase(p)) {
			return false;
		}
		return Objects.equals(Preferences.getBoolean(p), Boolean.TRUE);
	}
}
