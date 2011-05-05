/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Layton
 */
public class Preferences {
    private final static Map<String, Preference> prefs = new HashMap<String, Preference>();
    private final String appName;
    private final Logger logger;
    
    private File prefFile;
    
    /**
     * The type a particular preference can be. The value will be cast to the given type
     * if possible. NUMBER and DOUBLE are guaranteed to be castable to a Double. NUMBER 
     * can also sometimes be cast to an int. 
     */
    public enum Type{
        NUMBER, BOOLEAN, STRING, INT, DOUBLE
    }
    
    public static class Preference{
        public String name;
        public String value;
        public Type allowed;
        public String description;

        public Object objectValue;
        
        public Preference(String name, String def, Type allowed, String description) {
            this.name = name;
            this.value = def;
            this.allowed = allowed;
            this.description = description;
        }
    }
    
    /**
     * Empty constructor
     */
    public Preferences(String appName, Logger logger, ArrayList<Preference> defaults){
        this.appName = appName;
        this.logger = logger;
        for(Preference p : defaults){
            prefs.put(p.name, p);
        }
    }

    public void init(File prefFile) throws Exception {
        this.prefFile = prefFile;
        if(prefFile.exists()){
            Properties userProperties = new Properties();
            FileInputStream in = new FileInputStream(prefFile);
            userProperties.load(in);
            in.close();
            for(String key : userProperties.stringPropertyNames()){
                String val = userProperties.getProperty(key);
                String value = getObject(val, ((Preference)prefs.get(key))).toString();
                Object ovalue = getObject(val, ((Preference)prefs.get(key)));
                Preference p1 = prefs.get(key);
                Preference p2 = new Preference(p1.name, value, p1.allowed, p1.description);
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
                    logger.log(Level.WARNING, "[{0}] expects the value of {1} to be an integer. Using the default of {2}", new Object[]{appName, p.name, p.value});
                    return Integer.parseInt(p.value);
                }
            case DOUBLE:
                try{
                    return Double.parseDouble(value);
                } catch (NumberFormatException e){
                    logger.log(Level.WARNING, "[{0}] expects the value of {1} to be a double. Using the default of {2}", new Object[]{appName, p.name, p.value});
                    return Double.parseDouble(p.value);
                }
            case BOOLEAN:
                try{
                    return getBoolean(value);
                } catch (NumberFormatException e){
                    logger.log(Level.WARNING, "[{0}] expects the value of {1} to be a boolean. Using the default of {2}", new Object[]{appName, p.name, p.value});
                    return getBoolean(p.value);
                }
            case NUMBER:
                try{
                    return Integer.parseInt(value);
                } catch(NumberFormatException e){
                    try{
                        return Double.parseDouble(value);
                    } catch(NumberFormatException f){
                        logger.log(Level.WARNING, "[{0}] expects the value of {1} to be a number. Using the default of {2}", new Object[]{appName, p.name, p.value});
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
        return prefs.get(name).objectValue;
    }
    
    private void save(){
        try {
            StringBuilder b = new StringBuilder();
            Iterator it = prefs.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, Preference> e = (Map.Entry<String, Preference>) it.next();
                Preference p = e.getValue();
                String nl = System.getProperty("line.separator");
                b.append("#").append(p.description).append(nl).append(p.name).append("=").append(p.value).append(nl).append(nl);
            }
            BufferedWriter out = null;
            out = new BufferedWriter(new FileWriter(prefFile.getAbsolutePath()));
            out.write(b.toString());
            out.close();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "[{0}] Could not write out preferences file", appName);
        }
    }
    
}
