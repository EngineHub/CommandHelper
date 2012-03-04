package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.TermColors;
import java.util.ArrayList;

/**
 *
 * @author layton
 */
public class Prefs {
    private static Object pref(String name){
        return Static.getPreferences().getPreference(name);
    }

    static void init() {
        ArrayList<Preferences.Preference> a = new ArrayList<Preferences.Preference>();
        //a.add(new Preference("check-for-updates", "false", Type.BOOLEAN, "Whether or not to check to see if there's an update for CommandHelper"));
        a.add(new Preferences.Preference("debug-mode", "false", Preferences.Type.BOOLEAN, "Whether or not to display debug information in the console"));
        a.add(new Preferences.Preference("show-warnings", "true", Preferences.Type.BOOLEAN, "Whether or not to display warnings in the console, while compiling"));
        a.add(new Preferences.Preference("console-log-commands", "true", Preferences.Type.BOOLEAN, "Whether or not to display the original command in the console when it is run"));
        //a.add(new Preference("max-sleep-time", "5", Type.INT, "The maximum number of seconds a sleep function can sleep for. If <= 0, no limit is imposed. Must be an integer."));
        a.add(new Preferences.Preference("script-name", "config.txt", Preferences.Type.STRING, "The path to the config file, relative to the CommandHelper plugin folder"));
        a.add(new Preferences.Preference("enable-interpreter", "false", Preferences.Type.BOOLEAN, "Whether or not to enable the /interpreter command. Note that even with this enabled, a player must still have the commandhelper.interpreter permission, but"
                + " setting it to false prevents all players from accessing the interpreter regardless of their permissions."));
        a.add(new Preferences.Preference("base-dir", "", Preferences.Type.STRING, "The base directory that scripts can read and write to. If left blank, then the default of the server directory will be used. "
                + "This setting affects functions like include and read."));
        a.add(new Preferences.Preference("play-dirty", "false", Preferences.Type.BOOLEAN, "Makes CommandHelper play dirty and break all sorts of programming rules, so that other plugins can't interfere with the operations that you defined. Note that doing this essentially makes CommandHelper have absolute say over commands. Use this setting only if you can't get another plugin to cooperate with CH, because it is a global setting."));
        a.add(new Preferences.Preference("case-sensitive", "true", Preferences.Type.BOOLEAN, "Makes command matching be case sensitive. If set to false, if your config defines /cmd, but the user runs /CMD, it will trigger the command anyways."));
        a.add(new Preferences.Preference("main-file", "main.ms", Preferences.Type.STRING, "The path to the main file, relative to the CommandHelper folder"));
        a.add(new Preferences.Preference("allow-debug-logging", "false", Preferences.Type.BOOLEAN, "If set to false, the Debug class of functions will do nothing."));
        a.add(new Preferences.Preference("debug-log-file", "logs/debug/%Y-%M-%D-debug.log", Preferences.Type.STRING, "The path to the debug output log file. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are for whatever reason leaving logging on, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction."));
        a.add(new Preferences.Preference("standard-log-file", "logs/%Y-%M-%D-commandhelper.log", Preferences.Type.STRING, "The path the standard log files that the log() function writes to. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are actively logging things, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction."));
        a.add(new Preferences.Preference("allow-profiling", "false", Preferences.Type.BOOLEAN, "If set to false, the Profiling class of functions will do nothing."));
        a.add(new Preferences.Preference("profiling-file", "logs/profiling/%Y-%M-%D-profiling.log", Preferences.Type.STRING, "The path to the profiling logs. These logs are perf4j formatted logs. Consult the documentation for more information."));
        a.add(new Preferences.Preference("show-splash-screen", "true", Preferences.Type.BOOLEAN, "Whether or not to show the splash screen at server startup"));
        a.add(new Preferences.Preference("use-colors", (TermColors.SYSTEM == TermColors.SYS.WINDOWS ? "false" : "true"), Preferences.Type.BOOLEAN, "Whether or not to use console colors. If this is a Windows machine, defaults to false, however, it can be toggled manually, and will then respect your setting."));
        com.laytonsmith.commandhelper.CommandHelperPlugin.prefs = new Preferences("CommandHelper", Static.getLogger(), a);
    }
    
    public static Boolean DebugMode(){
        return (Boolean)pref("debug-mode");
    }
    
    public static Boolean ShowWarnings(){
        return (Boolean)pref("show-warnings");
    }
    
    public static Boolean ConsoleLogCommands(){
        return (Boolean)pref("console-log-commands");
    }
    
    public static String ScriptName(){
        return (String)pref("script-name");
    }
    
    public static Boolean EnableInterpreter(){
        return (Boolean)pref("enable-interpreter");
    }
    
    public static String BaseDir(){
        return (String)pref("base-dir");
    }
    
    public static Boolean PlayDirty(){
        return (Boolean)pref("play-dirty");
    }
    
    public static Boolean CaseSensitive(){
        return (Boolean)pref("case-sensitive");
    }
    
    public static String MainFile(){
        return (String)pref("main-file");
    }
    
    public static Boolean AllowDebugLogging(){
        return (Boolean)pref("allow-debug-logging");
    }
    
    public static String DebugLogFile(){
        return (String)pref("debug-log-file");
    }
    
    public static String StandardLogFile(){
        return (String)pref("standard-log-file");
    }
    
    public static Boolean AllowProfiling(){
        return (Boolean)pref("allow-profiling");
    }
    
    public static String ProfilingFile(){
        return (String)pref("profiling-file");
    }
    
    public static Boolean ShowSplashScreen(){
        return (Boolean)pref("show-splash-screen");
    }
    
    public static Boolean UseColors(){
        return (Boolean)pref("use-colors");
    }
}
