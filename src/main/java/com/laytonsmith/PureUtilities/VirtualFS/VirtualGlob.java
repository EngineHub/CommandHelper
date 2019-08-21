package com.laytonsmith.PureUtilities.VirtualFS;

import java.util.regex.Pattern;

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
	private final boolean matchAll;
	private final Pattern globPattern;

	/**
	 * Creates a new virtual glob object, that will match this glob pattern.
	 *
	 * @param glob
	 */
	public VirtualGlob(String glob) {
		this.glob = glob.trim().toLowerCase();
		matchAll = "**".equals(glob);
		globPattern = getPattern(glob);
	}

	/**
	 * Creates a glob that will match only this file.
	 *
	 * @param file
	 */
	public VirtualGlob(VirtualFile file) {
		glob = file.getPath();
		matchAll = false;
		globPattern = getPattern(glob);
	}

	private Pattern getPattern(String glob) {
		if(glob.isEmpty()) {
			throw new IllegalArgumentException("Glob cannot be empty");
		}
		StringBuilder buffer = new StringBuilder();
		StringBuilder pattern = new StringBuilder();
		for(int i = 0; i < glob.length(); i++) {
			char c1 = glob.charAt(i);
			char c2 = '\0';
			if(i < glob.length() - 1) {
				c2 = glob.charAt(i + 1);
			}
			if(c1 == '?' || c1 == '*') {
				if(buffer.length() > 0) {
					pattern.append(Pattern.quote(buffer.toString()));
					buffer = new StringBuilder();
				}
				if(c1 == '?') {
					pattern.append('.');
				} else {
					if(c2 == '*') {
						i++;
						pattern.append(".*");
					} else {
						pattern.append("[^/\\\\]*");
					}
				}
				continue;
			}
			buffer.append(c1);
		}
		if(buffer.length() > 0) {
			pattern.append(Pattern.quote(buffer.toString()));
		}
		return Pattern.compile(pattern.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	}

	/**
	 * Returns true if the specified file matches this glob.
	 * @param file The actual file path to test
	 * @return
	 */
	public boolean matches(VirtualFile file) {
		if(matchAll) {
			// Trivial case
			return true;
		}
		return globPattern.matcher(file.getPath()).matches();
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
