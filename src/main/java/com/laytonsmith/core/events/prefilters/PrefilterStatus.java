package com.laytonsmith.core.events.prefilters;

/**
 *
 */
public enum PrefilterStatus {
	/**
	 * Indicates that a prefilter's use is deprecated, and while it still works, will be removed in a future release.
	 * This generates a compile warning, but still runs as before.
	 */
	DEPRECATED,
	/**
	 * Indicates that a prefilter's use is removed. This generates a compile error. While the prefilter is eligible
	 * for actual removal in code, it is good to keep in as removed for as long as possible, possibly indefinitely,
	 * so code can be upgraded from older versions.
	 */
	REMOVED;
}
