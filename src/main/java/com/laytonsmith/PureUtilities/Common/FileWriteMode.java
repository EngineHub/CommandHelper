package com.laytonsmith.PureUtilities.Common;

/**
 *
 */
public enum FileWriteMode {
	/**
	 * Replaces any existing file with the new contents (or simply creates a new file if it doesn't already exist).
	 */
	OVERWRITE,
	/**
	 * Appends to the existing file, or if one does not exist, simply creates the file with the new content.
	 */
	APPEND,
	/**
	 * Only writes the file if it doesn't already exist.
	 */
	SAFE_WRITE
}
