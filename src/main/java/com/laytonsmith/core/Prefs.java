package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.GroupData;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.TermColors;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class Prefs {

	private Prefs() {
	}

	/**
	 * Given a preference name, returns the preference value.
	 *
	 * @param name
	 * @return
	 */
	public static Boolean prefB(PNames name) {
		if(prefs == null) {
			//Uh oh.
			throw new RuntimeException("Preferences have not been initialized!");
		}
		return prefs.getBooleanPreference(name.config());
	}

	public static String prefS(PNames name) {
		if(prefs == null) {
			throw new RuntimeException("Preferences have not been initialized!");
		}
		return prefs.getStringPreference(name.config());
	}

	public static Integer prefI(PNames name) {
		if(prefs == null) {
			throw new RuntimeException("Preferences have not been initialized!");
		}
		return prefs.getIntegerPreference(name.config());
	}

	public static Double prefD(PNames name) {
		if(prefs == null) {
			throw new RuntimeException("Preferences have not been initialized!");
		}
		return prefs.getDoublePreference(name.config());
	}

	private static Preferences prefs;
	private static Thread watcherThread;

	/**
	 * A list of preferences that are known to MethodScript. Additional preferences may be set in the preferences table,
	 * but they will not be listed here.
	 */
	public static enum PNames {
		DEBUG_MODE("debug-mode", Preferences.Type.BOOLEAN),
		SHOW_WARNINGS("show-warnings", Preferences.Type.BOOLEAN),
		CONSOLE_LOG_COMMANDS("console-log-commands", Preferences.Type.BOOLEAN),
		SCRIPT_NAME("script-name", Preferences.Type.STRING),
		ENABLE_INTERPRETER("enable-interpreter", Preferences.Type.BOOLEAN),
		BASE_DIR("base-dir", Preferences.Type.FILE),
		PLAY_DIRTY("play-dirty", Preferences.Type.BOOLEAN),
		CASE_SENSITIVE("case-sensitive", Preferences.Type.BOOLEAN),
		MAIN_FILE("main-file", Preferences.Type.FILE),
		ALLOW_DEBUG_LOGGING("allow-debug-logging", Preferences.Type.BOOLEAN),
		DEBUG_LOG_FILE("debug-log-file", Preferences.Type.FILE),
		STANDARD_LOG_FILE("standard-log-file", Preferences.Type.FILE),
		ALLOW_PROFILING("allow-profiling", Preferences.Type.BOOLEAN),
		PROFILING_FILE("profiling-file", Preferences.Type.FILE),
		SHOW_SPLASH_SCREEN("show-splash-screen", Preferences.Type.BOOLEAN),
		USE_COLORS("use-colors", Preferences.Type.BOOLEAN),
		HALT_ON_FAILURE("halt-on-failure", Preferences.Type.BOOLEAN),
		USE_SUDO_FALLBACK("use-sudo-fallback", Preferences.Type.BOOLEAN),
		ALLOW_SHELL_COMMANDS("allow-shell-commands", Preferences.Type.BOOLEAN),
		ALLOW_DYNAMIC_SHELL("allow-dynamic-shell", Preferences.Type.BOOLEAN),
		SCREAM_ERRORS("scream-errors", Preferences.Type.BOOLEAN),
		INTERPRETER_TIMEOUT("interpreter-timeout", Preferences.Type.INT),
		STRICT_MODE("strict-mode", Preferences.Type.BOOLEAN);
		private final String name;
		private final Preferences.Type type;

		private PNames(String name, Preferences.Type type) {
			this.name = name;
			this.type = type;
		}

		/**
		 * Returns the name that will be listed in the config, not the name of the enum.
		 *
		 * @return
		 */
		public String config() {
			return name;
		}

		public Preferences.Type type() {
			return this.type;
		}
	}

	private static final GroupData GENERAL_GROUP = new GroupData("General").setSortOrder(0);
	private static final GroupData LOGGING_GROUP = new GroupData("Logging")
			.setDescription("Settings related to logging capabilities.");
	private static final GroupData PROFILING_GROUP = new GroupData("Profiling")
			.setDescription("Settings related to the built in profiler.");
	private static final GroupData SECURITY_GROUP = new GroupData("Security")
			.setDescription("Security related settings. Please ensure you understand the impact"
					+ " of changing these, as the defaults have been carefully selected.");
	private static final GroupData DEBUG_GROUP = new GroupData("Debugging");
	/**
	 * Initializes the global Prefs to this file.
	 *
	 * @param f
	 * @throws IOException
	 */
	public static void init(final File f) throws IOException {
		ArrayList<Preferences.Preference> a = new ArrayList<>();
		a.add(new Preference(PNames.DEBUG_MODE.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to display"
				+ " debug information in the console", DEBUG_GROUP));
		a.add(new Preference(PNames.SHOW_WARNINGS.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to display"
				+ " warnings in the console, while compiling", GENERAL_GROUP));
		a.add(new Preference(PNames.CONSOLE_LOG_COMMANDS.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to"
				+ " display the original command in the console when it is run", LOGGING_GROUP));
		a.add(new Preference(PNames.SCRIPT_NAME.config(), "aliases.msa", Preferences.Type.STRING, "The path to the"
				+ " default config file, relative to the CommandHelper plugin folder", GENERAL_GROUP));
		a.add(new Preference(PNames.ENABLE_INTERPRETER.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to"
				+ " enable the /interpreter command. Note that even with this enabled, a player must still have the"
				+ " commandhelper.interpreter permission, but"
				+ " setting it to false prevents all players from accessing the interpreter regardless of their"
				+ " permissions.", SECURITY_GROUP));
		a.add(new Preference(PNames.BASE_DIR.config(), "", Preferences.Type.STRING, "The base directory/directories"
				+ " that scripts"
				+ " can read and write to. If left blank, then the default of the server directory will be used. "
				+ "This setting affects functions like include and read. Multiple directories may be specified, use ';'"
				+ " to separate file paths. For security reasons, symlinks are resolved to their actual location on"
				+ " disk, so if you intend"
				+ " for a symlinked folder to be accessible, you need to mark the location of the actual folder"
				+ " as accessible, even if the symlink itself is within another entry in the list. Note that empty"
				+ " paths are supported when splitting the path, and having a trailing ';' will cause the default"
				+ " path to be added, so don't end the path with a trailing ; if you don't intend for the path to"
				+ " include the default.", SECURITY_GROUP));
		a.add(new Preference(PNames.PLAY_DIRTY.config(), "false", Preferences.Type.BOOLEAN, "Makes CommandHelper play"
				+ " dirty and break all sorts of programming rules, so that other plugins can't interfere with the"
				+ " operations that you defined. Note that doing this essentially makes CommandHelper have absolute"
				+ " say over commands. Use this setting only if you can't get another plugin to cooperate with CH,"
				+ " because it is a global setting.", GENERAL_GROUP));
		a.add(new Preference(PNames.CASE_SENSITIVE.config(), "false", Preferences.Type.BOOLEAN, "Makes command matching"
				+ " be case sensitive. If set to false, if your config defines /cmd, but the user runs /CMD, it will"
				+ " trigger the command anyways.", GENERAL_GROUP));
		a.add(new Preference(PNames.MAIN_FILE.config(), "main.ms", Preferences.Type.STRING, "The path to the main file,"
				+ " relative to the CommandHelper folder", GENERAL_GROUP));
		a.add(new Preference(PNames.ALLOW_DEBUG_LOGGING.config(), "false", Preferences.Type.BOOLEAN, "If set to false,"
				+ " the Debug class of functions will do nothing.", LOGGING_GROUP));
		a.add(new Preference(PNames.DEBUG_LOG_FILE.config(), "logs/debug/%Y-%M-%D-debug.log", Preferences.Type.STRING,
				"The path to the debug output log file. Six variables are available, %Y, %M, and %D, %h, %m, %s, which"
				+ " are replaced with the current year, month, day, hour, minute and second respectively. It is"
				+ " highly recommended that you use at least year, month, and day if you are for whatever"
				+ " reason leaving logging on, otherwise the file size would get excessively large. The path"
				+ " is relative to the CommandHelper directory and is not bound by the base-dir restriction."
				+ " The logger preferences file is created in the same directory this file is in as well, and"
				+ " is named loggerPreferences.txt", LOGGING_GROUP));
		a.add(new Preference(PNames.STANDARD_LOG_FILE.config(), "logs/%Y-%M-%D-commandhelper.log",
				Preferences.Type.STRING, "The path the standard log files that the log() function writes to. Six"
				+ " variables are available, %Y, %M, and %D, %h, %m, %s, which are replaced with the current"
				+ " year, month, day, hour, minute and second respectively. It is highly recommended that you"
				+ " use at least year, month, and day if you are actively logging things, otherwise the file"
				+ " size would get excessively large. The path is relative to the CommandHelper directory and"
				+ " is not bound by the base-dir restriction.", LOGGING_GROUP));
		a.add(new Preference(PNames.ALLOW_PROFILING.config(), "false", Preferences.Type.BOOLEAN, "If set to false, the"
				+ " Profiling class of functions will do nothing.", PROFILING_GROUP));
		a.add(new Preference(PNames.PROFILING_FILE.config(), "logs/profiling/%Y-%M-%D-profiling.log",
				Preferences.Type.STRING, "The path to the profiling logs. These logs are perf4j formatted logs. Consult"
				+ " the documentation for more information.", PROFILING_GROUP));
		a.add(new Preference(PNames.SHOW_SPLASH_SCREEN.config(), "true", Preferences.Type.BOOLEAN, "Whether or not to"
				+ " show the splash screen at server startup", GENERAL_GROUP));
		a.add(new Preference(PNames.USE_COLORS.config(),
				"true", Preferences.Type.BOOLEAN, "Whether or"
				+ " not to use console colors.", GENERAL_GROUP));
		a.add(new Preference(PNames.HALT_ON_FAILURE.config(), "false", Preferences.Type.BOOLEAN, "Whether or not to"
				+ " halt compilation of pure mscript files if a compilation failure occurs in any one of the files.",
				GENERAL_GROUP));
		a.add(new Preference(PNames.USE_SUDO_FALLBACK.config(), "false", Preferences.Type.BOOLEAN, "If true, sudo()"
				+ " will use a less safe fallback method if it fails. See the documentation on the sudo function for"
				+ " more details. If this is true, a warning is issued at startup.", SECURITY_GROUP));
		a.add(new Preference(PNames.ALLOW_SHELL_COMMANDS.config(), "false", Preferences.Type.BOOLEAN, "If true, allows"
				+ " for the shell functions to be used from outside of cmdline mode. WARNING: Enabling these functions"
				+ " can be extremely dangerous if you accidentally allow uncontrolled access to them, and can"
				+ " grant full control of your server if not careful. Leave this set to false unless you really know"
				+ " what you're doing.", SECURITY_GROUP));
		a.add(new Preference(PNames.ALLOW_DYNAMIC_SHELL.config(), "false", Preferences.Type.BOOLEAN, "If true, allows"
				+ " use of the shell() functions from dynamic code sources, i.e"
				+ " interpreter or eval(). This almost certainly should always remain false, and if enabled, enabled"
				+ " only temporarily. If this is true, if an account with"
				+ " interpreter mode is compromised, the attacker could gain access to your entire server, under the"
				+ " user running minecraft, not just the game server.", SECURITY_GROUP));
		a.add(new Preference(PNames.SCREAM_ERRORS.config(), "false", Preferences.Type.BOOLEAN, "Setting this to true"
				+ " allows you to scream errors. Regardless of other settings"
				+ " that you may have unintentionally configured, this will override all ways of suppressing fatal"
				+ " errors, including uncaught exception"
				+ " handlers, error logging turned off, etc. This is meant as a last ditch effort to diagnosing an"
				+ " error. This implicitely turns debug mode"
				+ " on as well, which will cause even more error logging to occur.", DEBUG_GROUP));
		a.add(new Preference(PNames.INTERPRETER_TIMEOUT.config(), "15", Preferences.Type.INT, "Sets the time (in"
				+ " minutes) that interpreter mode is unlocked for when /interpreter-on is run from console. Set to 0"
				+ " (or a negative number)"
				+ " to disable this feature, and allow interpreter mode all the time. It is highly recommended that"
				+ " you leave this set to some number greater than 0, to enhance"
				+ " server security, and require a \"two step\" authentication for interpreter mode.", SECURITY_GROUP));
		a.add(new Preference(PNames.STRICT_MODE.config(), "false", Preferences.Type.BOOLEAN, "If set to true, forces"
				+ " all files that do not specifically set strict mode on or off into strict mode. See the"
				+ " documentation for more information about what strict mode does.", GENERAL_GROUP));
		prefs = new Preferences("CommandHelper", Static.getLogger(), a);
		prefs.init(f);
	}

	public static boolean isInitialized() {
		return prefs != null;
	}

	/**
	 * Convenience function to set the term colors based on the UseColors preference.
	 */
	public static void SetColors() {
		if(UseColors()) {
			TermColors.EnableColors();
		} else {
			TermColors.DisableColors();
		}
	}

	public static Boolean DebugMode() {
		return prefs.getBooleanPreference(PNames.DEBUG_MODE.config()) || ScreamErrors();
	}

	public static Boolean ShowWarnings() {
		return prefB(PNames.SHOW_WARNINGS);
	}

	public static Boolean ConsoleLogCommands() {
		return prefB(PNames.CONSOLE_LOG_COMMANDS);
	}

	public static String ScriptName() {
		return prefS(PNames.SCRIPT_NAME);
	}

	public static Boolean EnableInterpreter() {
		return prefB(PNames.ENABLE_INTERPRETER);
	}

	/**
	 * Note that this should be split(';', -1) to get the individual file paths.
	 * @return
	 */
	public static String BaseDir() {
		return prefS(PNames.BASE_DIR);
	}

	public static Boolean PlayDirty() {
		return prefB(PNames.PLAY_DIRTY);
	}

	public static Boolean CaseSensitive() {
		return prefB(PNames.CASE_SENSITIVE);
	}

	public static String MainFile() {
		return prefS(PNames.MAIN_FILE);
	}

	public static Boolean AllowDebugLogging() {
		return prefB(PNames.ALLOW_DEBUG_LOGGING);
	}

	public static String DebugLogFile() {
		return prefS(PNames.DEBUG_LOG_FILE);
	}

	public static String StandardLogFile() {
		return prefS(PNames.STANDARD_LOG_FILE);
	}

	public static Boolean AllowProfiling() {
		return prefB(PNames.ALLOW_PROFILING);
	}

	public static String ProfilingFile() {
		return prefS(PNames.PROFILING_FILE);
	}

	public static Boolean ShowSplashScreen() {
		return prefB(PNames.SHOW_SPLASH_SCREEN);
	}

	public static Boolean UseColors() {
		return prefB(PNames.USE_COLORS);
	}

	public static Boolean HaltOnFailure() {
		return prefB(PNames.HALT_ON_FAILURE);
	}

	public static Boolean UseSudoFallback() {
		return prefB(PNames.USE_SUDO_FALLBACK);
	}

	public static Boolean AllowShellCommands() {
		return prefB(PNames.ALLOW_SHELL_COMMANDS);
	}

	public static Boolean AllowDynamicShell() {
		return prefB(PNames.ALLOW_DYNAMIC_SHELL);
	}

	public static Boolean ScreamErrors() {
		return prefB(PNames.SCREAM_ERRORS);
	}

	public static Boolean StrictMode() {
		return prefB(PNames.STRICT_MODE);
	}

	public static Integer InterpreterTimeout() {
		Integer i = prefI(PNames.INTERPRETER_TIMEOUT);
		if(i < 0) {
			i = 0;
		}
		return i;
	}
}
