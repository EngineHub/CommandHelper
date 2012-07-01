package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Prefs {
    private static enum PNames{
        ALLOW_DEBUG_LOGGING("allow-debug-logging"),
        ALLOW_PROFILING("allow-profiling"),
        BASE_DIR("base-dir"),
        CASE_SENSITIVE("case-sensitive"),
        CONSOLE_LOG_COMMANDS("console-log-commands"),
        DEBUG_LOG_FILE("debug-log-file"),
        DEBUG_MODE("debug-mode"),
        ENABLE_INTERPRETER("enable-interpreter"),
        HALT_ON_FAILURE("halt-on-failure"),
        MAIN_FILE("main-file"),
        PLAY_DIRTY("play-dirty"),
        PROFILING_FILE("profiling-file"),
        SCRIPT_NAME("script-name"),
        SHOW_SPLASH_SCREEN("show-splash-screen"),
        SHOW_WARNINGS("show-warnings"),
        STANDARD_LOG_FILE("standard-log-file"),
        USE_COLORS("use-colors");
        String name;
        private PNames(String name){
            this.name = name;
        }
        public String config(){
            return name;
        }
    }
    
    private static Preferences prefs;
    
    public static Boolean AllowDebugLogging(){
        return (Boolean)pref(PNames.ALLOW_DEBUG_LOGGING);
    }

    public static Boolean AllowProfiling(){
        return (Boolean)pref(PNames.ALLOW_PROFILING);
    }
    
    public static String BaseDir(){
        return (String)pref(PNames.BASE_DIR);
    }
    
    public static Boolean CaseSensitive(){
        return (Boolean)pref(PNames.CASE_SENSITIVE);
    }
    
    public static Boolean ConsoleLogCommands(){
        return (Boolean)pref(PNames.CONSOLE_LOG_COMMANDS);
    }
    
    public static String DebugLogFile(){
        return (String)pref(PNames.DEBUG_LOG_FILE);
    }
    
    public static Boolean DebugMode(){
        return (Boolean)pref(PNames.DEBUG_MODE);
    }
    
    public static Boolean EnableInterpreter(){
        return (Boolean)pref(PNames.ENABLE_INTERPRETER);
    }
    
    public static Boolean HaltOnFailure() {
        return (Boolean)pref(PNames.HALT_ON_FAILURE);
    }
    
    public static void init(File f) throws IOException {
        ArrayList<Preferences.Preference> a = new ArrayList<Preferences.Preference>();
        //a.add(new Preference("check-for-updates", "false", Type.BOOLEAN, "Whether or not to check to see if there's an update for CommandHelper"));
        a.add(new Preference(PNames.DEBUG_MODE.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to display debug information in the console"));
        a.add(new Preference(PNames.SHOW_WARNINGS.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to display warnings in the console, while compiling"));
        a.add(new Preference(PNames.CONSOLE_LOG_COMMANDS.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to display the original command in the console when it is run"));
        //a.add(new Preference("max-sleep-time", "5", Type.INT, "The maximum number of seconds a sleep function can sleep for. If <= 0, no limit is imposed. Must be an integer."));
        a.add(new Preference(PNames.SCRIPT_NAME.config(), "config.txt", Preferences.Type.STRING, "The path to the config file, relative to the CommandHelper plugin folder"));
        a.add(new Preference(PNames.ENABLE_INTERPRETER.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to enable the /interpreter command. Note that even with this enabled, a player must still have the commandhelper.interpreter permission, but"
                + " setting it to false prevents all players from accessing the interpreter regardless of their permissions."));
        a.add(new Preference(PNames.BASE_DIR.config(), "", Preferences.Type.STRING, "The base directory that scripts can read and write to. If left blank, then the default of the server directory will be used. "
                + "This setting affects functions like include and read."));
        a.add(new Preference(PNames.PLAY_DIRTY.config(), "false", Preferences.Type.BOOLEAN, "Makes CommandHelper play dirty and break all sorts of programming rules, so that other plugins can't interfere with the operations that you defined. Note that doing this essentially makes CommandHelper have absolute say over commands. Use this setting only if you can't get another plugin to cooperate with CH, because it is a global setting."));
        a.add(new Preference(PNames.CASE_SENSITIVE.config(), "true", Preferences.Type.BOOLEAN, "Makes command matching be case sensitive. If set to false, if your config defines /cmd, but the user runs /CMD, it will trigger the command anyways."));
        a.add(new Preference(PNames.MAIN_FILE.config(), "main.ms", Preferences.Type.STRING, "The path to the main file, relative to the CommandHelper folder"));
        a.add(new Preference(PNames.ALLOW_DEBUG_LOGGING.config(), "false", Preferences.Type.BOOLEAN, "If set to false, the Debug class of functions will do nothing."));
        a.add(new Preference(PNames.DEBUG_LOG_FILE.config(), "logs/debug/%Y-%M-%D-debug.log", Preferences.Type.STRING, "The path to the debug output log file. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are for whatever reason leaving logging on, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction. The logger preferences file is created in the same directory this file is in as well, and is named loggerPreferences.txt"));
        a.add(new Preference(PNames.STANDARD_LOG_FILE.config(), "logs/%Y-%M-%D-commandhelper.log", Preferences.Type.STRING, "The path the standard log files that the log() function writes to. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are actively logging things, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction."));
        a.add(new Preference(PNames.ALLOW_PROFILING.config(), "false", Preferences.Type.BOOLEAN, "If set to false, the Profiling class of functions will do nothing."));
        a.add(new Preference(PNames.PROFILING_FILE.config(), "logs/profiling/%Y-%M-%D-profiling.log", Preferences.Type.STRING, "The path to the profiling logs. These logs are perf4j formatted logs. Consult the documentation for more information."));
        a.add(new Preference(PNames.SHOW_SPLASH_SCREEN.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to show the splash screen at server startup"));
        a.add(new Preference(PNames.USE_COLORS.config(), (TermColors.SYSTEM == TermColors.SYS.WINDOWS ? "false" : "true"), Preferences.Type.BOOLEAN, "Whether or not to use console colors. If this is a Windows machine, defaults to false, however, it can be toggled manually, and will then respect your setting."));
        a.add(new Preference(PNames.HALT_ON_FAILURE.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to halt compilation of pure mscript files if a compilation failure occurs in any one of the files."));
        prefs = new Preferences("CommandHelper", Static.getLogger(), a);
        prefs.init(f);
    }
    
    public static String MainFile(){
        return (String)pref(PNames.MAIN_FILE);
    }
    
    public static Boolean PlayDirty(){
        return (Boolean)pref(PNames.PLAY_DIRTY);
    }
    
    private static Object pref(PNames name){
        if(prefs == null){
            //Uh oh. Default!
            try {
                Prefs.init(new File("plugins/CommandHelper/preferences.txt"));
            } catch (IOException ex) {
                //Well. We tried.
                Logger.getLogger(Prefs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return prefs.getPreference(name.config());
    }
    
    public static String ProfilingFile(){
        return (String)pref(PNames.PROFILING_FILE);
    }
    
    public static String ScriptName(){
        return (String)pref(PNames.SCRIPT_NAME);
    }
    
    public static Boolean ShowSplashScreen(){
        return (Boolean)pref(PNames.SHOW_SPLASH_SCREEN);
    }
    
    public static Boolean ShowWarnings(){
        return (Boolean)pref(PNames.SHOW_WARNINGS);
    }
    
    public static String StandardLogFile(){
        return (String)pref(PNames.STANDARD_LOG_FILE);
    }
    
    public static Boolean UseColors(){
        return (Boolean)pref(PNames.USE_COLORS);
    }
}
