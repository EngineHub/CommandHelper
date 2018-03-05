package com.laytonsmith.commandhelper;

import com.laytonsmith.core.MethodScriptFileLocations;
import java.io.File;

/**
 *
 */
public class CommandHelperFileLocations extends MethodScriptFileLocations {

    private static CommandHelperFileLocations defaultInstance = null;

    public static CommandHelperFileLocations getDefault() {
        if (defaultInstance == null) {
            setDefault(new CommandHelperFileLocations());
        }
        return defaultInstance;
    }

    public static void setDefault(CommandHelperFileLocations provider) {
        defaultInstance = provider;
        MethodScriptFileLocations.setDefault(defaultInstance);
    }

    /**
     * Returns the location of the upgrade log file.
     *
     * @return
     */
    public File getUpgradeLogFile() {
        return new File(getCacheDirectory(), "upgradeLog.json");
    }

}
