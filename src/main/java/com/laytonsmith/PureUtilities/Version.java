package com.laytonsmith.PureUtilities;

/**
 * An interface that Version backings must implement. Versions may be implemented as an enum, instead of using strings
 * in code, and so this must be a separate interface. See {@link SimpleVersion} for a string based implementation.
 */
public interface Version {

	/**
	 * Returns true if the major, minor, and supplemental versions are all equal.
	 *
	 * @param obj
	 * @return
	 */
	@Override
	boolean equals(Object obj);

	/**
	 * Returns the major version.
	 *
	 * @return
	 */
	int getMajor();

	/**
	 * Returns the minor version.
	 *
	 * @return
	 */
	int getMinor();

	/**
	 * Returns the supplemental version, aka, the patch version.
	 *
	 * @return
	 */
	int getSupplemental();

	/**
	 * Returns a toString representation of this Version. Usually dots are used to separate the major, minor, and
	 * supplemental versions.
	 *
	 * @return
	 */
	@Override
	String toString();

	/**
	 * Returns true if this version is less than the other version.
	 *
	 * @param other
	 * @return
	 */
	boolean lt(Version other);

	/**
	 * Returns true if this version is less than or equal to the other version.
	 *
	 * @param other
	 * @return
	 */
	boolean lte(Version other);

	/**
	 * Returns true if this version is greater than the other version.
	 *
	 * @param other
	 * @return
	 */
	boolean gt(Version other);

	/**
	 * Returns true if this version is greater than or equal to the other version.
	 *
	 * @param other
	 * @return
	 */
	boolean gte(Version other);

}
