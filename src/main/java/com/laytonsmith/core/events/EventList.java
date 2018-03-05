package com.laytonsmith.core.events;

import com.laytonsmith.core.extensions.ExtensionManager;
import java.util.Set;

/**
 *
 *
 */
public final class EventList {

	private EventList() {
	}

	/**
	 *
	 * @return list of the names of all events, as returned by getName()
	 */
	public static Set<Event> GetEvents() {
		return ExtensionManager.GetEvents();
	}

	/**
	 * Gets all the events of the specified type.
	 *
	 * @param type
	 * @return
	 */
	public static Set<Event> GetEvents(Driver type) {
		return ExtensionManager.GetEvents(type);
	}

	/**
	 * A more efficient lookup, this method will return a value in near constant time, as opposed to the other getEvent,
	 * which will return in O(n) time. This could return null if there is no event named name.
	 */
	public static Event getEvent(Driver type, String name) {
		return ExtensionManager.GetEvent(type, name);
	}

	/**
	 * This could return null if there is no event named name.
	 *
	 * @param name
	 * @return
	 */
	public static Event getEvent(String name) {
		return ExtensionManager.GetEvent(name);
	}
}
