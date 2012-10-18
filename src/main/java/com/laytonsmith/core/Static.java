

package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.*;
import com.laytonsmith.abstraction.*;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlugin;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.constructs.*;
import com.laytonsmith.core.environments.CommandHelperEnvironment;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Debug;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import com.laytonsmith.core.profiler.Profiler;
import com.laytonsmith.persistance.DataSourceException;
import com.laytonsmith.persistance.PersistanceNetwork;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class contains several static methods to get various objects that really should be static in the first
 * place, but aren't. For the most part, when any code is running, these things will have been initialized, but
 * in the event they aren't, each function will throw a NotInitializedYetException, which is a RuntimeException,
 * so you don't have to check for exceptions whenever you use them. The Exception is caught on a higher
 * level though, so it shouldn't bubble up too far.
 * @author Layton
 */
public final class Static {

    private Static(){}
    
    private static final Logger logger = Logger.getLogger("CommandHelper");
    
    private static Map<String, String> hostCache = new HashMap<String, String>();
	
	public static CArray getArray(Construct construct, Target t) {
		if(construct instanceof CArray){
			return ((CArray)construct);
		} else {
			throw new ConfigRuntimeException("Expecting array, but received " + construct.val(), ExceptionType.CastException, t);
		}
	}
	
	public static <T extends Construct> T getObject(Construct construct, Target t, String expectedClassName, Class<T> clazz){
		if(clazz.isAssignableFrom(construct.getClass())){
			return (T)construct;
		} else {
			throw new ConfigRuntimeException("Expecting " + expectedClassName + " but receieved " + construct.val() + " instead.", ExceptionType.CastException, t);
		}
	}

    /**
     * This function pulls a numerical equivalent from any given construct. It throws a ConfigRuntimeException
     * if it cannot be converted, for instance the string "s" cannot be cast to a number. The number returned
     * will always be a double.
     * @param c
     * @return 
     */
    public static double getNumber(Construct c) {
        double d;
        if (c == null || c instanceof CNull) {
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
                throw new ConfigRuntimeException("Expecting a number, but received \"" + c.val() + "\" instead",
                        ExceptionType.CastException, c.getTarget());
            }
        } else if(c instanceof CBoolean){
            if(((CBoolean)c).getBoolean()){
                d = 1;
            } else {
                d = 0;
            }
        } else {
            throw new ConfigRuntimeException("Expecting a number, but received \"" + c.val() + "\" instead",
                    ExceptionType.CastException, c.getTarget());
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
                    ExceptionType.CastException, c.getTarget());
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
        if (c == null || c instanceof CNull) {
            return 0;
        }
        if (c instanceof CInt) {
            i = ((CInt) c).getInt();
        } else if(c instanceof CBoolean){
            if(((CBoolean)c).getBoolean()){
                i = 1;
            } else {
                i = 0;
            }
        } else {
            try {
                i = Long.parseLong(c.val());
            } catch (NumberFormatException e) {
                throw new ConfigRuntimeException("Expecting an integer, but received " + c.val() + " instead",
                        ExceptionType.CastException, c.getTarget());
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
            b = !(getNumber(c) == 0);
        } else if(c instanceof CArray){
            b = ((CArray)c).size() != 0;
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
     */
    public static Logger getLogger() {
        return logger;
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

    private static String debugLogFileCurrent = null;
    private static FileWriter debugLogFileHandle = null;

    /**
     * Returns a file that is most likely ready to write to. The timestamp variables have already been replaced, and parent directories
     * are all created.
     * @return 
     */
    public static FileWriter debugLogFile(File root) throws IOException {
        String currentFileName = root.getPath() + DateUtil.ParseCalendarNotation(Prefs.DebugLogFile());
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

    public static FileWriter standardLogFile(File root) throws IOException {
        String currentFileName = root.getPath() + DateUtil.ParseCalendarNotation(Prefs.StandardLogFile());
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

    public static FileWriter profilingLogFile(File root) throws IOException {
        String currentFileName = root.getPath() + DateUtil.ParseCalendarNotation(Prefs.ProfilingFile());
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

    

    public static WorldEditPlugin getWorldEditPlugin(Target t) {
        if (Implementation.GetServerType() != Implementation.Type.BUKKIT) {
            throw new ConfigRuntimeException("Trying to use WorldEdit on non-bukkit server.", ExceptionType.InvalidPluginException, t);
        }
        if (CommandHelperPlugin.wep == null) {
            MCPlugin pwep = getServer().getPluginManager().getPlugin("WorldEdit");
            if (pwep != null && pwep.isEnabled() && pwep.isInstanceOf(WorldEditPlugin.class) && pwep instanceof BukkitMCPlugin) {
                CommandHelperPlugin.wep = (WorldEditPlugin) ((BukkitMCPlugin)pwep).getPlugin();
            }
        }
        return CommandHelperPlugin.wep;
    }

    public static WorldGuardPlugin getWorldGuardPlugin(Target t) {
        if (Implementation.GetServerType() != Implementation.Type.BUKKIT) {
            throw new ConfigRuntimeException("Trying to use WorldGuard on non-bukkit server.", ExceptionType.InvalidPluginException, t);
        }
        MCPlugin pwgp = getServer().getPluginManager().getPlugin("WorldGuard");
        if (pwgp != null && pwgp.isEnabled() && pwgp.isInstanceOf(WorldGuardPlugin.class) && pwgp instanceof BukkitMCPlugin) {
            return (WorldGuardPlugin) ((BukkitMCPlugin)pwgp).getPlugin();
        }
        return null;
    }

    public static void checkPlugin(String name, Target t) throws ConfigRuntimeException {
        if (Static.getServer().getPluginManager().getPlugin(name) == null) {
            throw new ConfigRuntimeException("Needed plugin " + name + " not found!",
                    ExceptionType.InvalidPluginException, t);
        }
    }

    /**
     * Given a string input, creates and returns a Construct of the appropriate
     * type. This takes into account that null, true, and false are keywords.
     * @param val
     * @param line_num
     * @return 
     */
    public static Construct resolveConstruct(String val, Target t) {
        if (val == null) {
            return new CString("", t);
        }
        if (val.equalsIgnoreCase("null")) {
            return new CNull(t);
        } else if (val.equalsIgnoreCase("true")) {
            return new CBoolean(true, t);
        } else if (val.equalsIgnoreCase("false")) {
            return new CBoolean(false, t);
        } else {
            try {
                return new CInt(Long.parseLong(val), t);
            } catch (NumberFormatException e) {
                try {
                    if (val.contains(" ") || val.contains("\t")) {
                        //Interesting behavior in Double.parseDouble causes it to "trim" strings first, then
                        //try to parse them, which is not desireable in our case. So, if the value contains
                        //any characters other than [\-0-9\.], we want to make it a string instead
                        return new CString(val, t);
                    }
                    return new CDouble(Double.parseDouble(val), t);
                } catch (NumberFormatException g) {
                    //It's a literal, but not a keyword. Push it in as a string to standardize everything
                    //later
                    return new CString(val, t);
                }
            }
        }
    }

    public static Construct resolveDollarVar(Construct variable, List<Variable> vars) {
        if(variable == null){
            return new CNull();
        }
        if (variable.getCType() == Construct.ConstructType.VARIABLE) {
            for (Variable var : vars) {
                if (var.getName().equals(((Variable) variable).getName())) {
                    return new CString(var.val(), var.getTarget());
                }
            }
            return new CString(((Variable) variable).getDefault(), variable.getTarget());
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
                //Trim just the right
                for(int i = toMsg.length() - 1; i >= 0; i--){
                    if(!Character.isWhitespace(toMsg.charAt(i))){
                        toMsg = toMsg.substring(0, i + 1);
                        break;
                    }
                }
                c.run(toMsg);
            }
        }

    }

    /**
     * This function sends a message to the player. It is useful to use this function because:
     * It handles newlines and wordwrapping for you.
     * @param p
     * @param msg 
     */
    public static void SendMessage(final MCCommandSender m, String msg, final Target t) {
        SendMessage(new LineCallback() {

            public void run(String line) {
                if (m instanceof MCPlayer) {
                    MCPlayer p = (MCPlayer) m;
                    if (p == null) {
                        throw new ConfigRuntimeException("The player " + p.getName() + " is not online", ExceptionType.PlayerOfflineException, t);
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
     * Returns whether or not this location appears to be a url.
     */
    public static boolean ApparentURL(String toCheck) {
        return false;
    }

    /**
     * Returns an item stack from the given item notation. Defaulting to the specified qty, this
     * will throw an exception if the notation is invalid.
     * @param functionName
     * @param notation
     * @param qty
     * @param line_num
     * @param f
     * @throws ConfigRuntimeException FormatException if the notation is invalid.
     * @return 
     */
    public static MCItemStack ParseItemNotation(String functionName, String notation, int qty, Target t) {
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
                throw new ConfigRuntimeException("Item value passed to " + functionName + " is invalid: " + notation, ExceptionType.FormatException, t);
            }
        } else {
            type = (int) Static.getInt(Static.resolveConstruct(notation, t));
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
            append = Integer.toString(is.getData().getData());
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

    private static Map<String, MCPlayer> injectedPlayers = new HashMap<String, MCPlayer>();
    public static MCPlayer GetPlayer(String player, Target t) throws ConfigRuntimeException {        
        MCPlayer m = Static.getServer().getPlayer(player);
        if(injectedPlayers.containsKey(player)){
            m = injectedPlayers.get(player);
        }
        if (m == null || (!m.isOnline() && !injectedPlayers.containsKey(player))) {
            throw new ConfigRuntimeException("The specified player (" + player + ") is not online", ExceptionType.PlayerOfflineException, t);
        }
        return m;
    }

    public static MCPlayer GetPlayer(Construct player, Target t) throws ConfigRuntimeException {
        return GetPlayer(player.val(), t);
    }

    public static MCPlayer GetPlayer(Construct player) {
        return GetPlayer(player, player.getTarget());
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
                    return StaticLayer.GetCorrectEntity(e);
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

	/**
	 * Returns the system based line seperator character
	 * @return 
	 */
    public static String LF() {
        return System.getProperty("line.separator");
    }

    public static synchronized void LogDebug(File root, String message) throws IOException {
        if (Debug.LOG_TO_SCREEN) {
            Static.getLogger().log(Level.INFO, message);
        }
        String timestamp = DateUtil.ParseCalendarNotation("%Y-%M-%D %h:%m.%s - ");
        QuickAppend(Static.debugLogFile(root), timestamp + message + Static.LF());
    }

    public static void QuickAppend(FileWriter f, String message) throws IOException {
        f.append(message);
        f.flush();
    }

    /**
     * Sets up CommandHelper to play-dirty, if the user has specified as such
     */
    public static void PlayDirty() {
        if (Prefs.PlayDirty()) {
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

    public static boolean hasCHPermission(String functionName, Environment env) {
        //The * label completely overrides everything
        if("*".equals(env.getEnv(CommandHelperEnvironment.class).GetLabel())){
            return true;
        }
		MCPlayer player = env.getEnv(CommandHelperEnvironment.class).GetPlayer();
		MCCommandSender commandSender = env.getEnv(CommandHelperEnvironment.class).GetCommandSender();
		String label = env.getEnv(CommandHelperEnvironment.class).GetLabel();
        boolean perm = false;
        PermissionsResolver perms = env.getEnv(GlobalEnv.class).GetPermissionsResolver();
        if (perms != null) {
            if (commandSender instanceof MCPlayer) {
                perm = perms.hasPermission(player.getName(), "ch.func.use." + functionName, player.getWorld().getName())
                        || perms.hasPermission(player.getName(), "commandhelper.func.use." + functionName, player.getWorld().getName());
                if (label != null && label.startsWith("~")) {
                    String[] groups = env.getEnv(CommandHelperEnvironment.class).GetLabel().substring(1).split("/");
                    for (String group : groups) {
                        if (perms.inGroup(env.getEnv(CommandHelperEnvironment.class).GetPlayer().getName(), group)) {
                            perm = true;
                            break;
                        }
                    }
                } else {
                    if (env.getEnv(CommandHelperEnvironment.class).GetLabel() != null){
                        if(env.getEnv(CommandHelperEnvironment.class).GetLabel().contains(".")){
                            //We are using a non-standard permission. Don't automatically
                            //add CH's prefix
                            if(perms.hasPermission(player.getName(), label, player.getWorld().getName())){
                                perm = true;
                            }
                        } else if((perms.hasPermission(player.getName(), "ch.alias." + label, player.getWorld().getName()))
                            || perms.hasPermission(player.getName(), "commandhelper.alias." + label, player.getWorld().getName())) {
                            perm = true;
                        }
                    }
                }
            } else if (commandSender instanceof MCConsoleCommandSender) {
                perm = true;
            }
        } else {
            perm = true;
        }
        if (label != null && label.equals("*")) {
            perm = true;
        }
        if (commandSender == null
                || commandSender.isOp()) {
            perm = true;
        }
        return perm;
    }
    
    public static String Logo(){
        String logo = Installer.parseISToString(Static.class.getResourceAsStream("/mainlogo"));
        logo = logo.replaceAll("( +)", TermColors.BG_BLACK + "$1");
        logo = logo.replaceAll("_", TermColors.BG_RED + TermColors.RED + "_");
        logo = logo.replaceAll("/", TermColors.BG_BRIGHT_WHITE + TermColors.WHITE + "/");
        String s = logo + TermColors.reset();
        return s;
    }
    
    public static String DataManagerLogo(){
        String logo = Installer.parseISToString(Static.class.getResourceAsStream("/datamanagerlogo"));
        logo = logo.replaceAll("( +)", TermColors.BG_BLACK + "$1");
        logo = logo.replaceAll("_", TermColors.CYAN + TermColors.BG_CYAN + "_");
        logo = logo.replaceAll("/", TermColors.BG_WHITE + TermColors.WHITE + "/");
        String s = logo + TermColors.reset();
        return s;
    }
    
    public static String GetStringResource(String name){
        return GetStringResource(Static.class, name);
    }
    
    public static String GetStringResource(Class path, String name){
        return Installer.parseISToString(path.getResourceAsStream(name));
    }

    /**
     * Pulls out the MCChatColors from the string, and replaces them
     * with the nearest match ANSI terminal color.
     * @param mes If null, simply returns null
     * @return 
     */
    public static String MCToANSIColors(String mes) {
        //Pull out the MC colors
        if(mes == null){
            return null;
        }
        return mes
                .replaceAll("§0", TermColors.BLACK + TermColors.BG_WHITE)
                .replaceAll("§1", TermColors.BLUE)
                .replaceAll("§2", TermColors.GREEN)
                .replaceAll("§3", TermColors.CYAN)
                .replaceAll("§4", TermColors.RED)
                .replaceAll("§5", TermColors.MAGENTA)
                .replaceAll("§6", TermColors.YELLOW)
                .replaceAll("§7", TermColors.WHITE)
                .replaceAll("§8", TermColors.BRIGHT_BLACK + TermColors.BG_BRIGHT_WHITE)
                .replaceAll("§9", TermColors.BRIGHT_BLUE)
                .replaceAll("§a", TermColors.BRIGHT_GREEN)
                .replaceAll("§b", TermColors.BRIGHT_CYAN)
                .replaceAll("§c", TermColors.BRIGHT_RED)
                .replaceAll("§d", TermColors.BRIGHT_MAGENTA)
                .replaceAll("§e", TermColors.BRIGHT_YELLOW)
                .replaceAll("§f", TermColors.BRIGHT_WHITE)
                .replaceAll("§k", "") //Uh, no equivalent for "random"
                .replaceAll("§l", TermColors.BOLD)
                .replaceAll("§m", TermColors.STRIKE)
                .replaceAll("§n", TermColors.UNDERLINE)
                .replaceAll("§o", TermColors.ITALIC)
                .replaceAll("§r", TermColors.reset());
                
                
    }

    public static void InjectPlayer(MCPlayer player) {
        injectedPlayers.put(player.getName(), player);
    }
    
    public static void UninjectPlayer(MCPlayer player){
        injectedPlayers.remove(player.getName());
    }

    public static void HostnameCache(final MCPlayer p) {
        CommandHelperPlugin.hostnameLookupThreadPool.submit(new Runnable(){
           public void run(){
               CommandHelperPlugin.hostnameLookupCache.put(p.getName(),
                       p.getAddress().getHostName());
           } 
        });
    }
    
    public static void SetPlayerHost(MCPlayer p, String host){
        hostCache.put(p.getName(), host);
    }
    public static String GetHost(MCPlayer p){
        return hostCache.get(p.getName());
    }
    
    public static void AssertPlayerNonNull(MCPlayer p, Target t){
        if(p == null){
            throw new ConfigRuntimeException("No player was specified!", ExceptionType.PlayerOfflineException, t);
        }
    }
	
	public static long msToTicks(long ms){
		return ms / 50;
	}
	
	public static void AssertNonNull(Object var, String message){
		if(var == null){
			throw new NullPointerException(message);
		}
	}
	
	/**
	 * Generates a new environment, assuming that the jar has a folder next to it named CommandHelper, and that
	 * folder is the root.
	 * @return
	 * @throws IOException
	 * @throws DataSourceException
	 * @throws URISyntaxException 
	 */
	public static Environment GenerateStandaloneEnvironment() throws IOException, DataSourceException, URISyntaxException{
		return GenerateStandaloneEnvironment(new PermissionsResolver.PermissiveResolver());
	}
	
	/**
	 * Generates a new environment, using the permissions resolver given. It is assumed that the jar has a folder
	 * next to it named CommandHelper, and that folder is the root.
	 * @param permissionsResolver
	 * @return
	 * @throws IOException
	 * @throws DataSourceException
	 * @throws URISyntaxException 
	 */
	public static Environment GenerateStandaloneEnvironment(PermissionsResolver permissionsResolver) throws IOException, DataSourceException, URISyntaxException{
		File jarLocation;
		if(Static.class.getProtectionDomain().getCodeSource().getLocation() != null){
			jarLocation = new File(Static.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile();
		} else {
			jarLocation = new File(".");
		}
		ConnectionMixinFactory.ConnectionMixinOptions options = new ConnectionMixinFactory.ConnectionMixinOptions();
		options.setWorkingDirectory(new File(jarLocation, "CommandHelper/"));
		PersistanceNetwork persistanceNetwork = new PersistanceNetwork(new File(jarLocation, "CommandHelper/persistance.config"), 
				new URI("sqlite://" + new File(jarLocation, "CommandHelper/persistance.db").getCanonicalPath().replace("\\", "/")), options);
		GlobalEnv gEnv = new GlobalEnv(new ExecutionQueue("MethodScript", "default"), 
				new Profiler(new File("CommandHelper/profiler.config")), persistanceNetwork, permissionsResolver, new File(jarLocation, "CommandHelper/"));
		return Environment.createEnvironment(gEnv, new CommandHelperEnvironment());
	}
	
	/**
	 * Asserts that all the args are not CNulls. If so, throws a ConfigRuntimeNullPointerException
	 * @param t
	 * @param args
	 * @throws ConfigRuntimeException 
	 */
	public static void AssertNonCNull(Target t, Construct ... args) throws ConfigRuntimeException {
		for(Construct arg : args){
			if(arg instanceof CNull){
				throw new ConfigRuntimeException("Argument was null, and nulls are not allowed.", ExceptionType.NullPointerException, t);
			}
		}
	}
    
}
