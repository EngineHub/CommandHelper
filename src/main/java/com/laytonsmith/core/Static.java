/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.Preferences.Type;
import com.laytonsmith.PureUtilities.fileutility.LineCallback;
import com.laytonsmith.PureUtilities.rParser;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlugin;
import com.laytonsmith.core.functions.Debug;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.logging.Level;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;


/**
 * This class contains several static methods to get various objects that really should be static in the first
 * place, but aren't. For the most part, when any code is running, these things will have been initialized, but
 * in the event they aren't, each function will throw a NotInitializedYetException, which is a RuntimeException,
 * so you don't have to check for exceptions whenever you use them. The Exception is caught on a higher
 * level though, so it shouldn't bubble up too far.
 * @author Layton
 */
public class Static {

    /**
     * This function pulls a numerical equivalent from any given construct. It throws a ConfigRuntimeException
     * if it cannot be converted, for instance the string "s" cannot be cast to a number. The number returned
     * will always be a double.
     * @param c
     * @return 
     */
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
                throw new ConfigRuntimeException("Expecting a number, but received " + c.val() + " instead",
                        ExceptionType.CastException, c.getLineNum(), c.getFile());
            }
        } else {
            throw new ConfigRuntimeException("Expecting a number, but received " + c.val() + " instead",
                    ExceptionType.CastException, c.getLineNum(), c.getFile());
        }
        return d;
    }

    /**
     * Alias to getNumber
     * @param c
     * @return 
     */
    public static double getDouble(Construct c) {
        try {
            return getNumber(c);
        } catch (ConfigRuntimeException e) {
            throw new ConfigRuntimeException("Expecting a double, but received " + c.val() + " instead",
                    ExceptionType.CastException, c.getLineNum(), c.getFile());
        }
    }

    /**
     * Returns an integer from any given construct. If the number is not castable to an int, a ConfigRuntimeException
     * is thrown.
     * @param c
     * @return 
     */
    public static long getInt(Construct c) {
        long i;
        if (c == null) {
            return 0;
        }
        if (c instanceof CInt) {
            i = ((CInt) c).getInt();
        } else {
            try {
                i = Long.parseLong(c.val());
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Expecting an integer, but received " + c.val() + " instead",
                        ExceptionType.CastException, c.getLineNum(), c.getFile());
            }
        }
        return i;
    }

    /**
     * Returns a boolean from any given construct. Depending on the type of the construct being converted, it follows the following rules:
     * If it is an integer or a double, it is false if 0, true otherwise. If it is a string, if it is empty, it is false, otherwise it is true.
     * @param c
     * @return 
     */
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

    /**
     * Returns true if any of the constructs are a CDouble, false otherwise.
     * @param c
     * @return 
     */
    public static boolean anyDoubles(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CDouble) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if any of the constructs are CStrings, false otherwise.
     * @param c
     * @return 
     */
    public static boolean anyStrings(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CString) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any of the constructs are CBooleans, false otherwise.
     * @param c
     * @return 
     */
    public static boolean anyBooleans(Construct... c) {
        for (int i = 0; i < c.length; i++) {
            if (c[i] instanceof CBoolean) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the logger for the plugin
     * @return
     * @throws NotInitializedYetException 
     */
    public static Logger getLogger() throws NotInitializedYetException {
        Logger l = com.laytonsmith.commandhelper.CommandHelperPlugin.logger;
        if (l == null) {
            throw new NotInitializedYetException("The logger has not been initialized yet");
        }
        return l;
    }

    /**
     * Returns the server for this plugin
     * @return
     * @throws NotInitializedYetException 
     */
    public static MCServer getServer() throws NotInitializedYetException {
        MCServer s = com.laytonsmith.commandhelper.CommandHelperPlugin.myServer;
        if (s == null) {
            throw new NotInitializedYetException("The server has not been initialized yet");
        }
        return s;
    }

    /**
     * Gets the reference to the AliasCore for this plugin
     * @return
     * @throws NotInitializedYetException 
     */
    public static AliasCore getAliasCore() throws NotInitializedYetException {
        AliasCore ac = com.laytonsmith.commandhelper.CommandHelperPlugin.getCore();
        if (ac == null) {
            throw new NotInitializedYetException("The core has not been initialized yet");
        }
        return ac;
    }

    /**
     * Gets the persistance object for this plugin
     * @return
     * @throws NotInitializedYetException 
     */
    public static SerializedPersistance getPersistance() throws NotInitializedYetException {
        SerializedPersistance p = com.laytonsmith.commandhelper.CommandHelperPlugin.persist;
        if (p == null) {
            throw new NotInitializedYetException("The persistance framework has not been initialized yet");
        }
        return p;
    }

    /**
     * Gets the permissions resolver manager this plugin uses
     * @return
     * @throws NotInitializedYetException 
     */
    public static PermissionsResolverManager getPermissionsResolverManager() throws NotInitializedYetException {
        PermissionsResolverManager prm = com.laytonsmith.commandhelper.CommandHelperPlugin.perms;
        if (prm == null) {
            throw new NotInitializedYetException("The permissions framework has not been initialized yet");
        }
        return prm;
    }

    /**
     * Gets the current version of the plugin
     * @return
     * @throws NotInitializedYetException 
     */
    public static Version getVersion() throws NotInitializedYetException {
        Version v = com.laytonsmith.commandhelper.CommandHelperPlugin.version;
        if (v == null) {
            throw new NotInitializedYetException("The plugin has not been initialized yet");
        }
        return v;
    }

    /**
     * Gets the preferences object for this plugin, as well as setting it up if
     * it is not already activated.
     * @return
     * @throws NotInitializedYetException 
     */
    public static Preferences getPreferences() throws NotInitializedYetException {
        if (com.laytonsmith.commandhelper.CommandHelperPlugin.prefs == null) {
            ArrayList<Preferences.Preference> a = new ArrayList<Preferences.Preference>();
            //a.add(new Preference("check-for-updates", "false", Type.BOOLEAN, "Whether or not to check to see if there's an update for CommandHelper"));
            a.add(new Preference("debug-mode", "false", Type.BOOLEAN, "Whether or not to display debug information in the console"));
            a.add(new Preference("show-warnings", "true", Type.BOOLEAN, "Whether or not to display warnings in the console, while compiling"));
            a.add(new Preference("console-log-commands", "true", Type.BOOLEAN, "Whether or not to display the original command in the console when it is run"));
            //a.add(new Preference("max-sleep-time", "5", Type.INT, "The maximum number of seconds a sleep function can sleep for. If <= 0, no limit is imposed. Must be an integer."));
            a.add(new Preference("script-name", "config.txt", Type.STRING, "The path to the config file, relative to the CommandHelper plugin folder"));
            a.add(new Preference("enable-interpreter", "false", Type.BOOLEAN, "Whether or not to enable the /interpreter command. Note that even with this enabled, a player must still have the commandhelper.interpreter permission, but"
                    + " setting it to false prevents all players from accessing the interpreter regardless of their permissions."));
            a.add(new Preference("base-dir", "", Type.STRING, "The base directory that scripts can read and write to. If left blank, then the default of the server directory will be used. "
                    + "This setting affects functions like include and read."));
            a.add(new Preference("play-dirty", "false", Type.BOOLEAN, "Makes CommandHelper play dirty and break all sorts of programming rules, so that other plugins can't interfere with the operations that you defined. Note that doing this essentially makes CommandHelper have absolute say over commands. Use this setting only if you can't get another plugin to cooperate with CH, because it is a global setting."));
            a.add(new Preference("case-sensitive", "true", Type.BOOLEAN, "Makes command matching be case sensitive. If set to false, if your config defines /cmd, but the user runs /CMD, it will trigger the command anyways."));
            a.add(new Preference("main-file", "main.ms", Type.STRING, "The path to the main file, relative to the CommandHelper folder"));
            a.add(new Preference("allow-debug-logging", "false", Type.BOOLEAN, "If set to false, the Debug class of functions will do nothing."));
            a.add(new Preference("debug-log-file", "logs/debug/%Y-%M-%D-debug.log", Type.STRING, "The path to the debug output log file. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are for whatever reason leaving logging on, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction."));
            a.add(new Preference("standard-log-file", "logs/%Y-%M-%D-commandhelper.log", Type.STRING, "The path the standard log files that the log() function writes to. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are actively logging things, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction."));
            a.add(new Preference("allow-profiling", "false", Type.BOOLEAN, "If set to false, the Profiling class of functions will do nothing."));
            a.add(new Preference("profiling-file", "logs/profiling/%Y-%M-%D-profiling.log", Type.STRING, "The path to the profiling logs. These logs are perf4j formatted logs. Consult the documentation for more information."));
            a.add(new Preference("show-splash-screen", "true", Type.BOOLEAN, "Whether or not to show the splash screen at server startup"));
            com.laytonsmith.commandhelper.CommandHelperPlugin.prefs = new Preferences("CommandHelper", getLogger(), a);
        }
        return com.laytonsmith.commandhelper.CommandHelperPlugin.prefs;
    }
    private static String debugLogFileCurrent = null;
    private static FileWriter debugLogFileHandle = null;

    /**
     * Returns a file that is most likely ready to write to. The timestamp variables have already been replaced, and parent directories
     * are all created.
     * @return 
     */
    public static FileWriter debugLogFile() throws IOException {
        String currentFileName = "plugins" + File.separator + "CommandHelper" + File.separator + ParseCalendarNotation((String) getPreferences().getPreference("debug-log-file"));
        if (!currentFileName.equals(debugLogFileCurrent)) {
            if (debugLogFileHandle != null) {
                //We're done with the old one, close it.
                debugLogFileHandle.close();
            }
            debugLogFileCurrent = currentFileName;
            new File(debugLogFileCurrent).getParentFile().mkdirs();
            debugLogFileHandle = new FileWriter(currentFileName);
        }
        return debugLogFileHandle;
    }
    private static String standardLogFileCurrent = null;
    private static FileWriter standardLogFileHandle = null;

    public static FileWriter standardLogFile() throws IOException {
        String currentFileName = "plugins" + File.separator + "CommandHelper" + File.separator + ParseCalendarNotation((String) getPreferences().getPreference("standard-log-file"));
        if (!currentFileName.equals(standardLogFileCurrent)) {
            if (standardLogFileHandle != null) {
                //We're done with the old one, close it.
                standardLogFileHandle.close();
            }
            standardLogFileCurrent = currentFileName;
            new File(standardLogFileCurrent).getParentFile().mkdirs();
            standardLogFileHandle = new FileWriter(currentFileName);
        }
        return standardLogFileHandle;
    }
    private static String profilingLogFileCurrent = null;
    private static FileWriter profilingLogFileHandle = null;

    public static FileWriter profilingLogFile() throws IOException {
        String currentFileName = "plugins" + File.separator + "CommandHelper" + File.separator + ParseCalendarNotation((String) getPreferences().getPreference("profiling-file"));
        if (!currentFileName.equals(profilingLogFileCurrent)) {
            if (profilingLogFileHandle != null) {
                //We're done with the old one, close it.
                profilingLogFileHandle.close();
            }
            profilingLogFileCurrent = currentFileName;
            new File(profilingLogFileCurrent).getParentFile().mkdirs();
            profilingLogFileHandle = new FileWriter(currentFileName);
        }
        return profilingLogFileHandle;
    }

    /**
     * Convenience notation for ParseCalendarNotation(name, null)
     */
    public static String ParseCalendarNotation(String name) {
        return ParseCalendarNotation(name, null);
    }

    /**
     * Parses a calendar notation. The following patterns are replaced with the following:
     * <table>
     * <tr><td>%Y</td><td>Year</td></tr>
     * <tr><td>%M</td><td>Month</td></tr>
     * <tr><td>%D</td><td>Day</td></tr>
     * <tr><td>%h</td><td>Hour</td></tr>
     * <tr><td>%m</td><td>Minute</td></tr>
     * <tr><td>%s</td><td>Second</td></tr>
     * </table>
     * 
     * A generally standard format for human readable logs is: %Y-%M-%D %h:%m.%s
     * @param name
     * @param c
     * @return 
     */
    public static String ParseCalendarNotation(String name, Calendar c) {
        if (c == null) {
            c = Calendar.getInstance();
        }
        String year = String.format("%04d", c.get(Calendar.YEAR));
        String month = String.format("%02d", 1 + c.get(Calendar.MONTH)); //January is 0
        String day = String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
        String hour = String.format("%02d", c.get(Calendar.HOUR));
        String minute = String.format("%02d", c.get(Calendar.MINUTE));
        String second = String.format("%02d", c.get(Calendar.SECOND));
        return name.replaceAll("%Y", year).replaceAll("%M", month)
                .replaceAll("%D", day).replaceAll("%h", hour)
                .replaceAll("%m", minute).replaceAll("%s", second);
    }

    public static WorldEditPlugin getWorldEditPlugin(int line_num, File file) {
        if (Implementation.GetServerType() != Implementation.Type.BUKKIT) {
            throw new ConfigRuntimeException("Trying to use WorldEdit on non-bukkit server.", ExceptionType.InvalidPluginException, line_num, file);
        }
        if (CommandHelperPlugin.wep == null) {
            MCPlugin pwep = getServer().getPluginManager().getPlugin("WorldEdit");
            if (pwep != null && pwep.isEnabled() && pwep.isInstanceOf(WorldEditPlugin.class) && pwep instanceof BukkitMCPlugin) {
                CommandHelperPlugin.wep = (WorldEditPlugin) ((BukkitMCPlugin)pwep).getPlugin();
            }
        }
        return CommandHelperPlugin.wep;
    }

    public static WorldGuardPlugin getWorldGuardPlugin(int line_num, File file) {
        if (Implementation.GetServerType() != Implementation.Type.BUKKIT) {
            throw new ConfigRuntimeException("Trying to use WorldGuard on non-bukkit server.", ExceptionType.InvalidPluginException, line_num, file);
        }
        MCPlugin pwgp = getServer().getPluginManager().getPlugin("WorldGuard");
        if (pwgp != null && pwgp.isEnabled() && pwgp.isInstanceOf(WorldGuardPlugin.class) && pwgp instanceof BukkitMCPlugin) {
            return (WorldGuardPlugin) ((BukkitMCPlugin)pwgp).getPlugin();
        }
        return null;
    }

    public static void checkPlugin(String name, int line_number, File f) throws ConfigRuntimeException {
        if (Static.getServer().getPluginManager().getPlugin(name) == null) {
            throw new ConfigRuntimeException("Needed plugin " + name + " not found!",
                    ExceptionType.InvalidPluginException, line_number, f);
        }
    }

    /**
     * Given a string input, creates and returns a Construct of the appropriate
     * type. This takes into account that null, true, and false are keywords.
     * @param val
     * @param line_num
     * @return 
     */
    public static Construct resolveConstruct(String val, int line_num, File file) {
        if (val == null) {
            return new CString("", line_num, file);
        }
        if (val.equalsIgnoreCase("null")) {
            return new CNull(line_num, file);
        } else if (val.equalsIgnoreCase("true")) {
            return new CBoolean(true, line_num, file);
        } else if (val.equalsIgnoreCase("false")) {
            return new CBoolean(false, line_num, file);
        } else {
            try {
                return new CInt(Integer.parseInt(val), line_num, file);
            } catch (NumberFormatException e) {
                try {
                    if (val.contains(" ") || val.contains("\t")) {
                        //Interesting behavior in Double.parseDouble causes it to "trim" strings first, then
                        //try to parse them, which is not desireable in our case. So, if the value contains
                        //any characters other than [\-0-9\.], we want to make it a string instead
                        return new CString(val, line_num, file);
                    }
                    return new CDouble(Double.parseDouble(val), line_num, file);
                } catch (NumberFormatException g) {
                    //It's a literal, but not a keyword. Push it in as a string to standardize everything
                    //later
                    return new CString(val, line_num, file);
                }
            }
        }
    }

    public static Construct resolveDollarVar(Construct variable, List<Variable> vars) {
        if (variable.getCType() == Construct.ConstructType.VARIABLE) {
            for (Variable var : vars) {
                if (var.getName().equals(((Variable) variable).getName())) {
                    return Static.resolveConstruct(var.val(), var.getLineNum(), var.getFile());
                }
            }
            return Static.resolveConstruct(((Variable) variable).getDefault(), variable.getLineNum(), variable.getFile());
        } else {
            return variable;
        }
    }

    /**
     * This function breaks a string into chunks based on Minecraft line length,
     * and newlines, then calls the LineCallback with each line.
     * @param c
     * @param msg 
     */
    public static void SendMessage(LineCallback c, String msg) {
        String[] newlines = msg.split("\n");
        for (String line : newlines) {
            String[] arr = rParser.wordWrap(line);
            for (String toMsg : arr) {                
                c.run(toMsg.trim());
            }
        }

    }

    /**
     * This function sends a message to the player. It is useful to use this function because:
     * It handles newlines and wordwrapping for you.
     * @param p
     * @param msg 
     */
    public static void SendMessage(final MCCommandSender m, String msg, final int line_num, final File f) {
        SendMessage(new LineCallback() {

            public void run(String line) {
                MCPlayer p = null;
                if (m instanceof MCPlayer) {
                    p = (MCPlayer) m;
                    if (p == null || !p.isOnline()) {
                        throw new ConfigRuntimeException("The player " + p.getName() + " is not online", ExceptionType.PlayerOfflineException, line_num, f);
                    }
                    p.sendMessage(line);
                } else {
                    if (m != null) {
                        m.sendMessage(line);
                    } else {
                        System.out.println(line);
                    }
                }
            }
        }, msg);
    }

    public static void SendMessage(final MCCommandSender m, String msg) {
        SendMessage(new LineCallback() {

            public void run(String line) {
                MCPlayer p = null;
                if (m instanceof MCPlayer) {
                    p = (MCPlayer) m;
                    if (p != null && p.isOnline()) {
                        p.sendMessage(line);
                    }
                } else {
                    if (m != null) {
                        m.sendMessage(line);
                    } else {
                        System.out.println(line);
                    }
                }
            }
        }, msg);
    }

    /**
     * Returns true if this filepath is accessible to CH, false otherwise.
     * @param location
     * @return 
     */
    public static boolean CheckSecurity(String location) {
        String pref = (String) Static.getPreferences().getPreference("base-dir");
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

    /**
     * Returns whether or not this location appears to be a url.
     */
    public static boolean ApparentURL(String toCheck) {
        return false;
    }

    public static MCItemStack ParseItemNotation(String functionName, String notation, int qty, int line_num, File f) {
        int type = 0;
        byte data = 0;
        MCItemStack is = null;
        if (notation.matches("\\d*:\\d*")) {
            String[] sData = notation.split(":");
            try {
                type = (int) Integer.parseInt(sData[0]);
                if (sData.length > 1) {
                    data = (byte) Integer.parseInt(sData[1]);
                }
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Item value passed to " + functionName + " is invalid: " + notation, ExceptionType.FormatException, line_num, f);
            }
        } else {
            type = (int) Static.getInt(Static.resolveConstruct(notation, line_num, f));
        }

        is = StaticLayer.GetItemStack(type, qty);
        is.setDurability(data);
        //is.setData(new MaterialData(type, data));
        return is;
    }

    /**
     * Works in reverse from the other ParseItemNotation
     * @param is
     * @return 
     */
    public static String ParseItemNotation(MCItemStack is) {
        if (is == null) {
            return "0";
        }
        String append = null;
        if (is.getData() != null) {
            append = Byte.toString(is.getData().getData());
        } else if (is.getDurability() != 0) {
            append = Short.toString(is.getDurability());
        }
        return is.getTypeId() + (append == null ? "" : ":" + append);
    }

    public static String ParseItemNotation(MCBlock b) {
        if (b == null || b.isNull()) {
            return "0";
        }
        return b.getTypeId() + (b.getData() == 0 ? "" : ":" + Byte.toString(b.getData()));
    }

    public static MCPlayer GetPlayer(String player, int line_num, File f) throws ConfigRuntimeException {
        MCPlayer m = Static.getServer().getPlayer(player);
        if (m == null || !m.isOnline()) {
            throw new ConfigRuntimeException("The specified player (player) is not online", ExceptionType.PlayerOfflineException, line_num, f);
        }
        return m;
    }

    public static MCPlayer GetPlayer(Construct player, int line_num, File f) throws ConfigRuntimeException {
        return GetPlayer(player.val(), line_num, f);
    }

    public static MCPlayer GetPlayer(String player) {
        return GetPlayer(player, 0, null);
    }

    public static MCPlayer GetPlayer(Construct player) {
        return GetPlayer(player, 0, null);
    }

    /**
     * Location "objects" are mscript arrays that represent a location in game. There are 
     * 4 usages:
     * <ul>
     * <li>(x, y, z)</li>
     * <li>(x, y, z, world)</li>
     * <li>(x, y, z, yaw, pitch)</li>
     * <li>(x, y, z, world, yaw, pitch)</li>
     * </ul>
     * In all cases, the pitch and yaw default to 0, and the world defaults to the specified world.
     * <em>More conveniently: ([world], x, y, z, [yaw, pitch])</em> 
     * @param c
     * @param w
     * @param line_num
     * @param f
     * @return 
     */
    public static MCLocation GetLocation(Construct c, MCWorld w, int line_num, File f) {
        if (!(c instanceof CArray)) {
            throw new ConfigRuntimeException("Expecting an array, received " + c.getCType(), ExceptionType.FormatException, line_num, f);
        }
        CArray array = (CArray) c;
        MCWorld world = w;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        if (array.size() == 3) {
            //Just the xyz, with default yaw and pitch, and given world
            x = Static.getNumber(array.get(0, line_num));
            y = Static.getNumber(array.get(1, line_num));
            z = Static.getNumber(array.get(2, line_num));
        } else if (array.size() == 4) {
            //x, y, z, world
            x = Static.getNumber(array.get(0, line_num));
            y = Static.getNumber(array.get(1, line_num));
            z = Static.getNumber(array.get(2, line_num));
            world = Static.getServer().getWorld(array.get(3, line_num).val());
        } else if (array.size() == 5) {
            //x, y, z, yaw, pitch, with given world
            x = Static.getNumber(array.get(0, line_num));
            y = Static.getNumber(array.get(1, line_num));
            z = Static.getNumber(array.get(2, line_num));
            yaw = (float) Static.getNumber(array.get(3, line_num));
            pitch = (float) Static.getNumber(array.get(4, line_num));
        } else if (array.size() == 6) {
            //All have been given
            x = Static.getNumber(array.get(0, line_num));
            y = Static.getNumber(array.get(1, line_num));
            z = Static.getNumber(array.get(2, line_num));
            world = Static.getServer().getWorld(array.get(3, line_num).val());
            yaw = (float) Static.getNumber(array.get(4, line_num));
            pitch = (float) Static.getNumber(array.get(5, line_num));
        } else {
            throw new ConfigRuntimeException("Expecting a Location array, but the array did not meet the format specifications", ExceptionType.FormatException, line_num, f);
        }
        return StaticLayer.GetLocation(world, x, y, z, yaw, pitch);
    }

    /**
     * Works the opposite of GetLocation
     * @param l
     * @return 
     */
    public static CArray GetLocationArray(MCLocation l) {
        CArray ca = new CArray(0, null);
        ca.push(new CDouble(l.getX(), 0, null));
        ca.push(new CDouble(l.getY(), 0, null));
        ca.push(new CDouble(l.getZ(), 0, null));
        ca.push(new CString(l.getWorld().getName(), 0, null));
        ca.push(new CDouble(l.getYaw(), 0, null));
        ca.push(new CDouble(l.getPitch(), 0, null));
        return ca;
    }

    public static boolean isNull(Construct construct) {
        return construct instanceof CNull;
    }

    public static int Normalize(int i, int min, int max) {
        return java.lang.Math.min(max, java.lang.Math.max(min, i));
    }

    /**
     * Returns the specified id, or null if it doesn't exist.
     * @param id
     * @return 
     */
    public static MCEntity getEntity(int id) {
        for (MCWorld w : Static.getServer().getWorlds()) {
            for (MCLivingEntity e : w.getLivingEntities()) {
                if (e.getEntityId() == id) {
                    return e;
                }
            }
        }
        return null;
    }

    public static String strJoin(Collection c, String inner) {
        StringBuilder b = new StringBuilder();
        Object[] o = c.toArray();
        for (int i = 0; i < o.length; i++) {
            if (i != 0) {
                b.append(inner);
            }
            b.append(o[i]);
        }
        return b.toString();
    }

    public static String strJoin(Object[] o, String inner) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < o.length; i++) {
            if (i != 0) {
                b.append(inner);
            }
            b.append(o[i]);
        }
        return b.toString();
    }

    public static String LF() {
        return System.getProperty("line.separator");
    }

    public static synchronized void LogDebug(String message) throws IOException {
        if (Debug.LOG_TO_SCREEN) {
            Static.getLogger().log(Level.INFO, message);
        }
        String timestamp = Static.ParseCalendarNotation("%Y-%M-%D %h:%m.%s - ");
        QuickAppend(Static.debugLogFile(), timestamp + message + Static.LF());
    }

    public static void QuickAppend(FileWriter f, String message) throws IOException {
        f.append(message);
        f.flush();
    }

    /**
     * Sets up CommandHelper to play-dirty, if the user has specified as such
     */
    public static void PlayDirty() {
        if ((Boolean) Static.getPreferences().getPreference("play-dirty")) {
            try {
                //Set up our "proxy"
                BukkitDirtyRegisteredListener.Repopulate();
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(Static.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
                AliasCore.logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            } catch (ClassCastException ex) {
                AliasCore.logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            } catch (IllegalArgumentException ex) {
                AliasCore.logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            } catch (IllegalAccessException ex) {
                AliasCore.logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            }
        } //else play nice :(
    }

    public static boolean hasCHPermission(String functionName, Env env) {
        //The * label completely overrides everything
        if("*".equals(env.GetLabel())){
            return true;
        }
        boolean perm = false;
        PermissionsResolverManager perms = Static.getPermissionsResolverManager();
        if (perms != null) {
            if (env.GetCommandSender() instanceof MCPlayer) {
                perm = perms.hasPermission(env.GetPlayer().getName(), "ch.func.use." + functionName)
                        || perms.hasPermission(env.GetPlayer().getName(), "commandhelper.func.use." + functionName);
                if (env.GetLabel() != null && env.GetLabel().startsWith("~")) {
                    String[] groups = env.GetLabel().substring(1).split("/");
                    for (String group : groups) {
                        if (perms.inGroup(env.GetPlayer().getName(), group)) {
                            perm = true;
                            break;
                        }
                    }
                } else {
                    if (env.GetLabel() != null){
                        if(env.GetLabel().contains(".")){
                            //We are using a non-standard permission. Don't automatically
                            //add CH's prefix
                            if(perms.hasPermission(env.GetPlayer().getName(), env.GetLabel())){
                                perm = true;
                            }
                        } else if((perms.hasPermission(env.GetPlayer().getName(), "ch.alias." + env.GetLabel()))
                            || perms.hasPermission(env.GetPlayer().getName(), "commandhelper.alias." + env.GetLabel())) {
                            perm = true;
                        }
                    }
                }
            } else if (env.GetCommandSender() instanceof MCConsoleCommandSender) {
                perm = true;
            }
        } else {
            perm = true;
        }
        if (env.GetLabel() != null && env.GetLabel().equals("*")) {
            perm = true;
        }
        if (env.GetCommandSender() == null
                || env.GetCommandSender().isOp()) {
            perm = true;
        }
        return perm;
    }
    
    public static String Logo(){
        String logo = Installer.parseISToString(Static.class.getResourceAsStream("/mainlogo"));
        AnsiConsole.systemInstall();
        logo = logo.replaceAll(" ", Ansi.ansi().bg(Ansi.Color.BLACK).a(" ").toString());
        logo = logo.replaceAll("_", Ansi.ansi().bg(Ansi.Color.RED).fg(Ansi.Color.RED).a("_").toString());
        logo = logo.replaceAll("/", Ansi.ansi().bg(Ansi.Color.WHITE).fg(Ansi.Color.WHITE).a("/").toString());
        String s = logo + Ansi.ansi().a(Ansi.Attribute.RESET);
        AnsiConsole.systemUninstall();
        return s;
    }
    
    public static String DataManagerLogo(){
        String logo = Installer.parseISToString(Static.class.getResourceAsStream("/datamanagerlogo"));
        AnsiConsole.systemInstall();
        logo = logo.replaceAll(" ", Ansi.ansi().bg(Ansi.Color.BLACK).a(" ").toString());
        logo = logo.replaceAll("_", Ansi.ansi().bg(Ansi.Color.CYAN).fg(Ansi.Color.CYAN).a("_").toString());
        logo = logo.replaceAll("/", Ansi.ansi().bg(Ansi.Color.WHITE).fg(Ansi.Color.WHITE).a("/").toString());
        String s = logo + Ansi.ansi().a(Ansi.Attribute.RESET);
        AnsiConsole.systemUninstall();
        return s;
    }
    
    public static String GetStringResource(String name){
        return GetStringResource(Static.class, name);
    }
    
    public static String GetStringResource(Class path, String name){
        return Installer.parseISToString(path.getResourceAsStream(name));
    }
}
