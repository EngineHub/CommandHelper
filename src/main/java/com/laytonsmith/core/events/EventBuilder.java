package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 */
public final class EventBuilder {

	private EventBuilder() {
	}

	private static final Map<Class<BindableEvent>, Method> methods = new HashMap<Class<BindableEvent>, Method>();
	private static final Map<Class<BindableEvent>, Class<BindableEvent>> eventImplementations = new HashMap<Class<BindableEvent>, Class<BindableEvent>>();

	static {
		//First, we need to pull all the event implementors
		for(Class c : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(abstraction.class)) {
			if(BindableEvent.class.isAssignableFrom(c)) {
				abstraction abs = (abstraction) c.getAnnotation(abstraction.class);
				if(abs.type().equals(Implementation.GetServerType())) {
					Class cinterface = null;
					for(Class implementor : c.getInterfaces()) {
						if(BindableEvent.class.isAssignableFrom(implementor)) {
							cinterface = implementor;
							break;
						}
					}
					eventImplementations.put(cinterface, c);
				}
			}
		}
	}

	/**
	 * Finds the _instantiate method in an event, and caches it for later use.
	 *
	 * @param clazz
	 */
	private static void warmup(Class<? extends BindableEvent> clazz) {
		if(!methods.containsKey((Class<BindableEvent>) clazz)) {
			Class implementor = eventImplementations.get((Class<BindableEvent>) clazz);
			Method method = null;
			for(Method m : implementor.getMethods()) {
				if(m.getName().equals("_instantiate") && (m.getModifiers() & Modifier.STATIC) != 0) {
					method = m;
					break;
				}
			}
			if(method == null) {
				StreamUtils.GetSystemErr().println("UNABLE TO CACHE A CONSTRUCTOR FOR " + clazz.getSimpleName()
						+ ". Manual triggering will be impossible, and errors will occur"
						+ " if an attempt is made. Did someone forget to add"
						+ " public static <Event> _instantiate(...) to " + clazz.getSimpleName() + "?");
			}
			methods.put((Class<BindableEvent>) clazz, method);
		}
	}

	public static <T extends BindableEvent> T instantiate(Class<? extends BindableEvent> clazz, Object... params) {
		try {
			if(!methods.containsKey((Class<BindableEvent>) clazz)) {
				warmup(clazz);
			}
			return (T) methods.get((Class<BindableEvent>) clazz).invoke(null, params);

		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
