/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassDiscovery;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author layton
 */
public class EventList {
    private static final Map<Driver, SortedSet<Event>> event_list =
            new EnumMap<Driver, SortedSet<Event>>(Driver.class);
    static {
        //Initialize all our events as soon as we start up
        initEvents();
    }
    
    /**
     * Gets all the events of the specified type.
     * @param type
     * @return 
     */
    public static SortedSet<Event> GetEvents(Driver type){
        SortedSet<Event> set = event_list.get(type);
        return set;
    }
    
    /**
     * A more efficient lookup, this method will return a value in near constant time,
     * as opposed to the other getEvent, which will return in O(n) time. This could
     * return null if there is no event named name.
     */
    public static Event getEvent(Driver type, String name){
        if(type == null){
            return getEvent(name);
        }
        SortedSet<Event> set = event_list.get(type);
        if(set != null){
            Iterator<Event> i = set.iterator();
            while(i.hasNext()){
                Event e = i.next();
                if(e.getName().equals(name)){
                    return e;
                }
            }
        }
        return null;
    }
    /**
     * This could return null if there is no event named name.
     * @param name
     * @return 
     */
    public static Event getEvent(String name){
        for(Driver type : event_list.keySet()){
            SortedSet<Event> set = event_list.get(type);
            Iterator<Event> i = set.iterator();
            while(i.hasNext()){
                Event e = i.next();
                if(e.getName().equals(name)){
                    return e;
                }
            }
        }
        return null;
    }
    
    private static void initEvents() {
        //Register internal classes first, so they can't be overridden
        Class[] classes = ClassDiscovery.GetClassesWithAnnotation(abstraction.class);
        int total = 0;
        for(Class c : classes){
            String apiClass = (c.getEnclosingClass() != null
                    ? c.getEnclosingClass().getName().split("\\.")[c.getEnclosingClass().getName().split("\\.").length - 1]
                    : "<global>");
            if (EventHandlerInterface.class.isAssignableFrom(c)) {
                try {
                    registerEvent(c, apiClass);
                    total++;
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                System.out.println("@abstraction events must implement " + EventList.class.getPackage().getName() + ".Event! " + c.getSimpleName() + " cannot be loaded.");
            }            
        }
        
        if((Boolean)com.laytonsmith.core.Static.getPreferences().getPreference("debug-mode")){
            System.out.println("CommandHelper: Loaded " + total + " event" + (total==1?"":"s"));
        }
    }
    
    public static void registerEvent(Class c, String apiClass) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        abstraction a = (abstraction)c.getAnnotation(abstraction.class);
        if(a.type() == Implementation.GetServerType()){
            //First, instantiate the handler
            Class load = a.load();
            if(EventHandlerInterface.class.isAssignableFrom(c)){
                 if(Event.class.isAssignableFrom(load)){
                    EventHandlerInterface handler = (EventHandlerInterface) c.newInstance();
                    Constructor loaderConstructor = load.getConstructor(EventHandlerInterface.class);
                    AbstractEvent e = (AbstractEvent) loaderConstructor.newInstance(handler);                   
                    //Get the mixin for this server, and add it to e
                    Class mixinClass = StaticLayer.GetServerEventMixin();
                    Constructor mixinConstructor = mixinClass.getConstructor(AbstractEvent.class);
                    EventMixinInterface mixin = (EventMixinInterface) mixinConstructor.newInstance(e);
                    e.setAbstractEventMixin(mixin);
                    if(!apiClass.equals("Sandbox")){
                        if((Boolean)com.laytonsmith.core.Static.getPreferences().getPreference("debug-mode")){
                            System.out.println("CommandHelper: Loaded event \"" + e.getName() + "\"");
                        }
                    }
                    if(!event_list.containsKey(e.driver())){
                        event_list.put(e.driver(), new TreeSet<Event>());
                    }
                    event_list.get(e.driver()).add(e);
                    try{
                        e.hook();
                    } catch(UnsupportedOperationException ex){}
            
                 } else {
                     System.out.println("CommandHelper: Loaded event " + load.getSimpleName() + " must implement Event");
                 }
            } else {
                System.out.println("CommandHelper: Loaded event " + c.getSimpleName() + " must implement AbstractEventHandler");
            }
        }
    }
    

    
    
    /**
     * This should be called when the plugin starts up. It registers all server event listeners.
     * This should only be called once, in onEnable from the main plugin.
     */
    public static void Startup(CommandHelperPlugin chp){
        StaticLayer.Startup(chp);        
    }
}
