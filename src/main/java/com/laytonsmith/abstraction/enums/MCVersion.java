package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.Version;

/**
 * Created by jb_aero on 3/17/2015.
 * <p/>
 * This may require unforeseen changes later to support fossil versions, but odds are support will not be requested.
 */
public enum MCVersion implements Version {
	/**
	 * Baseline, highly unlikely anyone is running something before this
	 */
	MC1_0,
	MC1_1,
	MC1_2,
	MC1_2_5(MC1_2),
	MC1_3,
	MC1_3_2(MC1_3),
	MC1_4,
	MC1_4_2(MC1_4),
	MC1_4_5(MC1_4),
	// below this point, Bukkit begins package versioning
	MC1_4_7(MC1_4),
	MC1_5,
	MC1_5_2(MC1_5),
	MC1_6,
	MC1_6_2(MC1_6),
	MC1_6_4(MC1_6),
	MC1_7,
	MC1_7_2(MC1_7),
	MC1_7_10(MC1_7),
	MC1_8,
	MC1_8_3(MC1_8),
	MC1_8_6(MC1_8),
	MC1_9;

	private MCVersion majorVersion;

	private MCVersion() {
		this(null);
	}

	private MCVersion(MCVersion majorVersion) {
		this.majorVersion = majorVersion;
	}

	public MCVersion getMajorVersion() {
		return majorVersion == null ? this : majorVersion;
	}

	@Override
	public int getMajor() {
		return Integer.valueOf(name().split("_")[0].substring(2));
	}

	@Override
	public int getMinor() {
		return Integer.valueOf(name().split("_")[1]);
	}

	@Override
	public int getSupplemental() {
		String[] parts = name().split("_");
		return parts.length < 3 ? 0 : Integer.valueOf(parts[2]);
	}

	@Override
	public boolean lt(Version other) {
		if (other instanceof MCVersion) {
			return this.ordinal() < ((MCVersion) other).ordinal();
		}
		return false;
	}

	@Override
	public boolean lte(Version other) {
		if (other instanceof MCVersion) {
			return !(this.ordinal() < ((MCVersion) other).ordinal());
		}
		return false;
	}

	@Override
	public boolean gt(Version other) {
		if (other instanceof MCVersion) {
			return this.ordinal() > ((MCVersion) other).ordinal();
		}
		return false;
	}

	@Override
	public boolean gte(Version other) {
		if (other instanceof MCVersion) {
			return !(this.ordinal() < ((MCVersion) other).ordinal());
		}
		return false;
	}
}
