package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.TermColors;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public final class Prefs {
    
    private Prefs(){}
    
    private static Object pref(PNames name){
        if(prefs == null){
            //Uh oh.
            throw new RuntimeException("Preferences have not been initialized!");
        }
        return prefs.getPreference(name.config());
    }
    
    private static Preferences prefs;
    
    private static enum PNames{
        DEBUG_MODE("debug-mode"),
        SHOW_WARNINGS("show-warnings"),
        CONSOLE_LOG_COMMANDS("console-log-commands"),
        SCRIPT_NAME("script-name"),
        ENABLE_INTERPRETER("enable-interpreter"),
        BASE_DIR("base-dir"),
        PLAY_DIRTY("play-dirty"),
        CASE_SENSITIVE("case-sensitive"),
        MAIN_FILE("main-file"),
        ALLOW_DEBUG_LOGGING("allow-debug-logging"),
        DEBUG_LOG_FILE("debug-log-file"),
        STANDARD_LOG_FILE("standard-log-file"),
        ALLOW_PROFILING("allow-profiling"),
        PROFILING_FILE("profiling-file"),
        SHOW_SPLASH_SCREEN("show-splash-screen"),
        USE_COLORS("use-colors"),
        HALT_ON_FAILURE("halt-on-failure"),
		USE_SUDO_FALLBACK("use-sudo-fallback"),
		ALLOW_SHELL_COMMANDS("allow-shell-commands"),
		ALLOW_DYNAMIC_SHELL("allow-dynamic-shell"),
		SCREAM_ERRORS("scream-errors"),
		INTERPRETER_TIMEOUT("interpreter-timeout");
        String name;
        private PNames(String name){
            this.name = name;
        }
        public String config(){
            return name;
        }
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
        a.add(new Preference(PNames.CASE_SENSITIVE.config(), "false", Preferences.Type.BOOLEAN, "Makes command matching be case sensitive. If set to false, if your config defines /cmd, but the user runs /CMD, it will trigger the command anyways."));
        a.add(new Preference(PNames.MAIN_FILE.config(), "main.ms", Preferences.Type.STRING, "The path to the main file, relative to the CommandHelper folder"));
        a.add(new Preference(PNames.ALLOW_DEBUG_LOGGING.config(), "false", Preferences.Type.BOOLEAN, "If set to false, the Debug class of functions will do nothing."));
        a.add(new Preference(PNames.DEBUG_LOG_FILE.config(), "logs/debug/%Y-%M-%D-debug.log", Preferences.Type.STRING, "The path to the debug output log file. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are for whatever reason leaving logging on, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction. The logger preferences file is created in the same directory this file is in as well, and is named loggerPreferences.txt"));
        a.add(new Preference(PNames.STANDARD_LOG_FILE.config(), "logs/%Y-%M-%D-commandhelper.log", Preferences.Type.STRING, "The path the standard log files that the log() function writes to. Six variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current year, month, day, hour, minute and second respectively. It is highly recommended that you use at least year, month, and day if you are actively logging things, otherwise the file size would get excessively large. The path is relative to the CommandHelper directory and is not bound by the base-dir restriction."));
        a.add(new Preference(PNames.ALLOW_PROFILING.config(), "false", Preferences.Type.BOOLEAN, "If set to false, the Profiling class of functions will do nothing."));
        a.add(new Preference(PNames.PROFILING_FILE.config(), "logs/profiling/%Y-%M-%D-profiling.log", Preferences.Type.STRING, "The path to the profiling logs. These logs are perf4j formatted logs. Consult the documentation for more information."));
        a.add(new Preference(PNames.SHOW_SPLASH_SCREEN.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to show the splash screen at server startup"));
        a.add(new Preference(PNames.USE_COLORS.config(), (TermColors.SYSTEM == TermColors.SYS.WINDOWS ? "false" : "true"), Preferences.Type.BOOLEAN, "Whether or not to use console colors. If this is a Windows machine, defaults to false, however, it can be toggled manually, and will then respect your setting."));
        a.add(new Preference(PNames.HALT_ON_FAILURE.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to halt compilation of pure mscript files if a compilation failure occurs in any one of the files."));
		a.add(new Preference(PNames.USE_SUDO_FALLBACK.config(), "false", Preferences.Type.BOOLEAN, "If true, sudo() will use a less safe fallback method if it fails. See the documentation on the sudo function for more details. If this is true, a warning is issued at startup."));
		a.add(new Preference(PNames.ALLOW_SHELL_COMMANDS.config(), "false", Preferences.Type.BOOLEAN, "If true, allows for the shell functions to be used from outside of cmdline mode. WARNING: Enabling these functions can be extremely dangerous if you accidentally allow uncontrolled access to them, and can"
				+ " grant full control of your server if not careful. Leave this set to false unless you really know what you're doing."));
		a.add(new Preference(PNames.ALLOW_DYNAMIC_SHELL.config(), "false", Preferences.Type.BOOLEAN, "If true, allows use of the shell() functions from dynamic code sources, i.e"
				+ " interpreter or eval(). This almost certainly should always remain false, and if enabled, enabled only temporarily. If this is true, if an account with"
				+ " interpreter mode is compromised, the attacker could gain access to your entire server, under the user running minecraft, not just the game server."));
		a.add(new Preference(PNames.SCREAM_ERRORS.config(), "false", Preferences.Type.BOOLEAN, "Setting this to true allows you to scream errors. Regardless of other settings"
				+ " that you may have unintentionally configured, this will override all ways of suppressing fatal errors, including uncaught exception"
				+ " handlers, error logging turned off, etc. This is meant as a last ditch effort to diagnosing an error. This implicitely turns debug mode"
				+ " on as well, which will cause even more error logging to occur."));
		a.add(new Preference(PNames.INTERPRETER_TIMEOUT.config(), "15", Preferences.Type.INT, "Sets the time (in minutes) that interpreter mode is unlocked for when /interpreter-on is run from console. Set to 0 (or a negative number)"
				+ " to disable this feature, and allow interpreter mode all the time. It is highly recommended that you leave this set to some number greater than 0, to enahnce"
				+ " server security, and require a \"two step\" authentication for interpreter mode."));
        prefs = new Preferences("CommandHelper", Static.getLogger(), a);
        prefs.init(f);
    }
	
	public static boolean isInitialized(){
		return prefs != null;
	}
	
	/**
	 * Convenience function to set the term colors based on the UseColors preference.
	 */
	public static void SetColors(){
		if(UseColors()){
			TermColors.EnableColors();
		} else {
			TermColors.DisableColors();
		}
	}
    
    public static Boolean DebugMode(){
        return (Boolean)pref(PNames.DEBUG_MODE) || ScreamErrors();
    }
    
    public static Boolean ShowWarnings(){
        return (Boolean)pref(PNames.SHOW_WARNINGS);
    }
    
    public static Boolean ConsoleLogCommands(){
        return (Boolean)pref(PNames.CONSOLE_LOG_COMMANDS);
    }
    
    public static String ScriptName(){
        return (String)pref(PNames.SCRIPT_NAME);
    }
    
    public static Boolean EnableInterpreter(){
        return (Boolean)pref(PNames.ENABLE_INTERPRETER);
    }
    
    public static String BaseDir(){
        return (String)pref(PNames.BASE_DIR);
    }
    
    public static Boolean PlayDirty(){
        return (Boolean)pref(PNames.PLAY_DIRTY);
    }
    
    public static Boolean CaseSensitive(){
        return (Boolean)pref(PNames.CASE_SENSITIVE);
    }
    
    public static String MainFile(){
        return (String)pref(PNames.MAIN_FILE);
    }
    
    public static Boolean AllowDebugLogging(){
        return (Boolean)pref(PNames.ALLOW_DEBUG_LOGGING);
    }
    
    public static String DebugLogFile(){
        return (String)pref(PNames.DEBUG_LOG_FILE);
    }
    
    public static String StandardLogFile(){
        return (String)pref(PNames.STANDARD_LOG_FILE);
    }
    
    public static Boolean AllowProfiling(){
        return (Boolean)pref(PNames.ALLOW_PROFILING);
    }
    
    public static String ProfilingFile(){
        return (String)pref(PNames.PROFILING_FILE);
    }
    
    public static Boolean ShowSplashScreen(){
        return (Boolean)pref(PNames.SHOW_SPLASH_SCREEN);
    }
    
    public static Boolean UseColors(){
        return (Boolean)pref(PNames.USE_COLORS);
    }
    
    public static Boolean HaltOnFailure() {
        return (Boolean)pref(PNames.HALT_ON_FAILURE);
    }
	
	public static Boolean UseSudoFallback(){
		return (Boolean)pref(PNames.USE_SUDO_FALLBACK);
	}
	
	public static Boolean AllowShellCommands(){
		return (Boolean)pref(PNames.ALLOW_SHELL_COMMANDS);
	}
	
	public static Boolean AllowDynamicShell(){
		return (Boolean)pref(PNames.ALLOW_DYNAMIC_SHELL);
	}
	
	public static Boolean ScreamErrors(){
		return (Boolean)pref(PNames.SCREAM_ERRORS);
	}
	
	public static Integer InterpreterTimeout(){
		Integer i = (Integer)pref(PNames.INTERPRETER_TIMEOUT);
		if(i < 0){
			i = 0;
		}
		return i;
	}
}
