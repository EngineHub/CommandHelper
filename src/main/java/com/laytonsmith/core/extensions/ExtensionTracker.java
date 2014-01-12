
package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.core.events.Event;
import com.laytonsmith.core.functions.Function;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension tracking and control class.
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class ExtensionTracker {
	private Extension extension;
	final DynamicClassLoader dcl;
	final ClassDiscovery cd;
	private final List<? extends Function> functions;
	private final List<? extends Event> events;
	final URL container;

	public ExtensionTracker(URL container, ClassDiscovery cd, DynamicClassLoader dcl) {
		this.functions = new ArrayList<Function>();
		this.events = new ArrayList<Event>();
		this.container = container;
		this.cd = cd;
		this.dcl = dcl;
	}

	public void shutdown() {
		cd.removeDiscoveryLocation(container);
		cd.removePreCache(container);
		dcl.removeJar(container);
	}

	/**
	 * @return the extension
	 */
	public Extension getExtension() {
		return extension;
	}

	/**
	 * @param extension the extension to set
	 */
	public void setExtension(Extension extension) {
		this.extension = extension;
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
}
