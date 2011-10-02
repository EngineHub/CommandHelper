/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.aliasengine.Constructs.*;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.Preferences.Type;
import com.laytonsmith.PureUtilities.fileutility.LineCallback;
import com.laytonsmith.PureUtilities.rParser;
import com.laytonsmith.aliasengine.functions.Exceptions.ExceptionType;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.commandhelper.CommandHelperPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import java.nio.channels.FileLockInterruptionException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

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
            throw new ConfigRuntimeException("Expecting a number, but recieved " + c.val() + " instead",
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
            throw new ConfigRuntimeException("Expecting a double, but recieved " + c.val() + " instead",
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
                i = Integer.parseInt(c.val());
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Expecting an integer, but recieved " + c.val() + " instead",
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
        Logger l = com.sk89q.commandhelper.CommandHelperPlugin.logger;
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
    public static Server getServer() throws NotInitializedYetException {
        Server s = com.sk89q.commandhelper.CommandHelperPlugin.myServer;
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
        AliasCore ac = com.sk89q.commandhelper.CommandHelperPlugin.getCore();
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
        SerializedPersistance p = com.sk89q.commandhelper.CommandHelperPlugin.persist;
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
        PermissionsResolverManager prm = com.sk89q.commandhelper.CommandHelperPlugin.perms;
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
        Version v = com.sk89q.commandhelper.CommandHelperPlugin.version;
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
        if (com.sk89q.commandhelper.CommandHelperPlugin.prefs == null) {
            ArrayList<Preferences.Preference> a = new ArrayList<Preferences.Preference>();
            //a.add(new Preference("check-for-updates", "false", Type.BOOLEAN, "Whether or not to check to see if there's an update for CommandHelper"));
            a.add(new Preference("debug-mode", "false", Type.BOOLEAN, "Whether or not to display debug information in the console"));
            a.add(new Preference("show-warnings", "true", Type.BOOLEAN, "Whether or not to display warnings in the console, while compiling"));
            a.add(new Preference("console-log-commands", "true", Type.BOOLEAN, "Whether or not to display the original command in the console when it is run"));
            //a.add(new Preference("max-sleep-time", "5", Type.INT, "The maximum number of seconds a sleep function can sleep for. If <= 0, no limit is imposed. Must be an integer."));
            a.add(new Preference("script-name", "config.txt", Type.STRING, "The path to the config file, relative to the CommandHelper plugin folder"));
            a.add(new Preference("enable-interpreter", "false", Type.BOOLEAN, "Whether or not to enable the /interpreter command. Note that even with this enabled, a player must still have the commandhelper.interpreter permission, but"
                    + " setting it to false prevents all players from accessing the interpreter regardless of their permissions."));
            a.add(new Preference("base-dir", "", Type.STRING, "The base directory that scripts can read and write to. If left blank, then the default of the Bukkit directory will be used. "
                    + "This setting affects functions like include and read."));
            a.add(new Preference("play-dirty", "false", Type.BOOLEAN, "Makes CommandHelper play dirty and break all sorts of programming rules, so that other plugins can't interfere with the operations that you defined. Note that doing this essentially makes CommandHelper have absolute say over commands. Use this setting only if you can't get another plugin to cooperate with CH, because it is a global setting."));
            a.add(new Preference("case-sensitive", "true", Type.BOOLEAN, "Makes command matching be case sensitive. If set to false, if your config defines /cmd, but the user runs /CMD, it will trigger the command anyways."));
            com.sk89q.commandhelper.CommandHelperPlugin.prefs = new Preferences("CommandHelper", getLogger(), a);
        }
        return com.sk89q.commandhelper.CommandHelperPlugin.prefs;
    }

    public static WorldEditPlugin getWorldEditPlugin() {
        if(CommandHelperPlugin.wep == null){
            Plugin pwep = getServer().getPluginManager().getPlugin("WorldEdit");
            if(pwep != null && pwep.isEnabled() && pwep instanceof WorldEditPlugin){
                CommandHelperPlugin.wep = (WorldEditPlugin)pwep;
            }
        }
        return CommandHelperPlugin.wep;
    }
    
    public static WorldGuardPlugin getWorldGuardPlugin() {
        Plugin pwgp = getServer().getPluginManager().getPlugin("WorldGuard");
        if(pwgp != null && pwgp.isEnabled() && pwgp instanceof WorldGuardPlugin){
            return (WorldGuardPlugin) pwgp;
        }
        return null;
    }
    

    public static void checkPlugin(String name, int line_number, File f) throws ConfigRuntimeException {
        if (Bukkit.getServer().getPluginManager().getPlugin(name) == null) {
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
            return Static.resolveConstruct(((Variable)variable).getDefault(), variable.getLineNum(), variable.getFile());
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
    public static void SendMessage(final CommandSender m, String msg, final int line_num, final File f) {
        SendMessage(new LineCallback() {

            public void run(String line) {
                Player p = null;
                if(m instanceof Player){
                    p = (Player)m;
                    if (p == null || !p.isOnline()) {
                        throw new ConfigRuntimeException("The player " + p.getName() + " is not online", ExceptionType.PlayerOfflineException, line_num, f);
                    }
                    p.sendMessage(line);
                } else {
                    if(m != null){
                        m.sendMessage(line);
                    }
                }
            }
        }, msg);
    }

    public static void SendMessage(final CommandSender m, String msg) {
        SendMessage(new LineCallback() {

            public void run(String line) {
                Player p = null;
                if(m instanceof Player){
                    p = (Player)m;
                    if (p != null && p.isOnline()) {
                        p.sendMessage(line);
                    }
                } else {
                    if(m != null){
                        m.sendMessage(line);
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

    public static ItemStack ParseItemNotation(String functionName, String notation, int qty, int line_num, File f) {
        int type = 0;
        byte data = 0;
        ItemStack is = null;
        if (notation.matches("\\d*:\\d*")) {
            String[] sData = notation.split(":");
            try {
                type = (int) Integer.parseInt(sData[0]);
                if(sData.length > 1){
                    data = (byte) Integer.parseInt(sData[1]);
                }
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Item value passed to " + functionName + " is invalid: " + notation, ExceptionType.FormatException, line_num, f);
            }
        } else {
            type = (int) Static.getInt(Static.resolveConstruct(notation, line_num, f));
        }
        
        is = new ItemStack(type, qty);
        is.setDurability(data);
        //is.setData(new MaterialData(type, data));
        return is;
    }
    
    public static Player GetPlayer(String player, int line_num, File f) throws ConfigRuntimeException{
        Player m = Static.getServer().getPlayer(player);
        if(m == null || !m.isOnline()){
            throw new ConfigRuntimeException("The specified player (player) is not online", ExceptionType.PlayerOfflineException, line_num, f);
        }
        return m;
    }
    
    /**
     * Location "objects" are mscript arrays that represent a location in game. There are 
     * 4 usages:
     * <ul>
     * <li>(x, y, z)</li>
     * <li>(world, x, y, z)</li>
     * <li>(x, y, z, yaw, pitch)</li>
     * <li>(world, x, y, z, yaw, pitch)</li>
     * </ul>
     * In all cases, the pitch and yaw default to 0, and the world defaults to the specified world.
     * <em>More conveniently: ([world], x, y, z, [yaw, pitch])</em> 
     * @param c
     * @param w
     * @param line_num
     * @param f
     * @return 
     */
    public static Location GetLocation(Construct c, World w, int line_num, File f){
        if(!(c instanceof CArray)){
            throw new ConfigRuntimeException("Expecting an array, received " + c.getCType(), ExceptionType.FormatException, line_num, f);
        }
        CArray array = (CArray)c;
        World world = w;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        if(array.size() == 3){
            //Just the xyz, with default yaw and pitch, and given world
            x = Static.getNumber(array.get(0, line_num));
            y = Static.getNumber(array.get(1, line_num));
            z = Static.getNumber(array.get(2, line_num));
        } else if(array.size() == 4){
            //world, x, y, z
            world = Static.getServer().getWorld(array.get(0, line_num).val());
            x = Static.getNumber(array.get(1, line_num));
            y = Static.getNumber(array.get(2, line_num));
            z = Static.getNumber(array.get(3, line_num));
        } else if(array.size() == 5){
            //x, y, z, yaw, pitch, with given world
            x = Static.getNumber(array.get(0, line_num));
            y = Static.getNumber(array.get(1, line_num));
            z = Static.getNumber(array.get(2, line_num));
            yaw = (float)Static.getNumber(array.get(3, line_num));
            pitch = (float)Static.getNumber(array.get(4, line_num));
        } else if(array.size() == 6){
            //All have been given
            world = Static.getServer().getWorld(array.get(0, line_num).val());
            x = Static.getNumber(array.get(1, line_num));
            y = Static.getNumber(array.get(2, line_num));
            z = Static.getNumber(array.get(3, line_num));
            yaw = (float)Static.getNumber(array.get(4, line_num));
            pitch = (float)Static.getNumber(array.get(5, line_num));
        } else {
            throw new ConfigRuntimeException("Expecting a Location array, but the array did not meet the format specifications", ExceptionType.FormatException, line_num, f);
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static boolean isNull(Construct construct) {
        return construct instanceof CNull;
    }
    
    public static int Normalize(int i, int min, int max){
        return java.lang.Math.min(max, java.lang.Math.max(min, i));
    }
}
