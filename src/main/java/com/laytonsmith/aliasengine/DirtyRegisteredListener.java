/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Type;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.SimplePluginManager;

/**
 *
 * @author Layton
 */
public class DirtyRegisteredListener extends RegisteredListener {


    private final Listener listener;
    private final Event.Priority priority;
    private final Plugin plugin;
    private final EventExecutor executor;
    
    private static final int queueCapacity = 20;
    private static Queue<Event> cancelledEvents = new LinkedBlockingQueue<Event>(queueCapacity);

    public DirtyRegisteredListener(final Listener pluginListener, final EventExecutor eventExecutor, final Event.Priority eventPriority, final Plugin registeredPlugin) {
        super(pluginListener, eventExecutor, eventPriority, registeredPlugin);
        listener = pluginListener;
        priority = eventPriority;
        plugin = registeredPlugin;
        executor = eventExecutor;
    }
    
    public static void Repopulate() throws NoSuchFieldException, ClassCastException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException {
        //Go through the list of registered listeners, and inject our
        //our own poisoned DirtyRegisteredListeners in instead
        SimplePluginManager pm = (SimplePluginManager)AliasCore.parent.getServer().getPluginManager();
        Field fListener = SimplePluginManager.class.getDeclaredField("listeners");
        //set it to public
        fListener.setAccessible(true);        
        EnumMap<Event.Type, SortedSet<RegisteredListener>> listeners = 
                (EnumMap<Event.Type, SortedSet<RegisteredListener>>) fListener.get(pm);
        
        //Remove final from the listeners, so we can modify it
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(fListener, fListener.getModifiers() & ~Modifier.FINAL);
        
        Map<Event.Type, SortedSet<RegisteredListener>> newListeners = new EnumMap<Event.Type, SortedSet<RegisteredListener>>(Event.Type.class);

        
        //We need the comparator, so we can create a new listener map
        Field fComparator = SimplePluginManager.class.getDeclaredField("comparer");
        fComparator.setAccessible(true);
        Comparator<RegisteredListener> comparator = (Comparator<RegisteredListener>) fComparator.get(pm);
        
        //Ok, now we have the listeners, so lets loop through them, and shove them into our own newListener object, so that
        //we can replace the reference later, without modifying the existing variable, because it is currently being walked
        //through elsewhere in the code.
        
        boolean doReplace = false;
        
        Set<Map.Entry<Event.Type, SortedSet<RegisteredListener>>> entrySet = listeners.entrySet();
        Iterator i = entrySet.iterator();
        while(i.hasNext()){
            final Map.Entry<Event.Type, SortedSet<RegisteredListener>> mySet = (Map.Entry<Event.Type, SortedSet<RegisteredListener>>)i.next();
            Iterator k = mySet.getValue().iterator();
            SortedSet<RegisteredListener> rls = new TreeSet<RegisteredListener>(comparator);
            newListeners.put(mySet.getKey(), rls);
            while(k.hasNext()){
                final RegisteredListener rl = (RegisteredListener)k.next();
                if(!(rl instanceof DirtyRegisteredListener)){
                    doReplace = true;
                }
                rls.add(DirtyRegisteredListener.Generate(rl));
            }
        }
        
        if(doReplace){
            //Only replace it if we've made changes
            fListener.set(pm, newListeners);
        }
        
    }
    
    public static class MyEntry{
        public Type key;
        public DirtyRegisteredListener value;
    }
    
    public static void setCancelled(Event superCancelledEvent){
        if(cancelledEvents.size() >= queueCapacity){
            cancelledEvents.poll();
        }
        cancelledEvents.offer(superCancelledEvent);
    }

    public static DirtyRegisteredListener Generate(RegisteredListener real) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if(real instanceof DirtyRegisteredListener){
            return (DirtyRegisteredListener)real;
        }
        Field rListener = real.getClass().getDeclaredField("listener");
        rListener.setAccessible(true);
        Listener nListener = (Listener)rListener.get(real);
        
        Field rPriority = real.getClass().getDeclaredField("priority");
        rPriority.setAccessible(true);
        Event.Priority nPriority = (Event.Priority)rPriority.get(real);
        
        Field rPlugin = real.getClass().getDeclaredField("plugin");
        rPlugin.setAccessible(true);
        Plugin nPlugin = (Plugin)rPlugin.get(real);
        
        Field rExecutor = real.getClass().getDeclaredField("executor");
        rExecutor.setAccessible(true);
        EventExecutor nExecutor = (EventExecutor)rExecutor.get(real);
        
        return new DirtyRegisteredListener(nListener, nExecutor, nPriority, nPlugin);
    }
    
    /**
     * This is the magic method we need to override. When we call the event, if it
     * is "super cancelled", then we don't run it. Cancelled events are still run
     * if they aren't "super cancelled", which mirrors existing behavior.
     * @param event 
     */
    @Override
    public void callEvent(Event event){
        //If it isn't super cancelled, call it, even if it is cancelled
        if(!DirtyRegisteredListener.cancelledEvents.contains(event)){
            callEvent0(event);
        } else {
            //If it's a cancellable event, and this listener isn't Monitor priority, just return
            if(event instanceof Cancellable && this.priority != Event.Priority.Monitor){
                return;
            } else {
                callEvent0(event);
            }
        }
    }
    
    private void callEvent0(Event event){
        executor.execute(listener, event);
    }
}
