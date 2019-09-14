package com.laytonsmith.PureUtilities;

import static com.laytonsmith.PureUtilities.SimpleVersion.checkGT;
import static com.laytonsmith.PureUtilities.SimpleVersion.checkLT;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An implementation of the Version interface that conforms precisely to the
 * <a href="https://semver.org/spec/v2.0.0.html">Semantic Versioning 2.0.0</a> specification. While this is
 * only a small part of what makes SemVer useful, properly representing the format is part of it. For more details,
 * particularly about what should cause version changes, and how the version number should change over time,
 * see the full spec.
 */
public class SemVer2 implements Version, Comparable<SemVer2> {
	private final int major;
	private final int minor;
	private final int patch;
	private final String prerelease;
	private final String buildMetaData;

	private static final Pattern PATTERN
			= Pattern.compile("(0|[1-9]\\d*)"
					+ "\\.(0|[1-9]\\d*)"
					+ "\\.(0|[1-9]\\d*)"
					+ "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)"
						+ "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
					+ "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

	/**
	 * Creates a new Semantic Versioning object from a string version number. The prerelease and buildMetaData
	 * is optional, but all other parameters are required.
	 *
	 * @param version The version, as a string
	 * @throws IllegalArgumentException If the input string is invalid. An invalid input string is one which
	 * does not conform to the Semantic Versioning 2.0.0 standard.
	 */
	public SemVer2(String version) throws IllegalArgumentException {
		Matcher m = PATTERN.matcher(version);
		if(m.find()) {
			try {
				major = Integer.parseInt(m.group(1));
				minor = Integer.parseInt(m.group(2));
				patch = Integer.parseInt(m.group(3));
				prerelease = m.group(4) == null ? "" : m.group(4);
				buildMetaData = m.group(5) == null ? "" : m.group(5);
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
	 * @param major MAJOR version when you make incompatible API changes
	 * @param minor MINOR version when you add functionality in a backwards compatible manner
	 * @param patch PATCH version when you make backwards compatible bug fixes
	 * @param prerelease Identifiers MUST comprise only ASCII alphanumerics and hyphen [0-9A-Za-z-].
	 * Identifiers MUST NOT be empty. Numeric identifiers MUST NOT include leading zeroes. Pre-release versions have
	 * a lower precedence than the associated normal version. A pre-release version indicates that the version is
	 * unstable and might not satisfy the intended compatibility requirements as denoted by its associated normal
	 * version.
	 */
	public SemVer2(int major, int minor, int patch, String prerelease) {
		this(major, minor, patch, prerelease, "");
	}

	/**
	 * Creates a new version with programmatic parameters.
	 *
	 * @param major MAJOR version when you make incompatible API changes
	 * @param minor MINOR version when you add functionality in a backwards compatible manner
	 * @param patch PATCH version when you make backwards compatible bug fixes
	 * @param prerelease Identifiers MUST comprise only ASCII alphanumerics and hyphen [0-9A-Za-z-].
	 * Identifiers MUST NOT be empty. Numeric identifiers MUST NOT include leading zeroes. Pre-release versions have
	 * a lower precedence than the associated normal version. A pre-release version indicates that the version is
	 * unstable and might not satisfy the intended compatibility requirements as denoted by its associated normal
	 * version.
	 * @param buildMetaData Identifiers MUST comprise only ASCII alphanumerics and hyphen [0-9A-Za-z-]. Identifiers
	 * MUST NOT be empty. Build metadata MUST be ignored when determining version precedence. Thus two versions that
	 * differ only in the build metadata, have the same precedence.
	 */
	public SemVer2(int major, int minor, int patch, String prerelease, String buildMetaData) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.prerelease = prerelease;
		this.buildMetaData = buildMetaData;
	}

	/**
	 * Creates a new version with programmatic parameters, and an empty prerelease and buildMetaData values.
	 *
	 * @param major MAJOR version when you make incompatible API changes
	 * @param minor MINOR version when you add functionality in a backwards compatible manner
	 * @param patch PATCH version when you make backwards compatible bug fixes
	 */
	public SemVer2(int major, int minor, int patch) {
		this(major, minor, patch, "", "");
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
	 * Returns the patch version. This is merely implemented to conform to the {@link Version} interface, prefer
	 * {@link #getPatch()}.
	 *
	 * @return
	 */
	@Override
	public int getSupplemental() {
		return patch;
	}

	/**
	 * Returns the patch version.
	 * @return
	 */
	public int getPatch() {
		return patch;
	}

	/**
	 * Returns the prerelease label in this version.
	 *
	 * @return
	 */
	public String getPrerelease() {
		return prerelease;
	}

	/**
	 * Returns the build meta data label in this version.
	 * @return
	 */
	public String getBuildMetaData() {
		return buildMetaData;
	}

	@Override
	public String toString() {
		String s = major + "." + minor + "." + patch;
		if(!"".equals(prerelease)) {
			s += "-" + prerelease;
		}
		if(!"".equals(buildMetaData)) {
			s += "+" + buildMetaData;
		}
		return s;
	}

	private int compareIdentifiers(String a, String b) {
		boolean anum = false;
		try {
			Long.parseLong(a);
			anum = true;
		} catch (NumberFormatException e) {
			//
		}
		boolean bnum = false;
		try {
			Long.parseLong(b);
			bnum = true;
		} catch (NumberFormatException e) {
			//
		}

		Integer r = a.equals(b) ? 0
			: (anum && !bnum) ? -1
			: (bnum && !anum) ? 1
			: Integer.MAX_VALUE;
		if(r != Integer.MAX_VALUE) {
			return r;
		}
		// Need to do a comparison, but the comparison varies if this is a string or not.
		if(anum && bnum) {
			return Long.valueOf(a).compareTo(Long.valueOf(b));
		} else {
			return a.compareTo(b);
		}
	}

	private int comparePre(SemVer2 other) {
		String[] thisParts = this.prerelease.split("\\.", 0);
		if(this.prerelease.equals("")) {
			thisParts = new String[0];
		}
		String[] thatParts = other.prerelease.split("\\.", 0);
		if(other.prerelease.equals("")) {
			thatParts = new String[0];
		}
		// NOT having a prerelease is > having one
		if(thisParts.length > 0 && thatParts.length == 0) {
			return -1;
		} else if(thisParts.length == 0 && thatParts.length > 0) {
			return 1;
		} else if(thisParts.length == 0 && thatParts.length == 0) {
			return 0;
		}

		int i = 0;

		do {
			if(thisParts.length <= i && thatParts.length <= i) {
				return 0;
			} else if(thatParts.length <= i) {
				return 1;
			} else if(thisParts.length <= i) {
				return -1;
			}
			String a = thisParts[i];
			String b = thatParts[i];
			if(!a.equals(b)) {
				return compareIdentifiers(a, b);
			}
			++i;
		} while(true);
	}

	/**
	 * {@inheritDoc}
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(SemVer2 o) {
		{
			int[] thisParts = new int[]{major, minor, patch};
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
		}
		// They're the same version, but the prerelease version needs to be considered also. (buildMetaData is
		// completely ignored for precedence purposes). The rules for priority of
		// this is as follows:
		/*
		Precedence for two pre-release versions with the same major, minor, and patch version MUST be determined by
		comparing each dot separated identifier from left to right until a difference is found as follows: identifiers
		consisting of only digits are compared numerically and identifiers with letters or hyphens are compared
		lexically in ASCII sort order. Numeric identifiers always have lower precedence than non-numeric identifiers.
		A larger set of pre-release fields has a higher precedence than a smaller set, if all of the preceding
		identifiers are equal.
		Example: 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11
		< 1.0.0-rc.1 < 1.0.0.
		*/
		return comparePre(o);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SemVer2) {
			SemVer2 v = (SemVer2) obj;
			if(major == v.getMajor() && minor == v.getMinor() && patch == v.getSupplemental() && comparePre(v) == 0) {
				return true;
			} else {
				return false;
			}
		}
		if(obj instanceof Version) {
			Version v = (Version) obj;
			if(major == v.getMajor() && minor == v.getMinor() && patch == v.getSupplemental()) {
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
		hash = 97 * hash + this.patch;
		return hash;
	}

	@Override
	public boolean lt(Version other) {
		if(other instanceof SemVer2) {
			return compareTo((SemVer2) other) < 0;
		}
		return checkLT(this, other);
	}

	@Override
	public boolean lte(Version other) {
		if(other instanceof SemVer2) {
			if(this.compareTo((SemVer2) other) == 0) {
				return true;
			}
		} else if(this.equals(other)) {
			return true;
		}
		return lt(other);
	}

	@Override
	public boolean gt(Version other) {
		if(other instanceof SemVer2) {
			return compareTo((SemVer2) other) > 0;
		}
		return checkGT(this, other);
	}

	@Override
	public boolean gte(Version other) {
		if(other instanceof SemVer2) {
			if(this.compareTo((SemVer2) other) == 0) {
				return true;
			}
		} else if(this.equals(other)) {
			return true;
		}
		return gt(other);
	}
}
