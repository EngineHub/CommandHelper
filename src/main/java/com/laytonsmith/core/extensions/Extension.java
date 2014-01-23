package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.Version;
import java.io.File;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface Extension {

	/**
	 * Create and return a valid data directory for this extension's use.
	 * @return
	 */
	File getConfigDir();

	// Identity functions
	/**
	 * Return the identity of this extension.
	 * @return
	 */
	String getName();
	
	/**
	 * Return the version for this extension.
	 * @return 
	 */
	Version getVersion();

	// Lifetime functions
	/**
	 * Called when server is loading, or during a /reloadaliases call.
	 */
	void onStartup();
	
    /**
	 * Called after the logic in /reloadaliases is called. Won't be called
	 * if /reloadaliases's help function is called.
	 */
	void onPostReloadAliases();

	/**
	 * Called just before the logic in /reloadaliases is called. Won't be called
	 * if /reloadaliases's help function is called.
	 *
	 * @param reloadGlobals
	 * @param reloadTimeouts
	 * @param reloadExecutionQueue
	 * @param reloadPersistenceConfig
	 * @param reloadPreferences
	 * @param reloadProfiler
	 * @param reloadScripts
	 * @param reloadExtensions
	 */
	void onPreReloadAliases(boolean reloadGlobals, boolean reloadTimeouts, boolean reloadExecutionQueue, 
			boolean reloadPersistenceConfig, boolean reloadPreferences, boolean reloadProfiler, 
			boolean reloadScripts, boolean reloadExtensions);

	/**
	 * Called when server is shutting down, or during a /reloadaliases call.
	 */
	void onShutdown();
}
