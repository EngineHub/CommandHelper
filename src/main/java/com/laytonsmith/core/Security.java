package com.laytonsmith.core;

import java.io.File;

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
	 */
	public static boolean CheckSecurity(String location) {
		if(on) {
			String pref = Prefs.BaseDir();
			if(pref.trim().isEmpty()) {
				pref = ".";
			}
			File base_dir = new File(pref);
			String base_final = base_dir.getAbsolutePath();
			if(base_final.endsWith(".")) {
				base_final = base_final.substring(0, base_final.length() - 1);
			}
			File loc = new File(location);
			return loc.getAbsolutePath().startsWith(base_final);
		} else {
			return true;
		}
	}

	/**
	 * Returns true if this file path is accessible via normal script circumstances, false otherwise.
	 *
	 * @param location
	 * @return
	 */
	public static boolean CheckSecurity(File location) {
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
