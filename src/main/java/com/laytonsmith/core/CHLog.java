package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.Preferences.Preference;
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
public class CHLog {
    private static String header = "The logger preferences allow you to granularly define what information\n"
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
    private static EnumMap<Tags, Level> lookup = new EnumMap<Tags, Level>(Tags.class);
    public enum Tags{
        COMPILER("compiler", "Logs compiler errors (but not runtime errors)", Level.ERROR),
        RUNTIME("runtime", "Logs runtime errors, (exceptions that bubble all the way to the top)", Level.ERROR),
        DEPRECATION("deprecation", "Shows deprecation warnings", Level.WARNING),
        PERSISTANCE("persistance", "Logs when any persistance actions occur.", Level.OFF),
        //TODO Add the rest of these hooks into the code
//        IO("IO", "Logs when the filesystem is accessed.", Level.OFF),
//        ALIAS("alias", "Logs use of user aliases.", Level.OFF),
//        EVENTS("events", "Logs bindings of an event.", Level.OFF),
//        PROCEDURES("procedures", "Logs when a procedure is created", Level.OFF),
//        INCLUDES("includes", "Logs what file is requested when include() is used", Level.OFF),
        GENERAL("general", "Anything that doesn't fit in a more specific category is logged here.", Level.ERROR)
        ;
        
        
        String name;
        String description;
        Level level;
        private Tags(String name, String description, Level defaultLevel){
            this.name = name;
            this.description = description;
            this.level = defaultLevel;
        }
    }
    
    public enum Level{
        OFF(0), ERROR(1), WARNING(2), INFO(3), DEBUG(4), VERBOSE(5);
        int level;
        Level(int i){
            level = i;
        }
    }
    
    static{
        initialize();
    }
    
    private static void initialize(){
        List<Preference> prefs = new ArrayList<Preference>();
        for(Tags t : Tags.values()){
            prefs.add(new Preference(t.name, t.level.name(), Preferences.Type.STRING, t.description));
        }
        CHLog.prefs = new Preferences("CommandHelper", Static.getLogger(), prefs, header);
        try{
            File file = new File(new File("plugins" + File.separator + "CommandHelper" + File.separator + Prefs.DebugLogFile()).getParentFile(), "loggerPreferences.txt");
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
    private static Level GetLevel(Tags tag){
        if(lookup.containsKey(tag)){
            return lookup.get(tag);
        }
        Level level;
        try{
            level = Level.valueOf((String)prefs.getPreference(tag.name));
        } catch(IllegalArgumentException e){
            level = Level.ERROR;
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
    public static boolean WillLog(Tags tag, Level l){
        Level level = GetLevel(tag);
        if(level == Level.OFF){
            return false;
        } else {
            return l.level <= level.level;
        }
        
    }
    
    
    /**
     * From the given MsgBundles, picks the most appropriate log level, tending towards more verbose,
     * and uses that message.
     * This is useful if a message would be different, not just more information, given a level.
     * For instance, given the following: LogOne(Tags.tag, new MsgBundle(Level.ERROR, "An error occured"),
     * new MsgBundle(Level.VERBOSE, "An error occured, and here is why")), if the level was set to ERROR, only "An error occured"
     * would show. If the level was set to VERBOSE, only "An error occured, and here is why" would show.
     * @param messages 
     */
    public static void LogOne(Tags tag, Target t, MsgBundle ... messages){
        if(GetLevel(tag) == Level.OFF){
            return; //Bail!
        }
        Level tagLevel = GetLevel(tag);
        Level [] levels = new Level[]{Level.VERBOSE, Level.DEBUG, Level.INFO, Level.WARNING, Level.ERROR};
        for(Level l : levels){
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
     * @param messages 
     */
    public static void LogAll(Tags tag, Target t, MsgBundle ... messages){
        if(GetLevel(tag) == Level.OFF){
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
     */
    public static void Log(Tags module, String message, Target t){
        Log(module, Level.ERROR, message, t);
    }
    
    /**
     * Logs the given message at the specified level.
     * @param modules
     * @param level
     * @param message 
     */
    public static void Log(Tags modules, Level level, String message, Target t){
        Level moduleLevel = GetLevel(modules);
        if(moduleLevel == Level.OFF){
            return; //Bail as quick as we can!
        }
        if(moduleLevel.level <= level.level){
            //We want to do the log
            try{
                Static.LogDebug(message + (t!=Target.UNKNOWN?" "+t.toString():""));
            } catch(IOException e){
                //Well, shoot.
                if(level.level <= 1){
                    System.err.println("Was going to print information to the log, but instead, there was"
                            + " an IOException: ");
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    
    public static class MsgBundle{
        private Level level;
        private String message;
        public MsgBundle(Level level, String message){
            this.level = level;
            this.message = message;
        }
    }
}
