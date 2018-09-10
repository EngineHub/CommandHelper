package com.laytonsmith.core.extensions;

import com.methodscript.PureUtilities.ClassLoading.ClassDiscovery;
import com.methodscript.PureUtilities.ClassLoading.DynamicClassLoader;
import com.methodscript.PureUtilities.Version;
import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.events.AbstractEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.events.EventMixinInterface;
import com.laytonsmith.core.functions.FunctionBase;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Extension tracking and control class.
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class ExtensionTracker {

	/* package */ String identifier;
	/* package */ Version version;
	/* package */ List<Extension> allExtensions;
	private final DynamicClassLoader dcl;
	private final ClassDiscovery cd;
	/* package */ final Map<api.Platforms, Map<String, FunctionBase>> functions;
	/* package */ final Map<String, Set<api.Platforms>> supportedPlatforms;
	/* package */ final Map<Driver, Set<Event>> events;
	/* package */ final URL container;
	/* package */ boolean isShutdown;

	public ExtensionTracker(URL container, ClassDiscovery cd, DynamicClassLoader dcl) {
		functions = new EnumMap<>(api.Platforms.class);
		supportedPlatforms = new HashMap<>();

		for(api.Platforms p : api.Platforms.values()) {
			functions.put(p, new HashMap<String, FunctionBase>());
		}

		this.events = new EnumMap<>(Driver.class);
		this.allExtensions = new ArrayList<>();
		this.version = CHVersion.V0_0_0;

		this.container = container;
		this.cd = cd;
		this.dcl = dcl;
	}

	public void shutdownTracker() {
		// Remove as much as possible from memory.
		if(isShutdown) {
			return;
		}

		cd.removeDiscoveryLocation(container);
		cd.removePreCache(container);
		dcl.removeJar(container);

		isShutdown = true;
	}

	/**
	 * @return the extensions known to this tracker
	 */
	public List<Extension> getExtensions() {
		return Collections.unmodifiableList(allExtensions);
	}

	/**
	 * @return the functions
	 */
	public Set<FunctionBase> getFunctions() {
		Set<FunctionBase> retn = new HashSet<>();

		for(Map<String, FunctionBase> function : functions.values()) {
			retn.addAll(function.values());
		}

		return retn;
	}

	public void registerFunction(FunctionBase f) {
		api api = f.getClass().getAnnotation(api.class);
		api.Platforms[] platforms = api.platform();

		if(!api.enabled()) {
			return;
		}

		if(supportedPlatforms.get(f.getName()) == null) {
			supportedPlatforms.put(f.getName(), EnumSet.noneOf(api.Platforms.class));
		}

		supportedPlatforms.get(f.getName()).addAll(Arrays.asList(platforms));

		for(api.Platforms platform : platforms) {
			try {
				functions.get(platform).put(f.getName(), f);
			} catch (UnsupportedOperationException e) {
				//This function isn't done yet, and during production this is a serious problem,
				//but it will be caught when we test all the functions, so for now just ignore it,
				//since this function is called during initial initialization
			}
		}
	}

	/**
	 * @return the events
	 */
	public Set<Event> getEvents() {
		Set<Event> retn = new HashSet<>();
		for(Set<Event> set : events.values()) {
			retn.addAll(set);
		}
		return retn;
	}

	public Set<Event> getEvents(Driver type) {
		Set<Event> retn = events.get(type);

		if(retn == null) {
			return Collections.emptySet();
		}

		return retn;
	}

	public void registerEvent(Event e) {
		if(e instanceof AbstractEvent) {
			AbstractEvent ae = (AbstractEvent) e;
			//Get the mixin for this server, and add it to e
			Class mixinClass = StaticLayer.GetServerEventMixin();
			try {
				Constructor mixinConstructor = mixinClass.getConstructor(AbstractEvent.class);
				EventMixinInterface mixin = (EventMixinInterface) mixinConstructor.newInstance(e);
				ae.setAbstractEventMixin(mixin);
			} catch (Exception ex) {
				//This is a serious problem, and it should kill the plugin, for fast failure detection.
				throw new Error("Could not properly instantiate the mixin class. "
						+ "The constructor with the signature \"public " + mixinClass.getSimpleName() + "(AbstractEvent e)\" is missing"
						+ " from " + mixinClass.getName());
			}
		}

		//Finally, add it to the list, and hook it.
		if(!events.containsKey(e.driver())) {
			events.put(e.driver(), new TreeSet<Event>());
		}

		events.get(e.driver()).add(e);
	}

	/**
	 * Get the internal identifier for this tracker.
	 *
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Get the internal version for this tracker.
	 *
	 * @return
	 */
	public Version getVersion() {
		return version;
	}
}
