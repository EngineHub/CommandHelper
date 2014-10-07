

package com.laytonsmith.commandhelper;

import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

/**
 *
 * 
 */
public class BukkitDirtyRegisteredListener extends RegisteredListener {

    private final Listener listener;
    private final EventPriority priority;
    private final Plugin plugin;
    private final EventExecutor executor;
    private static final int queueCapacity = 20;
    private static Queue<Event> cancelledEvents = new LinkedBlockingQueue<Event>(queueCapacity);

    public BukkitDirtyRegisteredListener(final Listener pluginListener, final EventExecutor eventExecutor, final EventPriority eventPriority, final Plugin registeredPlugin,
            boolean ignoreCancelled) {
        super(pluginListener, eventExecutor, eventPriority, registeredPlugin, ignoreCancelled);
        listener = pluginListener;
        priority = eventPriority;
        plugin = registeredPlugin;
        executor = eventExecutor;
    }

    public static class DirtyEnumMap<K extends Enum<K>, V> extends EnumMap<K, V> {

        public DirtyEnumMap(Class<K> keyType) {
            super(keyType);
        }

        public DirtyEnumMap(EnumMap<K, ? extends V> m) {
            super(m);
        }

        public DirtyEnumMap(Map<K, ? extends V> m) {
            super(m);
        }
        
		@Override
        public V put(K key, V value) {
            if (!(value instanceof DirtyTreeSet) && value instanceof TreeSet) {
                return super.put(key, (V) DirtyTreeSet.GenerateDirtyTreeSet((TreeSet) value));
            } else {
                return super.put(key, value);
            }
            //return null;
        }
    }

    public static class DirtyTreeSet<E> extends TreeSet {

        public static DirtyTreeSet GenerateDirtyTreeSet(TreeSet ts) {
            DirtyTreeSet dts = new DirtyTreeSet(ts.comparator());            
            for (Object o : ts) {
                dts.add(o);
            }
            return dts;
        }
        
        public DirtyTreeSet(Comparator<? super E> comparator) {
            super(comparator);
        }

        @Override
        public boolean add(Object e) {
            if(!(e instanceof BukkitDirtyRegisteredListener) && e instanceof RegisteredListener){
                try {
                    return super.add(Generate((RegisteredListener)e));
                } catch (NoSuchFieldException ex) {
                    Logger.getLogger(BukkitDirtyRegisteredListener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(BukkitDirtyRegisteredListener.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(BukkitDirtyRegisteredListener.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                return super.add(e);
            }
            return false;
        }
                
    }

    public static void Repopulate() throws NoSuchFieldException, ClassCastException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException {
        ConfigRuntimeException.DoWarning(null, "Play-dirty mode is currently disabled until further notice. Disable play-dirty in your preferences file"
                + " to get rid of this message.", false);
        //Go through the list of registered listeners, and inject our
        //our own poisoned DirtyRegisteredListeners in instead
//        SimplePluginManager pm = (SimplePluginManager) AliasCore.parent.getServer().getPluginManager();
//        Field fListener = SimplePluginManager.class.getDeclaredField("listeners");
//        //set it to public
//        fListener.setAccessible(true);
//        EnumMap<Event.Type, SortedSet<RegisteredListener>> listeners =
//                (EnumMap<Event.Type, SortedSet<RegisteredListener>>) fListener.get(pm);
//
//        if (listeners instanceof DirtyEnumMap) {
//            return; //We don't need to bother with it, we've already injected our poisoned EnumMap,
//            //so further additions will go through that instead.
//        }
//        
//        //Remove final from the listeners, so we can modify it
//        Field modifiersField = Field.class.getDeclaredField("modifiers");
//        modifiersField.setAccessible(true);
//        modifiersField.setInt(fListener, fListener.getModifiers() & ~Modifier.FINAL);
//
//        Map<Event.Type, SortedSet<RegisteredListener>> newListeners = new DirtyEnumMap<Event.Type, SortedSet<RegisteredListener>>(Event.Type.class);
//
//        //We need the comparator, so we can create a new listener map
//        Field fComparator = SimplePluginManager.class.getDeclaredField("comparer");
//        fComparator.setAccessible(true);
//        Comparator<RegisteredListener> comparator = (Comparator<RegisteredListener>) fComparator.get(pm);
//
//        //Ok, now we have the listeners, so lets loop through them, and shove them into our own newListener object, so that
//        //we can replace the reference later, without modifying the existing variable, because it is currently being walked
//        //through elsewhere in the code.
//
//        boolean doReplace = false;
//
//        Set<Map.Entry<Event.Type, SortedSet<RegisteredListener>>> entrySet = listeners.entrySet();
//        Iterator i = entrySet.iterator();
//        while (i.hasNext()) {
//            final Map.Entry<Event.Type, SortedSet<RegisteredListener>> mySet = (Map.Entry<Event.Type, SortedSet<RegisteredListener>>) i.next();
//            Iterator k = mySet.getValue().iterator();
//            SortedSet<RegisteredListener> rls = new DirtyTreeSet<RegisteredListener>(comparator);
//            newListeners.put(mySet.getKey(), rls);
//            while (k.hasNext()) {
//                final RegisteredListener rl = (RegisteredListener) k.next();
//                if (!(rl instanceof BukkitDirtyRegisteredListener)) {
//                    doReplace = true;
//                }
//                rls.add(BukkitDirtyRegisteredListener.Generate(rl));
//            }
//        }
//
//        if (doReplace) {
//            //Only replace it if we've made changes
//            fListener.set(pm, newListeners);
//        }

    }

//    public static class MyEntry {
//
//        public Type key;
//        public DirtyRegisteredListener value;
//    }

    public static void setCancelled(Event superCancelledEvent) {
        if (cancelledEvents.size() >= queueCapacity) {
            cancelledEvents.poll();
        }
        cancelledEvents.offer(superCancelledEvent);
    }

    public static BukkitDirtyRegisteredListener Generate(RegisteredListener real) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (real instanceof BukkitDirtyRegisteredListener) {
            return (BukkitDirtyRegisteredListener) real;
        }
        Field rListener = real.getClass().getDeclaredField("listener");
        rListener.setAccessible(true);
        Listener nListener = (Listener) rListener.get(real);

        Field rPriority = real.getClass().getDeclaredField("priority");
        rPriority.setAccessible(true);
        EventPriority nPriority = (EventPriority) rPriority.get(real);

        Field rPlugin = real.getClass().getDeclaredField("plugin");
        rPlugin.setAccessible(true);
        Plugin nPlugin = (Plugin) rPlugin.get(real);

        Field rExecutor = real.getClass().getDeclaredField("executor");
        rExecutor.setAccessible(true);
        EventExecutor nExecutor = (EventExecutor) rExecutor.get(real);
        
        Field rIgnoreCancelled = real.getClass().getDeclaredField("ignoreCancelled");
        rIgnoreCancelled.setAccessible(true);
        boolean nIgnoreCancelled = rIgnoreCancelled.getBoolean(real);

        return new BukkitDirtyRegisteredListener(nListener, nExecutor, nPriority, nPlugin, nIgnoreCancelled);
    }

    /**
     * This is the magic method we need to override. When we call the event, if it
     * is "super cancelled", then we don't run it. Cancelled events are still run
     * if they aren't "super cancelled", which mirrors existing behavior.
     * @param event 
     */
    @Override
    public void callEvent(Event event) {
//        if (Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)) {
//            Debug.DoLog(event.getType(), 1, "Bukkit Event received: " + event.getType().name());
//        }
//        //If it isn't super cancelled, call it, even if it is cancelled
//        if (!BukkitDirtyRegisteredListener.cancelledEvents.contains(event)) {
//            if (Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)
//                    && Debug.EVENT_LOGGING_FILTER.contains(event.getType())) {
//                Debug.DoLog(event.getType(), 3, "\tEvent is not super cancelled, so triggering now");
//            }
//            callEvent0(event);
//        } else {
//            //If it's a cancellable event, and this listener isn't Monitor priority, just return
//            if (event instanceof Cancellable && this.priority != EventPriority.MONITOR) {
//                if (Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)
//                        && Debug.EVENT_LOGGING_FILTER.contains(event.getType())) {
//                    Debug.DoLog(event.getType(), 3, "\tEvent is being ignored, due to play-dirty mode rules");
//                }
//                return;
//            } else {
//                if (Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)
//                        && Debug.EVENT_LOGGING_FILTER.contains(event.getType())) {
//                    Debug.DoLog(event.getType(), 3, "\tEvent is super cancelled, but this listener is either monitor priority (Y/N:"
//                            + (this.priority == EventPriority.MONITOR ? "y" : "n") + " or it it is not cancellable (Y/N:"
//                            + (event instanceof Cancellable ? "n" : "y"));
//                }
//                callEvent0(event);
//            }
//        }
    }

    private void callEvent0(Event event) {
//        StopWatch stopWatch = null;
//        if (Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)
//                && Debug.EVENT_LOGGING_FILTER.contains(event.getType())) {
//            if (Debug.EVENT_LOGGING_LEVEL >= 1) {
//                Debug.DoLog(event.getType(), 1, "\tEvent type: " + event.getType().name());
//                Debug.DoLog(event.getType(), 1, "\tCalled from plugin: " + this.plugin.getClass().getSimpleName());
//            }
//            if (Debug.EVENT_LOGGING_LEVEL >= 2) {
//                Debug.DoLog(event.getType(), 1, "\tListener Registered: " + this.listener.getClass().getCanonicalName());
//                Debug.DoLog(event.getType(), 2, "\tIs Cancellable? " + (event instanceof Cancellable ? "Y" : "N"));
//                if (event instanceof Cancellable) {
//                    Debug.DoLog(event.getType(), 2, "\t\tIs Cancelled? " + (((Cancellable) event).isCancelled() ? "Y" : "N"));
//                }
//            }
//            if (Debug.EVENT_LOGGING_LEVEL >= 3) {
//                Debug.DoLog(event.getType(), 3, "\tEvent class: " + event.getClass().getCanonicalName());
//            }
//            if (Debug.EVENT_LOGGING_LEVEL >= 4) {
//                //Let's just dump the fields
//                StringBuilder b = new StringBuilder("\n\tFields in this event:\n");
//                for (Field f : event.getClass().getSuperclass().getDeclaredFields()) {
//                    b.append("\t\t").append(f.getType().getSimpleName()).append(" ").append(f.getName());
//                    f.setAccessible(true);
//                    try {
//                        Object o = f.get(event);
//                        b.append(" = (actual type: ").append(o.getClass().getSimpleName()).append(") ").append(o.toString()).append("\n");
//                    } catch (IllegalArgumentException ex) {
//                        Logger.getLogger(BukkitDirtyRegisteredListener.class.getName()).log(Level.SEVERE, null, ex);
//                    } catch (IllegalAccessException ex) {
//                        Logger.getLogger(BukkitDirtyRegisteredListener.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                Debug.DoLog(event.getType(), 4, b.toString());
//            }
//            if (Debug.EVENT_LOGGING_LEVEL == 5) {
//                //dump ALL the things
//                StringBuilder b = new StringBuilder("\n\tMethods in this event:\n");
//                for (Method m : event.getClass().getSuperclass().getDeclaredMethods()) {
//                    b.append("\t\t").append(m.getReturnType().getSimpleName()).append(" ").append(m.getName()).append("(").append(Static.strJoin(m.getParameterTypes(), ", ")).append(");\n");
//                }
//                Debug.DoLog(event.getType(), 5, b.toString());
//            }
//        }
//        if ((Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)
//                && Debug.EVENT_LOGGING_FILTER.contains(event.getType()) && Debug.EVENT_LOGGING_LEVEL >= 2) || Performance.PERFORMANCE_LOGGING) {
//            stopWatch = new StopWatch(
//                    this.plugin.getClass().getSimpleName() + "."//Plugin name
//                    + this.listener.getClass().getCanonicalName().replaceAll("\\.", "/") + "." //File event is being called from
//                    + (event.getType() == Event.Type.CUSTOM_EVENT ? "CUSTOM_EVENT/" + event.getEventName() : event.getType().name()) //Event name
//                    );
//        }
//        try{
//            executor.execute(listener, event);
//        } catch(EventException e){
//            Logger.getLogger(BukkitDirtyRegisteredListener.class.getName()).log(Level.SEVERE, e.getMessage(), e);
//        }
//        if (stopWatch != null) {
//            stopWatch.stop();
//            if (Debug.EVENT_LOGGING) {
//                Debug.DoLog(event.getType(), 2, "\t\t\tEvent completed in " + stopWatch.getElapsedTime() + " milliseconds");
//            }
//            if (Performance.PERFORMANCE_LOGGING) {
//                Performance.DoLog(stopWatch);
//            }
//        }
//        if (Debug.EVENT_LOGGING && Debug.IsFiltered(plugin)) {
//            Debug.DoLog(event.getType(), 1, "--------------------------------------------------------------\n");
//        }
    }

	/**
	 * Sets up CommandHelper to play-dirty, if the user has specified as such
	 */
	public static void PlayDirty() {
		if (Prefs.PlayDirty()) {
			try {
				//Set up our "proxy"
				BukkitDirtyRegisteredListener.Repopulate();
			} catch (NoSuchMethodException ex) {
				Logger.getLogger(Static.class.getName()).log(Level.SEVERE, null, ex);
			} catch (NoSuchFieldException | ClassCastException | IllegalArgumentException | IllegalAccessException ex) {
				Static.getLogger().log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
			}
		} //else play nice :(
	}
}
