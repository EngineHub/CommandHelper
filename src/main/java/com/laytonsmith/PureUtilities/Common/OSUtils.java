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
		UNKNOWN;

		/**
		 * Returns true if this is {@link #WINDOWS}
		 * @return
		 */
		public boolean isWindows() {
			return this == WINDOWS;
		}

		/**
		 * Returns true if this is {@link #MAC}
		 * @return
		 */
		public boolean isMac() {
			return this == MAC;
		}

		/**
		 * Returns true if this is {@link #LINUX}
		 * @return
		 */
		public boolean isLinux() {
			return this == LINUX;
		}

		/**
		 * Returns true if this is {@link #SOLARIS}
		 * @return
		 */
		public boolean isSolaris() {
			return this == SOLARIS;
		}

		/**
		 * Returns true if this is {@link #UNKNOWN}
		 * @return
		 */
		public boolean isUnknown() {
			return this == UNKNOWN;
		}

		/**
		 * Returns true if this is a strict UNIX implementation, that is, {@link #MAC} or {@link #SOLARIS}
		 *
		 * @return
		 */
		public boolean isUnix() {
			return this == MAC || this == SOLARIS;
		}

		/**
		 * Returns true if this {@link #isUnix()} returns true, or if this is {@link #LINUX}. This is a less strict
		 * category than {@link #isUnix()}, because for most purposes, Linux is UNIX compatible, but this is not
		 * strictly the case. Depending on what you're doing, you may need to differentiate between strict UNIX OSes
		 * or not, so if this matters, you'll need to do a more granular check.
		 * @return
		 */
		public boolean isUnixLike() {
			return isUnix() || this == LINUX;
		}
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
