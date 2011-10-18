/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.functions.exceptions.EventException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.omg.CORBA.ORB;

/**
 *
 * @author layton
 */
public class EventHandler {
    private static int EventID = 0;
    public static int GetUniqueID(){
        return ++EventID;
    }
    
    private static final Map<org.bukkit.event.Event.Type, SortedSet<BoundEvent>> event_handles =
            new EnumMap<org.bukkit.event.Event.Type, SortedSet<BoundEvent>>(org.bukkit.event.Event.Type.class);
    
    /**
     * Registers a BoundEvent.
     * @param b
     * @throws EventException 
     */
    public static void RegisterEvent(BoundEvent b) throws EventException{
        Event event = EventList.getEvent(b.getEventObjName());
        if(event == null){
            throw new EventException("The event type \"" + b.getEventObjName() + "\" could not be found.");
        }
        if(!event_handles.containsKey(event.driver())){
            event_handles.put(event.driver(), new TreeSet<BoundEvent>());
        }
        event_handles.get(event.driver()).add(b);
    }
    
    /**
     * Looks through all the events for an event with id <code>id</code>. Once found, removes it.
     * If no event with that id is registered, nothing happens.
     * @param id 
     */
    public static void UnregisterEvent(String id){
        for(org.bukkit.event.Event.Type type : event_handles.keySet()){
            SortedSet<BoundEvent> set = event_handles.get(type);
            Iterator<BoundEvent> i = set.iterator();
            while(i.hasNext()){
                BoundEvent b = i.next();
                if(b.getId().equals(id)){
                    i.remove();
                    return;
                }
            }
        }
    }
    
    /**
     * Unregisters all event handlers
     */
    public static void UnregisterAll(){
        event_handles.clear();
    }
    
    /**
     * This runs in constant time, so hundreds of lookups shouldn't slow this down
     * at all.
     * @param type
     * @return 
     */
    public static SortedSet<BoundEvent> GetEvents(org.bukkit.event.Event.Type type){
        return event_handles.get(type);
    }
    
    public static void TriggerListener(org.bukkit.event.Event.Type type, org.bukkit.event.Event e){
        SortedSet<BoundEvent> bounded = GetEvents(type);
        SortedSet<BoundEvent> toRun = new TreeSet<BoundEvent>();
        for(BoundEvent b : bounded){
            Event driver = EventList.getEvent(b.getDriver(), b.getEventObjName());
            if(driver.matches(b.getPrefilter(), e)){
                toRun.add(b);
            }
        }
        
        for(BoundEvent b : toRun){
            
        }
    }
}
