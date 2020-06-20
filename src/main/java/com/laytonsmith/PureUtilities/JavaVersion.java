package com.laytonsmith.PureUtilities;

/**
 * Utilities for getting the java version
 */
public class JavaVersion {

	/**
	 * Gets the major version of the running JVM, for instance 6 for "1.6" or 8 for "1.8" or 11 for "11".
	 * @return
	 */
	public static int GetMajorVersion() {
		String version = System.getProperty("java.version");
		if(version.startsWith("1.")) {
			version = version.substring(2, 3);
		} else {
			int dot = version.indexOf(".");
			if(dot != -1) {
				version = version.substring(0, dot);
			}
		}
		return Integer.parseInt(version);
	}

	/**
	 * Returns the bit depth of the currently running JVM, NOT the OS bit depth.
	 * @return Currently, either 32 or 64, could be different in future versions.
	 * @throws UnsupportedOperationException If the value returned by the "os.arch" system property was unexpected.
	 * If this happens, a new JVM type has been released, and this function needs to be updated.
	 */
	public static int GetJVMBitDepth() {
		// Note that despite the name, this is not the architecture of the operating system,
		// it is in fact the JVM architecture. On a 32 bit system, this will always be 32 bit,
		// but it's possible to install a 32 bit JVM on a 64 bit system.
		String arch = System.getProperty("os.arch");
		switch(arch) {
			case "x86":
				return 32;
			case "amd64":
			case "x64":
				return 64;
			default:
				throw new UnsupportedOperationException("JVM bit depth could not be determined.");
		}
	}

}
