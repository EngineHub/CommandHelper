
package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.functions.Function;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extension tracking and control class.
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class ExtensionTracker {
	/* package */ String identifier;
	/* package */ Version version;
	/* package */ List<Extension> allExtensions;
	private final DynamicClassLoader dcl;
	private final ClassDiscovery cd;
	/* package */ final List<Function> functions;
	/* package */ final List<Event> events;
	/* package */ final URL container;

	public ExtensionTracker(URL container, ClassDiscovery cd, DynamicClassLoader dcl) {
		this.functions = new ArrayList<>();
		this.events = new ArrayList<>();
		this.allExtensions = new ArrayList<>();
		this.version = CHVersion.V0_0_0;
		
		this.container = container;
		this.cd = cd;
		this.dcl = dcl;
	}

	public void shutdownTracker() {
		// Remove as much as possible from memory.
		cd.removeDiscoveryLocation(container);
		cd.removePreCache(container);
		dcl.removeJar(container);
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
	public List<? extends Function> getFunctions() {
		return functions;
	}

	/**
	 * @return the events
	 */
	public List<? extends Event> getEvents() {
		return events;
	}

	/**
	 * Get the internal identifier for this tracker.
	 * @return 
	 */
	public String getIdentifier() {
		return identifier;
	}
	
	/**
	 * Get the internal version for this tracker.
	 * @return 
	 */
	public Version getVersion() {
		return version;
	}
}
