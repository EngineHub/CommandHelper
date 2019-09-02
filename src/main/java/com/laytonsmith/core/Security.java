package com.laytonsmith.core;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public final class Security {

	private Security() {
	}

	private static boolean on = true;

	/**
	 * Returns true if this file path is accessible via normal script circumstances, false otherwise.
	 *
	 * @param location
	 * @return
	 * @throws java.io.IOException
	 */
	public static boolean CheckSecurity(String location) throws IOException {
		if(on) {
			// If the location is within any of the paths, it is allowed.
			for(String pref : Prefs.BaseDir().split(";", -1)) {
				if(pref.trim().isEmpty()) {
					pref = ".";
				}
				File baseDir = new File(pref);
				String baseFinal = baseDir.getCanonicalPath();
				if(baseFinal.endsWith(".")) {
					baseFinal = baseFinal.substring(0, baseFinal.length() - 1);
				}
				File loc = new File(location);
				if(loc.getCanonicalPath().startsWith(baseFinal)) {
					return true;
				}
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns true if this file path is accessible via normal script circumstances, false otherwise.
	 *
	 * @param location
	 * @return
	 * @throws java.io.IOException
	 */
	public static boolean CheckSecurity(File location) throws IOException {
		return CheckSecurity(location.getAbsolutePath());
	}

	/**
	 * Turns security on or off. In general, this shouldn't be off, though for certain meta operations (including
	 * cmdline usage or testing) this is acceptable.
	 *
	 * @param security
	 */
	public static void setSecurityEnabled(boolean security) {
		on = security;
	}
}
