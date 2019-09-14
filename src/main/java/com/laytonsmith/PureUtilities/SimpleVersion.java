package com.laytonsmith.PureUtilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A version is formatted as such: 1.2.10 beta-1 where 1 is the major version, 2 is the minor version, 10 is the
 * supplemental version, and beta-1 is the tag. When comparing two versions, the tag is not considered.
 *
 * Generally speaking, this shouldn't be used in favor of {@link SemVer2}, but it is not deprecated, since third
 * party version numbers may not conform to the SemVer2 standard, which is quite strict. However, new code should
 * use SemVer2 where possible.
 */
public class SimpleVersion implements Version {

	private int major;
	private int minor;
	private int supplemental;
	private String tag;

	private static final Pattern PATTERN = Pattern.compile("(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:\\s+(.*))?");

	/**
	 * Creates a new SimpleVersion object from a string version number. The tag is optional, but all other parameters
	 * are required. If left off, each version part is set to 0.
	 *
	 * @param version The version, as a string
	 */
	public SimpleVersion(String version) {
		Matcher m = PATTERN.matcher(version);
		if(m.find()) {
			try {
				major = Integer.parseInt(m.group(1) == null ? "0" : m.group(1));
				minor = Integer.parseInt(m.group(2) == null ? "0" : m.group(2));
				supplemental = Integer.parseInt(m.group(3) == null ? "0" : m.group(3));
				tag = m.group(4) == null ? "" : m.group(4);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Version numbers must be integers", e);
			}
		} else {
			throw new IllegalArgumentException("Invalid version string provided");
		}
	}

	/**
	 * Creates a new version with programmatic parameters.
	 *
	 * @param major
	 * @param minor
	 * @param supplemental
	 * @param tag
	 */
	public SimpleVersion(int major, int minor, int supplemental, String tag) {
		this.major = major;
		this.minor = minor;
		this.supplemental = supplemental;
		this.tag = tag;
	}

	/**
	 * Creates a new version with programmatic parameters, and an emtpy tag.
	 *
	 * @param major
	 * @param minor
	 * @param supplemental
	 */
	public SimpleVersion(int major, int minor, int supplemental) {
		this(major, minor, supplemental, "");
	}

	/**
	 * Returns the major version.
	 *
	 * @return
	 */
	@Override
	public int getMajor() {
		return major;
	}

	/**
	 * Returns the minor version.
	 *
	 * @return
	 */
	@Override
	public int getMinor() {
		return minor;
	}

	/**
	 * Returns the supplemental version.
	 *
	 * @return
	 */
	@Override
	public int getSupplemental() {
		return supplemental;
	}

	/**
	 * Returns the tag in this version.
	 *
	 * @return
	 */
	public String getTag() {
		return tag;
	}

	@Override
	public String toString() {
		return (major + "." + minor + "." + supplemental + " " + tag).trim();
	}

	public int compareTo(Version o) {
		int[] thisParts = new int[]{major, minor, supplemental};
		int[] otherParts = new int[]{o.getMajor(), o.getMinor(), o.getSupplemental()};
		for(int i = 0; i < thisParts.length; i++) {
			int n1 = thisParts[i];
			int n2 = otherParts[i];
			if(n1 < n2) {
				return -1;
			}
			if(n1 > n2) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Version) {
			Version v = (Version) obj;
			if(major == v.getMajor() && minor == v.getMinor() && supplemental == v.getSupplemental()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + this.major;
		hash = 97 * hash + this.minor;
		hash = 97 * hash + this.supplemental;
		return hash;
	}

	@Override
	public boolean lt(Version other) {
		return checkLT(this, other);
	}

	@Override
	public boolean lte(Version other) {
		return checkLTE(this, other);
	}

	@Override
	public boolean gt(Version other) {
		return checkGT(this, other);
	}

	@Override
	public boolean gte(Version other) {
		return checkGTE(this, other);
	}

	public static boolean checkLT(Version lhs, Version rhs) {
		if(lhs == null || rhs == null) {
			throw new NullPointerException();
		}
		if(lhs.getMajor() == rhs.getMajor()) {
			if(lhs.getMinor() == rhs.getMinor()) {
				if(lhs.getSupplemental() == rhs.getSupplemental()) {
					return false;
				} else if(lhs.getSupplemental() < rhs.getSupplemental()) {
					return true;
				}
			} else if(lhs.getMinor() < rhs.getMinor()) {
				return true;
			}
		} else if(lhs.getMajor() < rhs.getMajor()) {
			return true;
		}
		return false;
	}

	public static boolean checkLTE(Version lhs, Version rhs) {
		if(lhs == null || rhs == null) {
			throw new NullPointerException();
		}
		if(lhs.equals(rhs)) {
			return true;
		}
		return checkLT(lhs, rhs);
	}

	public static boolean checkGT(Version lhs, Version rhs) {
		if(lhs == null || rhs == null) {
			throw new NullPointerException();
		}
		if(lhs.getMajor() == rhs.getMajor()) {
			if(lhs.getMinor() == rhs.getMinor()) {
				if(lhs.getSupplemental() == rhs.getSupplemental()) {
					return false;
				} else if(lhs.getSupplemental() > rhs.getSupplemental()) {
					return true;
				}
			} else if(lhs.getMinor() > rhs.getMinor()) {
				return true;
			}
		} else if(lhs.getMajor() > rhs.getMajor()) {
			return true;
		}
		return false;
	}

	public static boolean checkGTE(Version lhs, Version rhs) {
		if(lhs == null || rhs == null) {
			throw new NullPointerException();
		}
		if(lhs.equals(rhs)) {
			return true;
		}
		return checkGT(lhs, rhs);
	}
}
