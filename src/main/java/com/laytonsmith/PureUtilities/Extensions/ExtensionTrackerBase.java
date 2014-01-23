
package com.laytonsmith.PureUtilities.Extensions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.PureUtilities.ClassLoading.DynamicClassLoader;
import com.laytonsmith.PureUtilities.Version;
import java.net.URL;

/**
 * Base class for extension trackers.
 * @author Jason Unger <entityreborn@gmail.com>
 */
public abstract class ExtensionTrackerBase {
	protected String identifier;
	protected Version version;
	protected final DynamicClassLoader dcl;
	protected final ClassDiscovery cd;
	protected final URL container;

	public ExtensionTrackerBase(String identifier, Version version, DynamicClassLoader dcl, ClassDiscovery cd, URL container) {
		this.identifier = identifier;
		this.version = version;
		this.dcl = dcl;
		this.cd = cd;
		this.container = container;
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

	/**
	 * Get the URL to the original file this tracker was populated from.
	 * @return 
	 */
	public URL getContainer() {
		return container;
	}
	
	/**
	 * Shut down this tracker. Any handles on it's classes will need to be
	 * released for a complete shutdown and release of memory resources.
	 */
	public void shutdownTracker() {
		// Remove as much as possible from memory.
		cd.removeDiscoveryLocation(container);
		cd.removePreCache(container);
		dcl.removeJar(container);
	}
}
