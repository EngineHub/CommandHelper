package com.laytonsmith.core;

import java.io.File;

/**
 *
 * @author layton
 */
public class Security {
    /**
     * Returns true if this filepath is accessible to CH, false otherwise.
     * @param location
     * @return 
     */
    public static boolean CheckSecurity(String location) {
        String pref = Prefs.BaseDir();
        if (pref.trim().equals("")) {
            pref = ".";
        }
        File base_dir = new File(pref);
        String base_final = base_dir.getAbsolutePath();
        if (base_final.endsWith(".")) {
            base_final = base_final.substring(0, base_final.length() - 1);
        }
        File loc = new File(location);
        return loc.getAbsolutePath().startsWith(base_final);
    }
}
