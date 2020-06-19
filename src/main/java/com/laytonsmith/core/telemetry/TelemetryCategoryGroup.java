package com.laytonsmith.core.telemetry;

import com.laytonsmith.PureUtilities.Preferences;

/**
 *
 */
public enum TelemetryCategoryGroup {
	GENERAL_GROUP(new Preferences.GroupData("General").setSortOrder(0)
			.setDescription("These are general settings"
					+ " and don't have a more specific category.")),
	STATIC_ANALYSIS(new Preferences.GroupData("Static Analysis")
			.setDescription("These are settings related to the static"
					+ " analysis system."));
	private final Preferences.GroupData gd;

	private TelemetryCategoryGroup(Preferences.GroupData gd) {
		this.gd = gd;
	}

	/*package*/
	Preferences.GroupData getGroupData() {
		return this.gd;
	}

}
