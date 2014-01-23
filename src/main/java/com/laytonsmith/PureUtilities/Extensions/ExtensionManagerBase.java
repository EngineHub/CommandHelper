
package com.laytonsmith.PureUtilities.Extensions;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 * @param <T>
 */
public abstract class ExtensionManagerBase<T extends ExtensionTrackerBase> {
	protected Map<URL, T> extensions = new HashMap<>();
	protected final List<File> locations = new ArrayList<>();
	protected Logger logger = Logger.getLogger(ExtensionManagerBase.class.getName());;

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	protected void log(Level level, String line) {
		if (logger != null) {
			logger.log(level, line);
		}
	}
	
	protected void log(Level level, String line, Throwable t) {
		if (logger != null) {
			logger.log(level, line, t);
		}
	}
	
	/**
	 * Add a location to be scanned for extension points.
	 * @param file 
	 */
	public void addDiscoveryLocation(File file) {
		try {
			locations.add(file.getCanonicalFile());
		} catch (IOException ex) {
			log(Level.SEVERE, null, ex);
		}
	}
	
	/**
	 * Get the known trackers.
	 * @return 
	 */
	public Map<URL, T> getTrackers() {
		return extensions;
	}
	
	/**
	 * Process the given location for any jars. If the location is a jar, add it
	 * directly. If the location is a directory, look for jars in it.
	 *
	 * @param location file or directory
	 * @return
	 */
	protected List<File> getFiles(File location) {
		List<File> toProcess = new ArrayList<>();

		if (location.isDirectory()) {
			for (File f : location.listFiles()) {
				if (f.getName().endsWith(".jar")) {
					try {
						// Add the trimmed absolute path.
						toProcess.add(f.getCanonicalFile());
					} catch (IOException ex) {
						log(Level.SEVERE, "Could not get exact path for "
								+ f.getAbsolutePath(), ex);
					}
				}
			}
		} else if (location.getName().endsWith(".jar")) {
			try {
				// Add the trimmed absolute path.
				toProcess.add(location.getCanonicalFile());
			} catch (IOException ex) {
				log(Level.SEVERE, "Could not get exact path for "
						+ location.getAbsolutePath(), ex);
			}
		}

		return toProcess;
	}
	
	/**
	 * Cache extension files. On non-windows, this function leaves early, as 
	 * the whole purpose of this is to enable updating jar files while the
	 * process is still running.
	 * @param extCache Where the extensions will be cached to.
	 * @param generalCache Where annotation caches will be saved to.
	 * @param extraClasses Other classes to include when setting up the 
	 * temporary ClassDiscovery.
	 */
	public abstract void cache(File extCache, File generalCache, Class... extraClasses);
	
	/**
	 * Initializes the extension manager. This operation is not necessarily
	 * required, and must be guaranteed to not run more than once per
	 * ClassDiscovery object.
	 *
	 * @param cd the ClassDiscovery to use for loading files.
	 */
	public abstract void load(ClassDiscovery cd);
}
