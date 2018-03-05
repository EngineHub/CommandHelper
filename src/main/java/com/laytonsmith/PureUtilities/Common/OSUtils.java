package com.laytonsmith.PureUtilities.Common;

/**
 * This class contains utilities that help with OS specific tasks.
 *
 */
public class OSUtils {

	public static enum OS {
		WINDOWS,
		MAC,
		LINUX,
		SOLARIS,
		UNKNOWN
	}

	/**
	 * Returns the OS that is currently running
	 *
	 * @return
	 */
	public static OS GetOS() {
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("win")) {
			return OS.WINDOWS;
		} else if(os.contains("mac")) {
			return OS.MAC;
		} else if(os.contains("nix") || os.contains("nux")) {
			return OS.LINUX;
		} else if(os.contains("sunos")) {
			return OS.SOLARIS;
		} else {
			return OS.UNKNOWN;
		}
	}

	public static String GetLineEnding() {
		return System.getProperty("line.separator");
	}
}
