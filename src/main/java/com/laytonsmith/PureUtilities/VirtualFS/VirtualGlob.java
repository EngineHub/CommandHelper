package com.laytonsmith.PureUtilities.VirtualFS;

/**
 * A VirtualGlob is a simple wrapper around a file matching glob. The following rules are applied: All characters are
 * taken literally, except for the asterisk, double asterisk, or question mark, which mean the following:
 * <ul>
 * <li>* - Match any number of characters, except directory separators (forward slash)</li>
 * <li>** - Match any number of characters, including directory separators (forward slash)</li>
 * <li>? - Match exactly one character, except directory separators (forward slash)</li>
 * </ul>
 *
 * The essential operations of a glob simply ask if a particular VirtualFile actually match this glob or not.
 *
 *
 */
public class VirtualGlob implements Comparable<VirtualGlob> {

	private final String glob;

	/**
	 * Creates a new virtual glob object, that will match this glob pattern.
	 *
	 * @param glob
	 */
	public VirtualGlob(String glob) {
		this.glob = glob.trim().toLowerCase();
	}

	/**
	 * Creates a glob that will match only this file.
	 *
	 * @param file
	 */
	public VirtualGlob(VirtualFile file) {
		glob = file.getPath();
	}

	/**
	 * Returns true if the specified file matches this glob.
	 * @param file The actual file path to test
	 * @return
	 */
	public boolean matches(VirtualFile file) {
		if("**".equals(glob)) {
			// Trivial case
			return true;
		}
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	/**
	 * Compares two globs, to see which one is more specific than the other.
	 * Returns 0 if they are the same weight, -1 if the passed in glob is more specific, and 1 if the passed
	 * in glob is less specific (TODO or maybe this should be reversed)
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(VirtualGlob o) {
		// TODO: This is wrong, we need to do a more specific comparison where we take into account the wildcards
		return glob.length() - o.glob.length();
	}

	@Override
	public String toString() {
		return glob;
	}

}
