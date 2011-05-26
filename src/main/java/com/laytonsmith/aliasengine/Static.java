/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.Preferences.Type;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.Server;

/**
 * This class contains several static methods to get various objects that really should be static in the first
 * place, but aren't. For the most part, when any code is running, these things will have been initialized, but
 * in the event they aren't, each function will throw a NotInitializedYetException, which is a RuntimeException,
 * so you don't have to check for exceptions whenever you use them. The Exception is caught on a higher
 * level though, so it shouldn't bubble up too far.
 * @author Layton
 */
public class Static {

    public static double getNumber(Construct c) {
        double d;
        if (c == null) {
            return 0.0;
        }
        if (c instanceof CInt) {
            d = ((CInt) c).getInt();
        } else if (c instanceof CDouble) {
            d = ((CDouble) c).getDouble();
        } else if (c instanceof CString) {
            try {
                d = Double.parseDouble(c.val());
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Expecting a number, but received " + c.val() + " instead");
            }
        } else {
            throw new ConfigRuntimeException("Expecting a number, but recieved " + c.val() + " instead");
        }
        return d;
    }

    public static double getDouble(Construct c) {
        try {
            return getNumber(c);
        } catch (ConfigRuntimeException e) {
            throw new ConfigRuntimeException("Expecting a double, but recieved " + c.val() + " instead");
        }
    }

    public static long getInt(Construct c) {
        long i;
        if (c == null) {
            return 0;
        }
        if (c instanceof CInt) {
            i = ((CInt) c).getInt();
        } else {
            throw new ConfigRuntimeException("Expecting an integer, but recieved " + c.val() + " instead");
        }
        return i;
    }

    public static boolean getBoolean(Construct c) {
        boolean b = false;
        if (c == null) {
            return false;
        }
        if (c instanceof CBoolean) {
            b = ((CBoolean) c).getBoolean();
        } else if (c instanceof CString) {
            b = (c.val().length() > 0);
        } else if (c instanceof CInt || c instanceof CDouble) {
            b = (getNumber(c) > 0 || getNumber(c) < 0);
        }
        return b;
    }

    public static boolean anyDoubles(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CDouble) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyStrings(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CString) {
                return true;
            }
        }
        return false;
    }

    public static boolean anyBooleans(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CBoolean) {
                return true;
            }
        }
        return false;
    }

    public static Logger getLogger() throws NotInitializedYetException {
        Logger l = com.sk89q.commandhelper.CommandHelperPlugin.logger;
        if (l == null) {
            throw new NotInitializedYetException("The logger has not been initialized yet");
        }
        return l;
    }

    public static Server getServer() throws NotInitializedYetException {
        Server s = com.sk89q.commandhelper.CommandHelperPlugin.myServer;
        if (s == null) {
            throw new NotInitializedYetException("The server has not been initialized yet");
        }
        return s;
    }

    public static AliasCore getAliasCore() throws NotInitializedYetException {
        AliasCore ac = com.sk89q.commandhelper.CommandHelperPlugin.getCore();
        if (ac == null) {
            throw new NotInitializedYetException("The core has not been initialized yet");
        }
        return ac;
    }

    public static Persistance getPersistance() throws NotInitializedYetException {
        Persistance p = com.sk89q.commandhelper.CommandHelperPlugin.persist;
        if (p == null) {
            throw new NotInitializedYetException("The persistance framework has not been initialized yet");
        }
        return p;
    }

    public static PermissionsResolverManager getPermissionsResolverManager() throws NotInitializedYetException {
        PermissionsResolverManager prm = com.sk89q.commandhelper.CommandHelperPlugin.perms;
        if (prm == null) {
            throw new NotInitializedYetException("The permissions framework has not been initialized yet");
        }
        return prm;
    }

    public static Version getVersion() throws NotInitializedYetException {
        Version v = com.sk89q.commandhelper.CommandHelperPlugin.version;
        if (v == null) {
            throw new NotInitializedYetException("The plugin has not been initialized yet");
        }
        return v;
    }

    public static Preferences getPreferences() throws NotInitializedYetException {
        if (com.sk89q.commandhelper.CommandHelperPlugin.prefs == null) {
            ArrayList<Preferences.Preference> a = new ArrayList<Preferences.Preference>();
            //a.add(new Preference("check-for-updates", "false", Type.BOOLEAN, "Whether or not to check to see if there's an update for CommandHelper"));
            a.add(new Preference("debug-mode", "false", Type.BOOLEAN, "Whether or not to display debug information in the console"));
            a.add(new Preference("show-warnings", "true", Type.BOOLEAN, "Whether or not to display warnings in the console, while compiling"));
            a.add(new Preference("console-log-commands", "true", Type.BOOLEAN, "Whether or not to display the original command in the console when it is run"));
            a.add(new Preference("max-sleep-time", "5", Type.INT, "The maximum number of seconds a sleep function can sleep for. If <= 0, no limit is imposed. Must be an integer."));
            a.add(new Preference("script-name", "config.txt", Type.STRING, "The path to the config file, relative to the CommandHelper plugin folder"));
            com.sk89q.commandhelper.CommandHelperPlugin.prefs = new Preferences("CommandHelper", getLogger(), a);
        }
        return com.sk89q.commandhelper.CommandHelperPlugin.prefs;
    }

    public static Construct resolveConstruct(String val, int line_num) {
        if (val.equalsIgnoreCase("null")) {
            return new CNull(line_num);
        } else if (val.equalsIgnoreCase("true")) {
            return new CBoolean(true, line_num);
        } else if (val.equalsIgnoreCase("false")) {
            return new CBoolean(false, line_num);
        } else {
            try {
                return new CInt(Integer.parseInt(val), line_num);
            } catch (NumberFormatException e) {
                try {
                    return new CDouble(Double.parseDouble(val), line_num);
                } catch (NumberFormatException g) {
                    //It's a literal, but not a keyword. Push it in as a string to standardize everything
                    //later
                    return new CString(val, line_num);
                }
            }
        }
    }
}
