/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import com.laytonsmith.aliasengine.Constructs.CArray;
import com.laytonsmith.aliasengine.Constructs.CString;
import com.laytonsmith.aliasengine.Constructs.Construct;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.exceptions.CancelCommandException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.exceptions.EventException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author layton
 */
public class EventHandler {
    
    private static final Map<org.bukkit.event.Event.Type, SortedSet<BoundEvent>> event_handles =
            new EnumMap<org.bukkit.event.Event.Type, SortedSet<BoundEvent>>(org.bukkit.event.Event.Type.class);
    
    /**
     * Registers a BoundEvent.
     * @param b
     * @throws EventException 
     */
    public static void RegisterEvent(BoundEvent b) throws EventException{
        Event event = EventList.getEvent(b.getEventName());
        if(event == null){
            throw new EventException("The event type \"" + b.getEventName() + "\" could not be found.");
        }
        if(!event_handles.containsKey(event.driver())){
            event_handles.put(event.driver(), new TreeSet<BoundEvent>());
        }
        SortedSet<BoundEvent> set = event_handles.get(event.driver());
        set.add(b);
        try{
            event.bind();
        } catch(UnsupportedOperationException e){}
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
     * Unregisters all event handlers. Runs in O(n)
     */
    public static void UnregisterAll(String name){
        for(org.bukkit.event.Event.Type type : event_handles.keySet()){
            SortedSet<BoundEvent> set = event_handles.get(type);
            Iterator<BoundEvent> i = set.iterator();
            while(i.hasNext()){
                BoundEvent b = i.next();
                if(b.getEventObjName().equals(name)){
                    i.remove();
                    return;
                }
            }
        }
    }
    
    /**
     * This should be used in the case the plugin is disabled, or /reloadalises is run.
     */
    public static void UnregisterAll(){
        event_handles.clear();
    }
    
    /**
     * Returns all events driven by type. O(1).
     * @param type
     * @return 
     */
    public static SortedSet<BoundEvent> GetEvents(org.bukkit.event.Event.Type type){
        return event_handles.get(type);
    }
    
    /**
     * Triggers an event by name. The event name is the primary filter for this event, but
     * to increase event lookup efficiency, the driver is required. This will run in O(n),
     * where n is the number of bound events driven by type <code>type</code>.
     * @param type
     * @param e 
     */
    public static void TriggerListener(org.bukkit.event.Event.Type type, String eventName, Object e){
        SortedSet<BoundEvent> toRun = new TreeSet<BoundEvent>();
        //This is the Event driver
        Event driver = EventList.getEvent(type, eventName);
        //This is the set of bounded events of this driver type. 
        //We must now look through the bound events to see if they are
        //the eventName, and if so, we will also run the prefilter.
        SortedSet<BoundEvent> bounded = GetEvents(type);
        if(bounded != null){
            for(BoundEvent b : bounded){
                if(driver.getName().equals(eventName) && driver.matches(b.getPrefilter(), e)){
                    toRun.add(b);
                }
            }
        }
        
        for(BoundEvent b : toRun){
            //TODO: Priorities
            try{
                b.trigger(driver.evaluate(e));
            } catch(CancelCommandException ex){
                try{
                    driver.cancel(e);
                } catch(EventException eex){
                    //Ignore, but still break.
                }
                break;
            } catch(EventException ex){
                throw new ConfigRuntimeException(ex.getMessage(), null, 0, null);
            }
        }
    }

    public static Construct DumpEvents() {
        CArray ca = new CArray(0, null);
        for(org.bukkit.event.Event.Type type : event_handles.keySet()){
            SortedSet<BoundEvent> set = event_handles.get(type);
            Iterator<BoundEvent> i = set.iterator();
            while(i.hasNext()){
                BoundEvent b = i.next();
                ca.push(new CString(b.toString(), 0, null));
            }
        }
        return ca;
    }
}
