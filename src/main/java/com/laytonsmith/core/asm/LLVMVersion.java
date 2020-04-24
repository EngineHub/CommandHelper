package com.laytonsmith.core.asm;

import com.laytonsmith.PureUtilities.SemVer2;
import com.laytonsmith.PureUtilities.Version;

/**
 *
 */
public enum LLVMVersion implements Version {
	V0_0_0(0, 0, 0),
	V0_0_1(0, 0, 1);

	private final SemVer2 version;

	private LLVMVersion(int major, int minor, int supplemental) {
		this.version = new SemVer2(major, minor, supplemental);
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
		return this.version.getPatch();
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
