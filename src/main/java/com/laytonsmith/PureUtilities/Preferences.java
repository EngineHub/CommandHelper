
package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows an application to more easily manage user preferences. As
 * an application grows, more preferences will likely be added, but if the application
 * uses flat file storage, managing these preferences while adding new preferences
 * can be difficult. This class manages, documents, and provides default values, all the
 * while not interfering with changes the user has made, meaning that you are free
 * to add new preferences, or change default values, without fear of changing
 * values that the user has specifically set. For sample usage, see
 * https://gist.github.com/1042094
 * @author Layton Smith
 */
public class Preferences {
    private final Map<String, Preference> prefs = new HashMap<String, Preference>();
    private final String appName;
    private final Logger logger;
    
    private File prefFile;
    
    private String header = "";
    
    /**
     * The type a particular preference can be. The value will be cast to the given type
     * if possible. NUMBER and DOUBLE are guaranteed to be castable to a Double. NUMBER 
     * can also sometimes be cast to an int. BOOLEAN is cast to a boolean, and may be stored
     * in the preferences file as either true/false, yes/no, on/off, or a number, which
     * get parsed accordingly. STRING can be any value.
     */
    public enum Type{
        NUMBER, BOOLEAN, STRING, INT, DOUBLE
    }
    
    /**
     * An object corresponding to a single preference
     */
    public static class Preference{
        /**
         * The name of the preference
         */
        public String name;
        /**
         * The value of the preference, as a string
         */
        public String value;
        /**
         * The allowed type of this value
         */
        public Type allowed;
        /**
         * The description of this preference. Used to write out to file.
         */
        public String description;

        /**
         * The object representation of this value. Should not be used
         * directly.
         */
        public Object objectValue;
        
        public Preference(String name, String def, Type allowed, String description) {
            this.name = name;
            this.value = def;
            this.allowed = allowed;
            this.description = description;			
        }
    }
    
    /**
     * Provide the name of the app, and logger, for recording errors, and a list
     * of defaults, in case the value is not provided by the user, or an invalid
     * value is provided. It also writes a custom header at the top of the file.
     * Newlines are supported, but only \n
     */
    public Preferences(String appName, Logger logger, List<Preference> defaults, String header){
        this.appName = appName;
        this.logger = logger;
        for(Preference p : defaults){
            prefs.put(p.name, p);
        }
        if(!header.trim().isEmpty()){
            this.header = "#  " + header.replaceAll("\n", "\n#  ");
        }
    }
    
    /**
     * Provide the name of the app, and logger, for recording errors, and a list
     * of defaults, in case the value is not provided by the user, or an invalid
     * value is provided. 
     */
    public Preferences(String appName, Logger logger, List<Preference> defaults){
        this(appName, logger, defaults, "");
    }

    /**
     * Given a file that the preferences are supposedly stored in, this
     * function will try to load the preferences. If the preferences don't exist,
     * or they are incomplete, this will also fill in the missing values, and 
     * store the now complete preferences in the file location specified.
     * @param prefFile
     * @throws Exception 
     */
    public void init(File prefFile) throws IOException {
        this.prefFile = prefFile;
        if(prefFile != null && prefFile.exists()){
            Properties userProperties = new Properties();
            FileInputStream in = new FileInputStream(prefFile);
            userProperties.load(in);
            in.close();
            for(String key : userProperties.stringPropertyNames()){
                String val = userProperties.getProperty(key);
                String value = getObject(val, ((Preference)prefs.get(key))).toString();
                Object ovalue = getObject(val, ((Preference)prefs.get(key)));
                Preference p1 = prefs.get(key);
                Preference p2;
                if(p1 != null){
                    p2 = new Preference(p1.name, value, p1.allowed, p1.description);
                } else {
                    p2 = new Preference(key, val, Type.STRING, "");
                }
                p2.objectValue = ovalue;
                prefs.put(key, p2);
            }
        }
        save();
    }
    
    private Object getObject(String value, Preference p){
        if(p == null){
            return value;
        }
        if(value.equalsIgnoreCase("null")){
            return getObject(p.value, p);
        }
        switch(p.allowed){
            case INT:
                try{
                    return Integer.parseInt(value);
                } catch (NumberFormatException e){
                    logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be an integer. Using the default of " + p.value);
                    return Integer.parseInt(p.value);
                }
            case DOUBLE:
                try{
                    return Double.parseDouble(value);
                } catch (NumberFormatException e){
                    logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be an double. Using the default of " + p.value);
                    return Double.parseDouble(p.value);
                }
            case BOOLEAN:
                try{
                    return getBoolean(value);
                } catch (NumberFormatException e){
                    logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be an boolean. Using the default of " + p.value);
                    return getBoolean(p.value);
                }
            case NUMBER:
                try{
                    return Integer.parseInt(value);
                } catch(NumberFormatException e){
                    try{
                        return Double.parseDouble(value);
                    } catch(NumberFormatException f){
                        logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be a number. Using the default of " + p.value);
                        try{
                            return Integer.parseInt(p.value);
                        } catch(NumberFormatException g){
                            return Double.parseDouble(p.value);
                        }
                    }
                }
            case STRING:
            default:
                return value;
        }
        
    }
    
    private Boolean getBoolean(String value){
        if(value.equalsIgnoreCase("true")){
            return true;
        } else if(value.equalsIgnoreCase("false")){
            return false;
        } else if(value.equalsIgnoreCase("yes")){
            return true;
        } else if(value.equalsIgnoreCase("no")){
            return false;
        } else if(value.equalsIgnoreCase("on")){
            return true;
        } else if(value.equalsIgnoreCase("off")){
            return false;
        } else {
            double d = Double.parseDouble(value);
            if(d == 0){
                return false;
            } else {
                return true;
            }
        }
    }
    
    /**
     * Returns the value of a preference, cast to the appropriate type.
     * @param name
     * @return 
     */
    public Object getPreference(String name){
        if(prefs.get(name).objectValue == null){
            prefs.get(name).objectValue = getObject(prefs.get(name).value, prefs.get(name));
        }
        return prefs.get(name).objectValue;
    }
    
    private void save(){
        try {
            StringBuilder b = new StringBuilder();
            String nl = System.getProperty("line.separator");
            
            b.append("#This file is generated automatically. Changes made to the values of this file")
                    .append(nl)
                    .append("#will persist, but changes to comments will not.")
                    .append(nl).append(nl);
            if(!header.trim().isEmpty()){
                b.append(header).append(nl).append(nl);
            }
			for (Map.Entry<String, Preference> e : prefs.entrySet()) {
				Preference p = e.getValue();
				String description = "This value is not used in " + appName;
				if(!p.description.trim().isEmpty()){
					description = p.description;
				}
				StringBuilder c = new StringBuilder();
				boolean first = true;
				for(String line : description.split("\n|\r\n|\n\r")){
					if(first){
						c.append("#").append(line);
						first = false;
					} else {
						c.append(nl).append("#").append(line);
					}
				}
				b.append(c).append(nl).append(p.name).append("=").append(p.value).append(nl).append(nl);
			}
            if(prefFile != null && !prefFile.exists()){
                prefFile.getAbsoluteFile().getParentFile().mkdirs();
                prefFile.createNewFile();
            }
			if(prefFile != null){
				FileUtil.write(b.toString(), prefFile);
			}
        } catch (Exception ex) {
            logger.log(Level.WARNING, "[" + appName + "] Could not write out preferences file: " + (prefFile!=null?prefFile.getAbsolutePath():"null"), ex);
        }
    }
    
}
