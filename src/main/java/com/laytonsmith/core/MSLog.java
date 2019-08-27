package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Log class simplifies logging for a user. Log messages are categorized by module and urgency, and the user
 * configures each separately, allowing for more granular log control. Eventually, if other clients are able to connect
 * to the system, they may each specify logging granularity, but the default is to use the preferences file and output
 * to the debug log file.
 *
 * Extensions may add modules (Tags) easily, and more generally, tags may be defined anywhere in the code. To
 * do so,
 *
 */
@SuppressWarnings("checkstyle:finalclass") // StaticTest.InstallFakeLogger() mocks this class, so it cannot be final.
public class MSLog {

	/**
	 * A StringProvider is a small object which returns a string. This is useful to defer creation of strings
	 * unless actually necessary, when construction of the string could potentially be an expensive operation.
	 */
	public static interface StringProvider {
		String getString();
	}

	private MSLog() {
	}

	private static final String HEADER = "The logger preferences allow you to granularly define what information\n"
			+ "is written out to file, to assist you in debugging or general logging.\n"
			+ "You may set the granularity of all the tags individually, to any one of\n"
			+ "the following values:\n"
			+ "OFF - Turns off all logging for this module.\n"
			+ "ON - Synonym for ERRORS\n"
			+ "\n"
			+ "ERROR - Logs errors, or other high importance messages.\n"
			+ "WARNING - Logs warnings and above.\n"
			+ "INFO - Logs informational notices, and above.\n"
			+ "DEBUG - Logs useful debugging information, and above.\n"
			+ "VERBOSE - Logs every little detail.\n"
			+ "\n\n"
			+ "In many cases, components will only use the ERROR level, therefore, ON is a synonym.\n"
			+ "However, in some cases, a component may give you more information if you set it lower."
			+ "";

	private static Preferences prefs;
	private static final Map<Tag, LogLevel> LOOKUP = new HashMap<>();

	private static final Set<Tag> KNOWN_TAGS = new HashSet<>();

	/**
	 * Statically defined, static instances of a {@link Tag} object tagged with the {@link LogTag} are gathered
	 * at startup, and represent the possible tags that can be logged. Extensions and other code may implement
	 * additional values. Generally, these are implemented as an enum that implements {@link Tag}, but they
	 * may be normal class members as well.
	 */
	public static interface Tag {
		/**
		 * This is the name of the tag in the preferences file. For standardization purposes, this should be
		 * a lowercase word.
		 * @return
		 */
		String getName();

		/**
		 * The comment description that will be provided in the preferences file. This should describe what events
		 * in general are logged under this tag.
		 * @return
		 */
		String getDescription();

		/**
		 * The default log level that the preference should be initially configured at in new installations.
		 * @return
		 */
		LogLevel getLevel();
	}

	/**
	 * Tagged to accessible, statically defined, static instances of a {@link Tag} object. These are gathered
	 * at startup, and represent the possible tags that can be logged. Extensions and other code may implement
	 * additional values. Generally, these are implemented as an enum that implements {@link Tag}, but they
	 * may be normal class members as well.
	 */
	@java.lang.annotation.Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface LogTag {}

	/**
	 * These are the default tags. However, new ones should generally not be added here, but instead
	 * added near the code that would use the tag (using the Tag interface and @LogTag annotation.)
	 */
	public enum Tags implements Tag {
		@LogTag
		COMPILER("compiler", "Logs compiler errors (but not runtime errors)", LogLevel.WARNING),
		@LogTag
		RUNTIME("runtime", "Logs runtime errors, (exceptions that bubble all the way to the top)", LogLevel.ERROR),
		@LogTag
		FALSESTRING("falsestring", "Logs coersion of the string \"false\" to boolean, which is actually true",
			LogLevel.ERROR),
		@LogTag
		DEPRECATION("deprecation", "Shows deprecation warnings", LogLevel.WARNING),
		@LogTag
		PERSISTENCE("persistence", "Logs when any persistence actions occur.", LogLevel.ERROR),
		//TODO Add the rest of these hooks into the code
		//        IO("IO", "Logs when the filesystem is accessed.", Level.OFF),
		//        EVENTS("events", "Logs bindings and use of an event.", Level.OFF),
		//        PROCEDURES("procedures", "Logs when a procedure is created", Level.OFF),
		@LogTag
		INCLUDES("includes", "Logs what file is requested when include() is used", LogLevel.ERROR),
		@LogTag
		GENERAL("general", "Anything that doesn't fit in a more specific category is logged here.", LogLevel.ERROR),
		@LogTag
		META("meta", "Functions in the meta class use this tag", LogLevel.ERROR),
		@LogTag
		EXTENSIONS("extensions", "Extension related logs use this tag", LogLevel.ERROR);

		private final String name;
		private final String description;
		private final LogLevel level;

		private Tags(String name, String description, LogLevel defaultLevel) {
			this.name = name;
			this.description = description;
			this.level = defaultLevel;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public LogLevel getLevel() {
			return level;
		}
	}

	private static File root = null;
	// Do not rename this field, it is used reflectively in tests
	private static MSLog instance = null;

	public static MSLog GetLogger() {
		if(root == null) {
			throw new RuntimeException("Logger is not initialized! Call "
					+ MSLog.class.getSimpleName() + ".initialize before using the logger.");
		}
		if(instance == null) {
			instance = new MSLog();
		}
		return instance;
	}

	/**
	 * Initializes the logger. This should be called once per JVM invocation. Eventually, a new instance of the logger
	 * should be created, but until then, the static approach is in use.
	 *
	 * @param root The root
	 */
	public static void initialize(File root) {
		MSLog.root = root;
		List<Preference> myPrefs = new ArrayList<>();
		List<Tag> tags = new ArrayList<>();
		for(Field f : ClassDiscovery.getDefaultInstance().loadFieldsWithAnnotation(MSLog.LogTag.class)) {
			try {
				Object o = f.get(null);
				if(o instanceof Tag) {
					tags.add((Tag) o);
				} else {
					System.err.println("Element tagged with LogTag, but is not an instance of Tag: "
						+ f.getDeclaringClass() + "." + f.getName());
				}
			} catch (IllegalArgumentException | IllegalAccessException ex) {
				System.err.println("Could not properly configure logger tag: " + ex.getMessage());
			}
		}
		for(Tag t : tags) {
			myPrefs.add(new Preference(t.getName(), t.getLevel().name(), Preferences.Type.STRING, t.getDescription()));
		}
		KNOWN_TAGS.addAll(tags);
		MSLog.prefs = new Preferences("CommandHelper", Static.getLogger(), myPrefs, HEADER);
		try {
			MSLog.prefs.init(MethodScriptFileLocations.getDefault().getLoggerPreferencesFile());
		} catch (IOException e) {
			Static.getLogger().log(java.util.logging.Level.SEVERE, "Could not create logger preferences", e);
		}
	}

	/**
	 * Gets the level for the specified tag
	 *
	 * @param tag
	 * @return
	 */
	private static LogLevel GetLevel(Tag tag) {
		if(!KNOWN_TAGS.contains(tag)) {
			String message = "Logging tag that was not properly configured: " + tag.getName()
					+ ". Tags must be registered with the @LogTag annotation, otherwise they"
					+ " are not configurable by the user!";
			// Log this everywhere, since this is a problem for the developer.
			System.err.println(message);
			GetLogger().e(Tags.GENERAL, message, Target.UNKNOWN);
		}
		if(LOOKUP.containsKey(tag)) {
			return LOOKUP.get(tag);
		}
		LogLevel level;
		try {
			String pref = prefs.getStringPreference(tag.getName());
			if("ON".equals(pref)) {
				level = LogLevel.ERROR;
			} else {
				level = LogLevel.valueOf(pref);
			}
		} catch (IllegalArgumentException e) {
			level = LogLevel.ERROR;
		}
		LOOKUP.put(tag, level);
		return level;
	}

	/**
	 * Returns true if a call to Log would cause this level and tag to be logged.
	 *
	 * @param tag
	 * @param l
	 * @return
	 */
	public boolean WillLog(Tag tag, LogLevel l) {
		LogLevel level = GetLevel(tag);
		if(level == LogLevel.OFF) {
			return false;
		} else {
			return l.getLevel() <= level.getLevel();
		}

	}

	/**
	 * From the given MsgBundles, picks the most appropriate log level, tending towards more verbose, and uses that
	 * message. This is useful if a message would be different, not just more information, given a level. For instance,
	 * given the following: LogOne(Tags.tag, new MsgBundle(Level.ERROR, "An error occured"), new
	 * MsgBundle(Level.VERBOSE, "An error occured, and here is why")), if the level was set to ERROR, only "An error
	 * occured" would show. If the level was set to VERBOSE, only "An error occured, and here is why" would show.
	 *
	 * @param tag
	 * @param t
	 * @param messages
	 */
	public void LogOne(Tag tag, Target t, MsgBundle... messages) {
		if(GetLevel(tag) == LogLevel.OFF) {
			return; //Bail!
		}
		LogLevel tagLevel = GetLevel(tag);
		LogLevel[] levels = new LogLevel[]{LogLevel.VERBOSE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR};
		for(LogLevel l : levels) {
			for(MsgBundle b : messages) {
				if(b.level == l && b.level == tagLevel) {
					//Found it.
					Log(tag, l, HEADER, t);
					return;
				}
			}
		}
	}

	/**
	 * Logs all the applicable levels. This is useful if a message is always displayed, but progressively more
	 * information is displayed the more verbose it gets.
	 *
	 * @param tag
	 * @param t
	 * @param messages
	 */
	public void LogAll(Tag tag, Target t, MsgBundle... messages) {
		if(GetLevel(tag) == LogLevel.OFF) {
			return; //For efficiency sake, go ahead and bail.
		}
		for(MsgBundle b : messages) {
			Log(tag, b.level, b.message, t);
		}
	}

	/**
	 * Logs the given message at the level of ERROR/ON.
	 *
	 * @param module
	 * @param message
	 * @param t
	 */
	public void Log(Tag module, String message, Target t) {
		Log(module, LogLevel.ERROR, message, t);
	}

	/**
	 * Equivalent to Log(modules, level, message, t, true);
	 *
	 * @param modules
	 * @param level
	 * @param message
	 * @param t
	 */
	public void Log(Tag modules, LogLevel level, String message, Target t) {
		Log(modules, level, message, t, true);
	}

	public void Log(Tag modules, LogLevel level, StringProvider message, Target t) {
		Log(modules, level, message, t, true);
	}

	public void Log(Tag modules, LogLevel level, String message, Target t, boolean printScreen) {
		Log(modules, level, () -> message, t, printScreen);
	}

	/**
	 * Logs the given message at the specified level.
	 *
	 * @param modules
	 * @param level
	 * @param message
	 * @param t
	 * @param printScreen
	 */
	public void Log(Tag modules, LogLevel level, StringProvider message, Target t, boolean printScreen) {
		LogLevel moduleLevel = GetLevel(modules);
		if(moduleLevel == LogLevel.OFF && !Prefs.ScreamErrors()) {
			return; //Bail as quick as we can!
		}
		if(moduleLevel.level >= level.level || (moduleLevel == LogLevel.ERROR && Prefs.ScreamErrors())) {
			//We want to do the log
			try {
				Static.LogDebug(root, "[" + level.name() + "][" + modules.getName() + "] " + message.getString()
						+ (t != Target.UNKNOWN ? " " + t.toString() : ""), level, printScreen);
			} catch (IOException e) {
				//Well, shoot.
				if(level.level <= 1) {
					StreamUtils.GetSystemErr().println("Was going to print information to the log, but instead, there was"
							+ " an IOException: ");
					e.printStackTrace(StreamUtils.GetSystemErr());
				}
			}
		}
	}

	/**
	 * Logs the given exception at the ERROR level.
	 *
	 * @param modules
	 * @param throwable
	 * @param t
	 */
	public void e(Tag modules, Throwable throwable, Target t) {
		Log(modules, LogLevel.ERROR, StackTraceUtils.GetStacktrace(throwable), t, true);
	}

	/**
	 * Logs the given message at the ERROR level.
	 *
	 * @param modules
	 * @param message
	 * @param t
	 */
	public void e(Tag modules, String message, Target t) {
		Log(modules, LogLevel.ERROR, message, t, true);
	}

	/**
	 * Logs the given message at the WARNING level.
	 *
	 * @param modules
	 * @param message
	 * @param t
	 */
	public void w(Tag modules, String message, Target t) {
		Log(modules, LogLevel.WARNING, message, t, true);
	}

	/**
	 * Logs the given message at the INFO level.
	 *
	 * @param modules
	 * @param message
	 * @param t
	 */
	public void i(Tag modules, String message, Target t) {
		Log(modules, LogLevel.INFO, message, t, true);
	}

	/**
	 * Logs the given message at the DEBUG level.
	 *
	 * @param modules
	 * @param message
	 * @param t
	 */
	public void d(Tag modules, String message, Target t) {
		Log(modules, LogLevel.DEBUG, message, t, true);
	}

	/**
	 * Logs the given message at the VERBOSE level.
	 *
	 * @param modules
	 * @param message
	 * @param t
	 */
	public void v(Tag modules, String message, Target t) {
		Log(modules, LogLevel.VERBOSE, message, t, true);
	}

	public static class MsgBundle {

		private LogLevel level;
		private String message;

		public MsgBundle(LogLevel level, String message) {
			this.level = level;
			this.message = message;
		}
	}
}
