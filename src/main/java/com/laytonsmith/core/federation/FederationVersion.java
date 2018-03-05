package com.laytonsmith.core.federation;

import com.laytonsmith.PureUtilities.SimpleVersion;
import com.laytonsmith.PureUtilities.Version;

public enum FederationVersion implements Version {
	V1_0_0("1.0.0"),;

	/**
	 * Given the version number, returns the version object.
	 *
	 * @param version
	 * @return
	 * @throws IllegalArgumentException If the version is unrecognized.
	 */
	public static FederationVersion fromVersion(String version) {
		for(FederationVersion f : values()) {
			if(version.equals(f.getVersionString())) {
				return f;
			}
		}
		throw new IllegalArgumentException("Unknown Federation version: " + version + ". This means this"
				+ " server is not aware of that protocol version, and cannot communicate via it.");
	}
	final SimpleVersion version;

	private FederationVersion(String version) {
		this.version = new SimpleVersion(version);
	}

	public String getVersionString() {
		return this.version.toString();
	}

	@Override
	public String toString() {
		return this.version.toString();
	}

	public int compareTo(Version o) {
		return this.version.compareTo(o);
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
