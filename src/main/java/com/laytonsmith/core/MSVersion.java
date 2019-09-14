package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SemVer2;
import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;

/**
 *
 *
 */
public enum MSVersion implements Version {
	V0_0_0("0.0.0"), //Unreleased version
	V3_0_1("3.0.1"),
	V3_0_2("3.0.2"),
	V3_1_0("3.1.0"),
	V3_1_2("3.1.2"),
	V3_1_3("3.1.3"),
	V3_2_0("3.2.0"),
	V3_3_0("3.3.0"),
	V3_3_1("3.3.1"),
	V3_3_2("3.3.2"),
	V3_3_3("3.3.3"),
	V3_3_4("3.3.4");
	final SemVer2 version;

	/**
	 * This points to the latest version in the series. This should normally only be used for things that report the
	 * <i>current</i> version, not things that are versioned. This is not an actual enum within the class, this is a
	 * static member of the class which points to the an actual enum.
	 */
	public static final MSVersion LATEST;

	static {
		//Dynamically determine the latest value.
		MSVersion latest = null;
		for(MSVersion v : MSVersion.values()) {
			if(latest == null || v.gt(latest)) {
				latest = v;
			}
		}
		LATEST = latest;
	}

	MSVersion(String version) {
		this.version = new SemVer2(version);
	}

	public String getVersionString() {
		return this.version.toString();
	}

	@Override
	public String toString() {
		return this.version.toString();
	}

	public int compareTo(Version o) {
		if(o instanceof SemVer2) {
			return this.version.compareTo((SemVer2) o);
		} else {
			return new SimpleVersion(this.version.toString()).compareTo(o);
		}
	}

	@Override
	public int getMajor() {
		return this.version.getMajor();
	}

	@Override
	public int getMinor() {
		return this.version.getMinor();
	}

	@Override
	public int getSupplemental() {
		return this.version.getSupplemental();
	}

	public int getPatch() {
		return this.version.getPatch();
	}

	public String getPrerelease() {
		return this.version.getPrerelease();
	}

	public String getBuildMetaData() {
		return this.version.getBuildMetaData();
	}

	@Override
	public boolean lt(Version other) {
		return this.version.lt(other);
	}

	@Override
	public boolean lte(Version other) {
		return this.version.lte(other);
	}

	@Override
	public boolean gt(Version other) {
		return this.version.gt(other);
	}

	@Override
	public boolean gte(Version other) {
		return this.version.gte(other);
	}

}
