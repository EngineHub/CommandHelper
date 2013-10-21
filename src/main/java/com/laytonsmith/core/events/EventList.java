

package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.Prefs;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author layton
 */
public final class EventList {
    
    private EventList(){}
    
    private static final Map<Driver, SortedSet<Event>> event_list =
            new EnumMap<Driver, SortedSet<Event>>(Driver.class);
    static {
        //Initialize all our events as soon as we start up
        initEvents();
    }
    
    /**
     * 
     * @return list of the names of all events, as returned by getName()
     */
    public static List<String> GetEvents() {
    	List<String> names = new ArrayList<String>();
    	for(Driver type : event_list.keySet()){
            SortedSet<Event> set = event_list.get(type);
            Iterator<Event> i = set.iterator();
            while(i.hasNext()){
                Event e = i.next();
                names.add(e.getName());
            }
        }
    	return names;
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
        Set<Class> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(api.class);
        int total = 0;
		StringBuilder message = new StringBuilder();
        for(Class c : classes){
            String apiClass = (c.getEnclosingClass() != null
                    ? c.getEnclosingClass().getName().split("\\.")[c.getEnclosingClass().getName().split("\\.").length - 1]
                    : "<global>");
            if (Event.class.isAssignableFrom(c)) {
                try {
                    registerEvent(c, apiClass, message);
                    total++;
                } catch (InstantiationException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(EventList.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if(!api.ValidClasses.IsValid(c)){
                System.out.println("Invalid class found: " + c.getName() + ". Classes tagged with @api"
                        + " must extend a valid class type.");
            }
        }
        
        if(Prefs.DebugMode()){
			System.out.println(Implementation.GetServerType().getBranding() + ": Loaded the following events: " + message.toString().trim());
            System.out.println(Implementation.GetServerType().getBranding() + ": Loaded " + total + " event" + (total==1?"":"s"));
        }
    }
    
    public static void registerEvent(Class<Event> c, String apiClass, StringBuilder message) throws InstantiationException, IllegalAccessException {
        //First, we need to instantiate the class
        
        Event e = c.newInstance();
        
        //Then, we need to find the mixin for this class, and set it, if this
        //is an AbstractEvent (if it implements Event directly, it's completely on
        //it's own anyways)
        if(e instanceof AbstractEvent){
            AbstractEvent ae = (AbstractEvent) e;
            //Get the mixin for this server, and add it to e
            Class mixinClass = StaticLayer.GetServerEventMixin();
            try{
                Constructor mixinConstructor = mixinClass.getConstructor(AbstractEvent.class);
                EventMixinInterface mixin = (EventMixinInterface) mixinConstructor.newInstance(e);
                ae.setAbstractEventMixin(mixin);
            } catch(Exception ex){
                //This is a serious problem, and it should kill the plugin, for fast failure detection.
                throw new Error("Could not properly instantiate the mixin class. "
                        + "The constructor with the signature \"public " + mixinClass.getSimpleName() + "(AbstractEvent e)\" is missing"
                        + " from " + mixinClass.getName());
            }
        }
        
        //Finally, add it to the list, and hook it.
        if(!event_list.containsKey(e.driver())){
            event_list.put(e.driver(), new TreeSet<Event>());
        }
        event_list.get(e.driver()).add(e);
        
        if(Prefs.DebugMode()){
            message.append(" \"").append(e.getName()).append("\"");
        }
    }
    
	/**
	 * This runs the hooks on all events. This should be called each time
	 * the server "starts up".
	 */
	public static void RunHooks(){
		for(SortedSet<Event> s : event_list.values()){
			for(Event e : s){
				try{
					e.hook();
				} catch(UnsupportedOperationException ex){}
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
