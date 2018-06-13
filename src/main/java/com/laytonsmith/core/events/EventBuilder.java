package com.laytonsmith.core.events;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.annotations.abstraction;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.exceptions.CRE.CREPluginInternalException;
import java.lang.reflect.Constructor;
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

	private static final Map<Class<BindableEvent>, Method> METHODS = new HashMap<Class<BindableEvent>, Method>();
	private static final Map<Class<BindableEvent>, Constructor<? extends BindableEvent>> CONSTRUCTORS =
			new HashMap<Class<BindableEvent>, Constructor<? extends BindableEvent>>();
	private static final Map<Class<BindableEvent>, Class<BindableEvent>> EVENT_IMPLEMENTATIONS =
			new HashMap<Class<BindableEvent>, Class<BindableEvent>>();

	static {
		// First, we need to pull all the event implementors.
		for(Class c : ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(abstraction.class)) {
			if(BindableEvent.class.isAssignableFrom(c)) {
				abstraction abs = (abstraction) c.getAnnotation(abstraction.class);
				if(abs.type().equals(Implementation.GetServerType())) {
					Class cInterface = null;
					Class bindableEventImplementorClass = c;
					matchLoop:
					do {

						// Get the interface, implemented by c, that extends from BindableEvent.
						for(Class implementor : bindableEventImplementorClass.getInterfaces()) {
							if(BindableEvent.class.isAssignableFrom(implementor)) {
								cInterface = implementor;
								break matchLoop;
							}
						}

						// No interface found, this means the class extends from an object that implements the
						// interface (in)directly. So retry with the class it extends from.
						bindableEventImplementorClass = bindableEventImplementorClass.getSuperclass();

						// Prevent infinite loop when a bug would be present in this code. This should never trigger.
						if(bindableEventImplementorClass.getClass().equals(Object.class)) {
							throw new InternalError("BindableEvent is assignable from some class c, but class c does"
									+ " not implement BindableEvent (in)directly. This is a bug in "
									+ EventBuilder.class.getSimpleName() + "'s static code block.");
						}

					} while(cInterface == null);

					// Add the interface implemented by c to the implementations map.
					EVENT_IMPLEMENTATIONS.put(cInterface, c);
				}
			}
		}
	}

	/**
	 * Finds the _instantiate method in an event, and caches it for later use.
	 * Does nothing if the _instantiate method for the given class is already cached.
	 * @param clazz
	 */
	private static void warmup(Class<? extends BindableEvent> clazz) {
		if(!METHODS.containsKey((Class<BindableEvent>) clazz)) {
			Class implementor = EVENT_IMPLEMENTATIONS.get((Class<BindableEvent>) clazz);
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
						+ " if an attempt is made. Did you forget to add"
						+ " public static <Event> _instantiate(...) to " + clazz.getSimpleName() + "?");
			}
			METHODS.put((Class<BindableEvent>) clazz, method);
		}
	}

	public static <T extends BindableEvent> T instantiate(Class<? extends BindableEvent> clazz, Object... params) {
		try {
			// Cache the _instantiate(...) method for clazz if it has not been cached yet.
			warmup(clazz);

			// Invoke the _instantiate(...) method to obtain a server software specific event object.
			Object o = METHODS.get((Class<BindableEvent>) clazz).invoke(null, params);

			//Now, we have an instance of the underlying object, which the instance
			//of the event BindableEvent should know how to handle in a constructor.
			if(!CONSTRUCTORS.containsKey((Class<BindableEvent>) clazz)) {
				Class bindableEvent = EVENT_IMPLEMENTATIONS.get((Class<BindableEvent>) clazz);
				Constructor constructor = null;
				for(Constructor c : bindableEvent.getConstructors()) {
					if(c.getParameterTypes().length == 1) {
						//looks promising
						if(c.getParameterTypes()[0].equals(o.getClass())) {
							//This is it
							constructor = c;
							break;
						}
					}
				}
				if(constructor == null) {
					throw new CREPluginInternalException("Cannot find an acceptable constructor that follows the format:"
							+ " public " + bindableEvent.getClass().getSimpleName() + "(" + o.getClass().getSimpleName() + " event)."
							+ " Please notify the plugin author of this error.", Target.UNKNOWN);
				}
				CONSTRUCTORS.put((Class<BindableEvent>) clazz, constructor);
			}
			//Construct a new instance, then return it.
			Constructor constructor = CONSTRUCTORS.get((Class<BindableEvent>) clazz);
			BindableEvent be = (BindableEvent) constructor.newInstance(o);
			return (T) be;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

//	public static MCPlayerJoinEvent MCPlayerJoinEvent(MCPlayer player, String message){
//		return instantiate(MCPlayerJoinEvent.class, player, message);
//	}
}
