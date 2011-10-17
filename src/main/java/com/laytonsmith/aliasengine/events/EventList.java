/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.api;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class EventList {
    private static ArrayList<Event> event_handlers = new ArrayList<Event>();
    static {
        //Initialize all our events as soon as we start up
        initEvents();
    }
    
    
    private static void initEvents() {
        //Register internal classes first, so they can't be overridden
        Class[] classes = ClassDiscovery.DiscoverClasses(EventList.class, null, null);
        for (int i = 0; i < classes.length; i++) {
            Annotation[] a = classes[i].getAnnotations();
            for (int j = 0; j < a.length; j++) {
                Annotation ann = a[j];
                if (ann.annotationType().equals(api.class)) {
                    Class api = classes[i];
                    String apiClass = (api.getEnclosingClass() != null
                            ? api.getEnclosingClass().getName().split("\\.")[api.getEnclosingClass().getName().split("\\.").length - 1]
                            : "<global>");
                    if (Event.class.isAssignableFrom(api)) {
                        try {
                            Event e = (Event) api.newInstance();
                            registerEvent(e, apiClass);
                            //System.out.println("Loaded " + apiClass + "." + f.getName());
                        } catch (InstantiationException ex) {
                            Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        System.out.println("@api functions must implement " + EventList.class.getPackage().getName() + ".Event! " + api.getSimpleName() + " cannot be loaded.");
                    }
                }
            }
        }
        
        if((Boolean)com.laytonsmith.aliasengine.Static.getPreferences().getPreference("debug-mode")){
            System.out.println("CommandHelper: Loaded " + event_handlers.size() + " function" + (event_handlers.size()==1?"":"s"));
        }
    }
    
    public static void registerEvent(Event e, String apiClass) {
        if(!apiClass.equals("Sandbox")){
            if((Boolean)com.laytonsmith.aliasengine.Static.getPreferences().getPreference("debug-mode")){
                System.out.println("CommandHelper: Loaded function \"" + e.getName() + "\"");
            }
        }
        event_handlers.add(e);
    }
}
