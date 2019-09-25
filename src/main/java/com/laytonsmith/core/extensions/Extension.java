package com.laytonsmith.core.extensions;

import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.core.AliasCore;

import java.io.File;
import java.util.Map;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface Extension {

	/**
	 * Return the extension tracker used to manage this extension. EXPERIMENTAL! Could have bad side-effects! The use of
	 * this function is for really advanced users. There is no guarantee of the fitness of this function for ANY use.
	 * You have been warned.
	 *
	 * @return
	 */
	ExtensionTracker getExtensionTracker();

	/**
	 * Create and return a valid data directory for this extension's use.
	 *
	 * @return
	 */
	File getConfigDir();

	// Identity functions
	/**
	 * Return the identity of this extension.
	 *
	 * @return
	 */
	String getName();

	/**
	 * Return the version for this extension.
	 *
	 * @return
	 */
	Version getVersion();

	// Lifetime functions
	/**
	 * Called when server is loading, or during a /reloadaliases call.
	 */
	void onStartup();

	/**
	 * Called after the logic in /reloadaliases is called. Won't be called if /reloadaliases's help function is called.
	 */
	void onPostReloadAliases();

	/**
	 * Called just before the logic in /reloadaliases is called. Won't be called if /reloadaliases's help function is
	 * called.
	 *
	 * @param options
	 */
	void onPreReloadAliases(AliasCore.ReloadOptions options);

	/**
	 * Called when server is shutting down, or during a /reloadaliases call.
	 */
	void onShutdown();

	/**
	 * Returns a list of help topics, mapping topic name to help text. This is used by the help-topic cmdline tool.
	 * It may return null or empty Map.
	 * @return
	 */
	Map<String, String> getHelpTopics();
}
