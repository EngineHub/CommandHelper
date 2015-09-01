package com.laytonsmith.abstraction.enums;

import com.laytonsmith.PureUtilities.Common.StringUtils;
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
	MC1_2_5,
	MC1_3,
	MC1_3_2,
	MC1_4,
	MC1_4_2,
	MC1_4_5,
	// below this point, Bukkit begins package versioning
	MC1_4_7,
	MC1_5,
	MC1_5_2,
	MC1_6,
	MC1_6_2,
	MC1_6_4,
	MC1_7,
	MC1_7_2,
	MC1_7_10,
	MC1_8,
	MC1_8_3,
	MC1_8_6,
	MC1_8_7,
	MC1_8_X,
	MC1_9,
	MC1_9_X,
	MC1_X,
	MC2_X,
	MCX_X,
	CURRENT,
	FUTURE,
	NEVER;

	public static MCVersion match(String[] source) {
		String[] parts = new String[Math.min(3, source.length)];
		for (int i = 0; i < parts.length; i++) {
			parts[i] = source[i];
		}
		String attempt = "MC" + StringUtils.Join(parts, "_");
		try {
			return valueOf(attempt);
		} catch (IllegalArgumentException iae) {
			if (parts.length == 3) {
				parts[2] = "0".equals(parts[2]) ? null : "X";
				attempt = "MC" + StringUtils.Join(parts[2] == null ? new String[]{parts[0], parts[1]} : parts, "_");
				try {
					return valueOf(attempt);
				} catch (IllegalArgumentException iae2) {
					parts[1] = "X";
					attempt = "MC" + StringUtils.Join(new String[]{parts[0], parts[1]}, "_");
					try {
						return valueOf(attempt);
					} catch (IllegalArgumentException iae3) {
						return MCX_X;
					}
				}
			}
			if (parts.length == 2) {
				parts[1] = "X";
				attempt = "MC" + StringUtils.Join(parts, "_");
				try {
					return valueOf(attempt);
				} catch (IllegalArgumentException iae2) {
					return MCX_X;
				}
			}
			return MCX_X;
		}
	}

	@Override
	public int getMajor() {
		String form = name().split("_")[0].substring(2);
		if ("X".equals(form)) {
			return -1;
		}
		return Integer.valueOf(form);
	}

	@Override
	public int getMinor() {
		String form = name().split("_")[1];
		if ("X".equals(form)) {
			return -1;
		}
		return Integer.valueOf(form);
	}

	@Override
	public int getSupplemental() {
		String[] parts = name().split("_");
		if (parts.length > 2) {
			if ("X".equals(parts[2])) {
				return -1;
			}
			return Integer.valueOf(parts[2]);
		}
		if (getMinor() == -1) {
			return -1;
		}
		return Integer.valueOf(parts[2]);
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
			return !(this.ordinal() > ((MCVersion) other).ordinal());
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
