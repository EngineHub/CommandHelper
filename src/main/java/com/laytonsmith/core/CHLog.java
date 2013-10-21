package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
import com.laytonsmith.PureUtilities.Common.StackTraceUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.core.constructs.Target;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * The Log class simplifies logging for a user. Log messages are categorized by module and urgency,
 * and the user configures each separately, allowing for more granular log control. Eventually, if other
 * clients are able to connect to the system, they may each specify logging granularity, but the default
 * is to use the preferences file and output to the debug log file.
 * @author layton
 */
public final class CHLog {
    
    private CHLog(){}
    
    private static final String header = "The logger preferences allow you to granularly define what information\n"
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
    private static final EnumMap<Tags, LogLevel> lookup = new EnumMap<Tags, LogLevel>(Tags.class);
    public enum Tags{
        COMPILER("compiler", "Logs compiler errors (but not runtime errors)", LogLevel.ERROR),
        RUNTIME("runtime", "Logs runtime errors, (exceptions that bubble all the way to the top)", LogLevel.ERROR),
        DEPRECATION("deprecation", "Shows deprecation warnings", LogLevel.WARNING),
        PERSISTANCE("persistance", "Logs when any persistance actions occur.", LogLevel.ERROR),
        //TODO Add the rest of these hooks into the code
//        IO("IO", "Logs when the filesystem is accessed.", Level.OFF),
//        ALIAS("alias", "Logs use of user aliases.", Level.OFF),
//        EVENTS("events", "Logs bindings and use of an event.", Level.OFF),
//        PROCEDURES("procedures", "Logs when a procedure is created", Level.OFF),
        INCLUDES("includes", "Logs what file is requested when include() is used", LogLevel.ERROR),
        GENERAL("general", "Anything that doesn't fit in a more specific category is logged here.", LogLevel.ERROR),
        META("meta", "Functions in the meta class use this tag", LogLevel.ERROR),
		EXTENSIONS("extensions", "Extension related logs use this tag", LogLevel.ERROR)
        ;
        
        
        String name;
        String description;
        LogLevel level;
        private Tags(String name, String description, LogLevel defaultLevel){
            this.name = name;
            this.description = description;
            this.level = defaultLevel;
        }
    }
    
	private static File root = null;
	private static CHLog instance = null;
    
	public static CHLog GetLogger(){
		if(root == null){
			throw new RuntimeException("Logger is not initialized! Call CHLog.initialize before using the logger.");
		}
		if(instance == null){
			instance = new CHLog();
		}
		return instance;
	}
	
	/**
	 * Initializes the logger. This should be called once per JVM invocation.
	 * Eventually, a new instance of the logger should be created, but until then,
	 * the static approach is in use.
	 * @param root The root 
	 */
    public static void initialize(File root){
		CHLog.root = root;
        List<Preference> myPrefs = new ArrayList<Preference>();
        for(Tags t : Tags.values()){
            myPrefs.add(new Preference(t.name, t.level.name(), Preferences.Type.STRING, t.description));
        }
        CHLog.prefs = new Preferences("CommandHelper", Static.getLogger(), myPrefs, header);
        try{
            File file = new File(new File(root, Prefs.DebugLogFile()).getParentFile(), "loggerPreferences.txt");
            CHLog.prefs.init(file);
        } catch(IOException e){
            Static.getLogger().log(java.util.logging.Level.SEVERE, "Could not create logger preferences", e);
        }
    }
    
    /**
     * Gets the level for the specified tag
     * @param tag
     * @return 
     */
    private static LogLevel GetLevel(Tags tag){
        if(lookup.containsKey(tag)){
            return lookup.get(tag);
        }
        LogLevel level;
        try{
            level = LogLevel.valueOf((String)prefs.getPreference(tag.name));
        } catch(IllegalArgumentException e){
            level = LogLevel.ERROR;
        }
        lookup.put(tag, level);
        return level;
    }
    
    /**
     * Returns true if a call to Log would cause this level and tag to be logged.
     * @param tag
     * @param l
     * @return 
     */
    public boolean WillLog(Tags tag, LogLevel l){
        LogLevel level = GetLevel(tag);
        if(level == LogLevel.OFF){
            return false;
        } else {
            return l.getLevel() <= level.getLevel();
        }
        
    }
    
    
    /**
     * From the given MsgBundles, picks the most appropriate log level, tending towards more verbose,
     * and uses that message.
     * This is useful if a message would be different, not just more information, given a level.
     * For instance, given the following: LogOne(Tags.tag, new MsgBundle(Level.ERROR, "An error occured"),
     * new MsgBundle(Level.VERBOSE, "An error occured, and here is why")), if the level was set to ERROR, only "An error occured"
     * would show. If the level was set to VERBOSE, only "An error occured, and here is why" would show.
	 * @param tag
	 * @param t
     * @param messages 
     */
    public void LogOne(Tags tag, Target t, MsgBundle ... messages){
        if(GetLevel(tag) == LogLevel.OFF){
            return; //Bail!
        }
        LogLevel tagLevel = GetLevel(tag);
        LogLevel [] levels = new LogLevel[]{LogLevel.VERBOSE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARNING, LogLevel.ERROR};
        for(LogLevel l : levels){
            for(MsgBundle b : messages){
                if(b.level == l && b.level == tagLevel){
                    //Found it.
                    Log(tag, l, header, t);
                    return;
                }
            }
        }
    }
    
    /**
     * Logs all the applicable levels. This is useful if a message is always displayed, but progressively
     * more information is displayed the more verbose it gets.
     * @param tag
	 * @param t
     * @param messages 
     */
    public void LogAll(Tags tag, Target t, MsgBundle ... messages){
        if(GetLevel(tag) == LogLevel.OFF){
            return; //For efficiency sake, go ahead and bail.
        }
        for(MsgBundle b : messages){
            Log(tag, b.level, b.message, t);
        }
    }
    
    /**
     * Logs the given message at the level of ERROR/ON.
     * @param module
     * @param message 
	 * @param t 
     */
    public void Log(Tags module, String message, Target t){
        Log(module, LogLevel.ERROR, message, t);
    }
	
	/**
	 * Logs the given exception at the ERROR level.
	 * @param modules
	 * @param throwable
	 * @param t 
	 */
	public void e(Tags modules, Throwable throwable, Target t){
		Log(modules, LogLevel.ERROR, StackTraceUtils.GetStacktrace(throwable), t);
	}
	
	/**
	 * Logs the given message at the ERROR level.
	 * @param modules
	 * @param message
	 * @param t 
	 */
	public void e(Tags modules, String message, Target t){
		Log(modules, LogLevel.ERROR, message, t);
	}
	
	/**
	 * Logs the given message at the WARNING level.
	 * @param modules
	 * @param message
	 * @param t 
	 */
	public void w(Tags modules, String message, Target t){
		Log(modules, LogLevel.WARNING, message, t);
	}
	
	/**
	 * Logs the given message at the INFO level.
	 * @param modules
	 * @param message
	 * @param t 
	 */
	public void i(Tags modules, String message, Target t){
		Log(modules, LogLevel.INFO, message, t);
	}
	
	/**
	 * Logs the given message at the DEBUG level.
	 * @param modules
	 * @param message
	 * @param t 
	 */
	public void d(Tags modules, String message, Target t){
		Log(modules, LogLevel.DEBUG, message, t);
	}
	
	/**
	 * Logs the given message at the VERBOSE level.
	 * @param modules
	 * @param message
	 * @param t 
	 */
	public void v(Tags modules, String message, Target t){
		Log(modules, LogLevel.VERBOSE, message, t);
	}
    
	/**
	 * Equivalent to Log(modules, level, message, t, true);
	 * @param modules
	 * @param level
	 * @param message
	 * @param t
	 */
    public void Log(Tags modules, LogLevel level, String message, Target t){
		Log(modules, level, message, t, true);
	}
	
    /**
     * Logs the given message at the specified level.
     * @param modules
     * @param level
     * @param message 
	 * @param t 
	 * @param printScreen 
     */
    public void Log(Tags modules, LogLevel level, String message, Target t, boolean printScreen){
        LogLevel moduleLevel = GetLevel(modules);
        if(moduleLevel == LogLevel.OFF && !Prefs.ScreamErrors()){
            return; //Bail as quick as we can!
        }
        if(moduleLevel.level >= level.level || (moduleLevel == LogLevel.ERROR && Prefs.ScreamErrors())){
            //We want to do the log
            try{
                Static.LogDebug(root, "[" + Implementation.GetServerType().getBranding() + "][" + level.name() + "][" + modules.name() + "] " + message + (t!=Target.UNKNOWN?" "+t.toString():""), 
						level, printScreen);
            } catch(IOException e){
                //Well, shoot.
                if(level.level <= 1){
                    System.err.println("Was going to print information to the log, but instead, there was"
                            + " an IOException: ");
                    e.printStackTrace(System.err);
                }
            }
        }
    }
    
    
    
    public static class MsgBundle{
        private LogLevel level;
        private String message;
        public MsgBundle(LogLevel level, String message){
            this.level = level;
            this.message = message;
        }
    }
}
