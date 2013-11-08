package com.laytonsmith.core.extensions;

import com.laytonsmith.commandhelper.CommandHelperFileLocations;
import java.io.File;
import java.lang.annotation.Annotation;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public abstract class AbstractExtension implements Extension {
    public AbstractExtension() {};
        
    // Identity functions

	/**
	 * Return the identity of this extension.
	 * @return
	 */
	@Override
	public final String getName() {
        for (Annotation a : getClass().getAnnotations()) {
            if (a instanceof MSExtension) {
                MSExtension e = (MSExtension)a;
                return e.value();
            }
        }
        
        return "<unknown>";
    }

	/**
	 * Create and return a valid data directory for this extension's use.
	 * @return
	 */
	@Override
	public File getConfigDir() {
		File f = new File(CommandHelperFileLocations.getDefault().getExtensionsDirectory(), getName());

		if (!f.exists()) {
				f.mkdirs();
		}
                
        return f;
    }
    
	// Lifetime functions

	/**
	 * Called when server is loading, or during a /reloadaliases call.
	 */
	@Override
	public void onStartup() {};

	/**
	 * Called just before the logic in /reloadaliases is called. Won't be called
	 * if /reloadaliases's help function is called.
	 * 
	 * @param reloadGlobals
	 * @param reloadTimeouts
	 * @param reloadExecutionQueue
	 * @param reloadPersistanceConfig
	 * @param reloadPreferences
	 * @param reloadProfiler
	 * @param reloadScripts
	 * @param reloadExtensions
	 */
	@Override
	public void onPreReloadAliases(boolean reloadGlobals, boolean reloadTimeouts, 
					boolean reloadExecutionQueue, boolean reloadPersistanceConfig, 
					boolean reloadPreferences, boolean reloadProfiler, 
					boolean reloadScripts, boolean reloadExtensions) {}

	/**
	 * Called after the logic in /reloadaliases is called. Won't be called
	 * if /reloadaliases's help function is called.
	 */
	@Override
	public void onPostReloadAliases() {}

	/**
	 * Called when server is shutting down, or during a /reloadaliases call.
	 */
	@Override
    public void onShutdown() {};
}
