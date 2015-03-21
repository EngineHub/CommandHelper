package com.laytonsmith.abstraction.enums;

/**
 * Created by jb_aero on 3/17/2015.
 * <p/>
 * This may require unforeseen changes later to support fossil versions, but odds are support will not be requested.
 */
public enum MCVersion {
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
}
