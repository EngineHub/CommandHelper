package com.laytonsmith.core.compiler.analysis;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.Preferences.Type;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.MethodScriptFileLocations;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 */
public final class StaticAnalysisConfiguration {

	private static volatile StaticAnalysisConfiguration config = null;

	public static StaticAnalysisConfiguration GetConfiguration() throws IOException {
		@SuppressWarnings("LocalVariableHidesMemberVariable")
		StaticAnalysisConfiguration config = StaticAnalysisConfiguration.config;
		if(config == null) {
			synchronized(StaticAnalysisConfiguration.class) {
				config = StaticAnalysisConfiguration.config;
				if(config == null) {
					StaticAnalysisConfiguration.config = config = new StaticAnalysisConfiguration();
					config.load();
				}
			}
		}
		return config;
	}

	private final Preferences prefs;

	private StaticAnalysisConfiguration() {
		List<Preferences.Preference> defaults = new ArrayList<>(Arrays.asList(
			// TODO - Replace "off" with "on" as soon as static analysis should be enabled by default.
			new Preference("global-enable", "off", Type.BOOLEAN, "This globally enables static analysis."
					+ " If this is off, the static analysis module will not be run, though existing compile errors will"
					+ " still be enabled regardless.", 0)
//			new Preference("name", "default", Type.BOOLEAN, "desc"),
		));
		prefs = new Preferences(Implementation.GetServerType().getBranding(),
				Logger.getLogger(StaticAnalysisConfiguration.class.getName()), defaults,
				"This file controls the static analysis options. As this is a major change, we have"
					+ " provided this configuration file to allow disabling the static analysis entirely,"
					+ " or just individual parts, so that in the event you find a bug with your code,"
					+ " you do not have to downgrade. Please note that this is a temporary measure in place,"
					+ " eventually all static analysis will be a mandatory part of code execution, so if you"
					+ " find bugs, it is essential that you report them before disabling this. Future versions"
					+ " of " + Implementation.GetServerType().getBranding() + " will forcibly reset the"
					+ " configuration you have set here, once known bugs are fixed.");
	}

	private void load() throws IOException {
		prefs.init(new File(MethodScriptFileLocations.getDefault().getPreferencesDirectory(),
				"static-analysis-config.ini"));
	}

	public boolean globalEnable() {
		return prefs.getBooleanPreference("global-enable");
	}
}
