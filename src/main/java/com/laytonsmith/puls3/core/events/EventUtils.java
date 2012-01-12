/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.puls3.core.events;

import com.laytonsmith.puls3.core.constructs.CArray;
import com.laytonsmith.puls3.core.constructs.CString;
import com.laytonsmith.puls3.core.constructs.Construct;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.puls3.core.exceptions.EventException;
import com.laytonsmith.puls3.core.exceptions.PrefilterNonMatchException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author layton
 */
public class EventUtils {
    
    private static final Map<Driver, SortedSet<BoundEvent>> event_handles =
            new EnumMap<Driver, SortedSet<BoundEvent>>(Driver.class);
    
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
        for(Driver type : event_handles.keySet()){
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
        for(Driver type : event_handles.keySet()){
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
    public static SortedSet<BoundEvent> GetEvents(Driver type){
        return event_handles.get(type);
    }
    
    public static void ManualTrigger(String eventName, CArray object, boolean serverWide){
            for(Driver type : event_handles.keySet()){
                SortedSet<BoundEvent> toRun = new TreeSet<BoundEvent>();
                SortedSet<BoundEvent> bounded = GetEvents(type);
                Event driver = EventList.getEvent(type, eventName);
                if(bounded != null){
                    for(BoundEvent b : bounded){
                        try {
                            if(b.getEventName().equalsIgnoreCase(eventName) && driver.matches(b.getPrefilter(), driver.convert(object))){
                                toRun.add(b);
                            }
                        } catch (PrefilterNonMatchException ex) {
                            //Not running this one
                        }
                    }
                }
                //If it's not a serverwide event, or this event doesn't support external events.
                if(!toRun.isEmpty()){
                    if(!serverWide || !driver.supportsExternal()){
                        FireListeners(toRun, driver, driver.convert(object));
                    } else {
                        //It's serverwide, so we can just trigger it normally with the driver, and it should trickle back down to us
                        driver.manualTrigger(driver.convert(object));
                    }
                }
            }
    }
    
    /**
     * Triggers an event by name. The event name is the primary filter for this event, but
     * to increase event lookup efficiency, the driver is required. This will run in O(n),
     * where n is the number of bound events driven by type <code>type</code>.
     * @param type
     * @param e 
     */
    public static void TriggerListener(Driver type, String eventName, Object e){
        SortedSet<BoundEvent> toRun = new TreeSet<BoundEvent>();
        //This is the Event driver
        Event driver = EventList.getEvent(type, eventName);
        //This is the set of bounded events of this driver type. 
        //We must now look through the bound events to see if they are
        //the eventName, and if so, we will also run the prefilter.
        SortedSet<BoundEvent> bounded = GetEvents(type);
        if(bounded != null){
            for(BoundEvent b : bounded){
                try {
                    if(driver.getName().equals(eventName) && driver.matches(b.getPrefilter(), e)){
                        toRun.add(b);
                    }
                } catch (PrefilterNonMatchException ex) {
                    //Not running this one
                }
            }
        }
        
        FireListeners(toRun, driver, e);
    }
    
    private static void FireListeners(SortedSet<BoundEvent> toRun, Event driver, Object e){
        for(BoundEvent b : toRun){
            //TODO: Priorities
            try{                
                b.trigger(e, driver.evaluate(e));            
            } catch(EventException ex){
                throw new ConfigRuntimeException(ex.getMessage(), null, 0, null);
            }
        }
    }

    public static Construct DumpEvents() {
        CArray ca = new CArray(0, null);
        for(Driver type : event_handles.keySet()){
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
