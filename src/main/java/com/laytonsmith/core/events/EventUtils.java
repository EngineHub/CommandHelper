package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.event;
import com.laytonsmith.core.constructs.CArray;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.BoundEvent.Priority;
import com.laytonsmith.core.exceptions.CRE.CREBindException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.EventException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.exceptions.PrefilterNonMatchException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public final class EventUtils {

	private EventUtils() {
	}

	private static final Map<Driver, SortedSet<BoundEvent>> event_handles
			= new EnumMap<Driver, SortedSet<BoundEvent>>(Driver.class);

	/**
	 * Registers a BoundEvent.
	 *
	 * @param b
	 * @throws EventException
	 */
	public static void RegisterEvent(BoundEvent b) throws EventException {
		Event event = EventList.getEvent(b.getEventName());
		if (event == null) {
			throw new EventException("The event type \"" + b.getEventName() + "\" could not be found.");
		}
		if (!event_handles.containsKey(event.driver())) {
			event_handles.put(event.driver(), new TreeSet<BoundEvent>());
		}
		//Check for duplicate IDs
		for (Set<BoundEvent> s : event_handles.values()) {
			for (BoundEvent bb : s) {
				if (bb.getId().equals(b.getId())) {
					throw ConfigRuntimeException.BuildException("Cannot have duplicate IDs defined."
							+ " (Tried to define an event handler with id \"" + b.getId() + "\" at " + b.getTarget() + ","
							+ " but it has already been defined at " + bb.getTarget() + ")",
							CREBindException.class, b.getTarget());
				}
			}
		}
		SortedSet<BoundEvent> set = event_handles.get(event.driver());
		set.add(b);
		event.bind(b);
	}

	/**
	 * Looks through all the events for an event with id <code>id</code>. Once
	 * found, removes it. If no event with that id is registered, nothing
	 * happens.
	 *
	 * @param id
	 */
	public static void UnregisterEvent(String id) {
		for (Driver type : event_handles.keySet()) {
			SortedSet<BoundEvent> set = event_handles.get(type);
			Iterator<BoundEvent> i = set.iterator();
			while (i.hasNext()) {
				BoundEvent b = i.next();
				if (b.getId().equals(id)) {
					b.getEventDriver().unbind(b);
					i.remove();
					return;
				}
			}
		}
	}

	/**
	 * Returns the BoundEvent, by id.
	 *
	 * @param id
	 * @return
	 */
	public static BoundEvent GetEventById(String id) {
		for (Driver type : event_handles.keySet()) {
			SortedSet<BoundEvent> set = event_handles.get(type);
			for (BoundEvent b : set) {
				if (b.getId().equals(id)) {
					return b;
				}
			}
		}
		return null;
	}

	/**
	 * Unregisters all event handlers. Runs in O(n)
	 */
	public static void UnregisterAll(String name) {
		for (Driver type : event_handles.keySet()) {
			SortedSet<BoundEvent> set = event_handles.get(type);
			Iterator<BoundEvent> i = set.iterator();
			while (i.hasNext()) {
				BoundEvent b = i.next();
				if (b.getEventObjName().equals(name)) {
					i.remove();
					return;
				}
			}
		}
	}

	/**
	 * This should be used in the case the plugin is disabled, or /reloadalises
	 * is run.
	 */
	public static void UnregisterAll() {
		event_handles.clear();
	}

	/**
	 * Returns all events driven by type. O(1).
	 *
	 * @param type
	 * @return
	 */
	public static SortedSet<BoundEvent> GetEvents(Driver type) {
		return event_handles.get(type);
	}

	public static void ManualTrigger(String eventName, CArray object, Target t, boolean serverWide) {
		for (Driver type : event_handles.keySet()) {
			SortedSet<BoundEvent> toRun = new TreeSet<>();
			SortedSet<BoundEvent> bounded = GetEvents(type);
			Event driver = EventList.getEvent(type, eventName);
			if (bounded != null) {
				for (BoundEvent b : bounded) {
					if (b.getEventName().equalsIgnoreCase(eventName)) {
						try {
							BindableEvent convertedEvent = null;
							try {
								convertedEvent = driver.convert(object, t);
							} catch (UnsupportedOperationException ex) {
								// The event will stay null, and be caught below
							}
							if(convertedEvent == null){
								throw ConfigRuntimeException.BuildException(eventName + " doesn't support the use of trigger() yet.", CREBindException.class, t);
							} else if (driver.matches(b.getPrefilter(), convertedEvent)) {
								toRun.add(b);
							}
						} catch (PrefilterNonMatchException ex) {
							//Not running this one
						}
					}
				}
			}
			//If it's not a serverwide event, or this event doesn't support external events.
			if (!toRun.isEmpty()) {
				if (!serverWide || !driver.supportsExternal()) {
					FireListeners(toRun, driver, driver.convert(object, t));
				} else {
					//It's serverwide, so we can just trigger it normally with the driver, and it should trickle back down to us
					driver.manualTrigger(driver.convert(object, t));
				}
			} else {
				//They have fired a non existant event
				ConfigRuntimeException.DoWarning(ConfigRuntimeException.CreateUncatchableException("Non existant event is being triggered: " + eventName, object.getTarget()));
			}
		}
	}

	/**
	 * Returns a set of events that should be triggered by this event.
	 *
	 * @param type
	 * @param eventName
	 * @param e
	 * @return
	 */
	public static SortedSet<BoundEvent> GetMatchingEvents(Driver type, String eventName, BindableEvent e, Event driver) {
		SortedSet<BoundEvent> toRun = new TreeSet<>();
		//This is the set of bounded events of this driver type.
		//We must now look through the bound events to see if they are
		//the eventName, and if so, we will also run the prefilter.
		SortedSet<BoundEvent> bounded = GetEvents(type);
		if (bounded != null) {
			//Wrap this in a new set, so we can safely iterate it with async threads
			bounded = new TreeSet<>(bounded);
			for (BoundEvent b : bounded) {
				try {
					boolean matches = false;
					try {
						matches = driver.matches(b.getPrefilter(), e);
					} catch (ConfigRuntimeException ex) {
						//This can happen in limited cases, but still needs to be
						//handled properly. This would happen if, for instance, a
						//prefilter was configured improperly with bad runtime data.
						//We use the environment from the bound event.
						ConfigRuntimeException.HandleUncaughtException(ex, b.getEnvironment());
					}
					if (b.getEventName().equals(eventName) && matches) {
						toRun.add(b);
					}
				} catch (PrefilterNonMatchException ex) {
					//Not running this one
				}
			}
		}
		return toRun;
	}

	/**
	 * Triggers an event by name. The event name is the primary filter for this
	 * event, but to increase event lookup efficiency, the driver is required.
	 * This will run in O(n), where n is the number of bound events driven by
	 * type <code>type</code>.
	 *
	 * @param type
	 * @param eventName
	 * @param e
	 */
	public static void TriggerListener(Driver type, String eventName, BindableEvent e) {
		Event driver = EventList.getEvent(type, eventName);
		if (driver == null) {
			throw ConfigRuntimeException.CreateUncatchableException("Tried to fire an unknown event: " + eventName, Target.UNKNOWN);
		} else {
			FireListeners(GetMatchingEvents(type, eventName, e, driver), driver, e);
		}
	}

	public static void FireListeners(SortedSet<BoundEvent> toRun, Event driver, BindableEvent e) {
		//Sort our event handlers by priorities
		BoundEvent.ActiveEvent activeEvent = new BoundEvent.ActiveEvent(e);
		for (BoundEvent b : toRun) {
			if (activeEvent.canReceive() || b.getPriority().equals(Priority.MONITOR)) {
				try {
					//We must re-set the active event's bound event and parsed event
					activeEvent.setBoundEvent(b);
					activeEvent.setParsedEvent(driver.evaluate(e));
					b.trigger(activeEvent);
				} catch (FunctionReturnException ex) {
					//We also know how to deal with this
				} catch (EventException ex) {
					throw ConfigRuntimeException.BuildException(ex.getMessage(), null, Target.UNKNOWN);
				} catch (ConfigRuntimeException ex) {
					//An exception has bubbled all the way up
					ConfigRuntimeException.HandleUncaughtException(ex, b.getEnvironment());
				}
			}
		}
		for (BoundEvent b : toRun) {
			activeEvent.setBoundEvent(b);
			if (activeEvent.isCancelled()) {
				activeEvent.executeCancelled();
			} else {
				activeEvent.executeTriggered();
			}
		}
	}

	public static Construct DumpEvents() {
		CArray ca = new CArray(Target.UNKNOWN);
		for (Driver type : event_handles.keySet()) {
			SortedSet<BoundEvent> set = event_handles.get(type);
			Iterator<BoundEvent> i = set.iterator();
			while (i.hasNext()) {
				BoundEvent b = i.next();
				ca.push(new CString(b.toString() + ":" + b.getFile() + ":" + b.getLineNum(), Target.UNKNOWN), Target.UNKNOWN);
			}
		}
		return ca;
	}

	/**
	 *
	 * @param mce
	 * @deprecated Use {@link #TriggerListener(com.laytonsmith.core.events.Driver, java.lang.String, com.laytonsmith.core.events.BindableEvent)} instead
	 */
	@Deprecated
	public static void TriggerExternal(BindableEvent mce) {
		for (Method m : ClassDiscovery.getDefaultInstance().loadMethodsWithAnnotation(event.class)) {
			Class<?>[] params = m.getParameterTypes();
			if (params.length != 1 || !BindableEvent.class.isAssignableFrom(params[0])) {
				Logger.getLogger(EventUtils.class.getName()).log(Level.SEVERE,
						"An event handler annotated with @{0} may only contain one parameter, which extends {1}",
						new Object[]{event.class.getSimpleName(), BindableEvent.class.getName()});
			} else {
				try {
					Object instance = null;

					if ((m.getModifiers() & Modifier.STATIC) == 0) {
						//It's not static, so we need an instance. Ideally we could skip
						//this step, but it's harder to enforce that across jars.
						//We could emit a warning, but the end user wouldn't know what
						//to do with that. However, if this step fails (no no-arg constructors
						//exist) we will be forced to fail.
						//
						// TODO: We could preprocess, as we are for lifecycles, and emit errors.
						try {
							instance = m.getDeclaringClass().newInstance();
						} catch (InstantiationException | IllegalAccessException e) {
							throw new RuntimeException("Could not instantiate the superclass " + m.getDeclaringClass().getName()
									+ ". There is no no-arg constructor present. Ideally however, the method " + m.getName()
									+ " would simply be static, which would decrease overhead in general. "
									+ " Note to the end user: This error is not a CommandHelper error,"
									+ " it is an error in the extension that provides the event handler for"
									+ " " + mce.getClass().getName() + ", and should be reported to the extension"
									+ " author.", e);
						}
					}

					m.invoke(instance, mce);
				} catch (IllegalAccessException ex) {
					Logger.getLogger(EventUtils.class.getName()).log(Level.SEVERE,
							"Illegal Access Exception while triggering"
							+ " an external event:", ex.getCause());
				} catch (IllegalArgumentException ex) {
					// If we do this, console gets spammed for hooks that don't apply for
					// the event being fired. Need to check if mce is instance of params[0].

					//Logger.getLogger(EventUtils.class.getName()).log(Level.SEVERE, null, ex);
				} catch (InvocationTargetException ex) {
					Logger.getLogger(EventUtils.class.getName()).log(Level.SEVERE,
							"Invocation Target Exception while triggering"
							+ " an external event:", ex.getCause());
				}
			}

		}
	}

	/**
	 * Verifies that the event name given is a valid event name. If not, an
	 * IllegalArgumentException is thrown.
	 *
	 * @param name The name of the event to validate
	 * @throws IllegalArgumentException if the event name is invalid
	 */
	public static void verifyEventName(String name) throws IllegalArgumentException {
		for (Event e : EventList.GetEvents()) {
			if (e.getName().equals(name)) {
				return;
			}
		}
		throw new IllegalArgumentException("No event named \"" + name + "\" was found.");
	}

}
