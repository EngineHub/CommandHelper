
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.FileLocations;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

/**
 *
 */
public class MethodScriptFileLocations extends FileLocations {
	private static MethodScriptFileLocations defaultInstance = null;
	public static MethodScriptFileLocations getDefault(){
		if(defaultInstance == null){
			setDefault(new MethodScriptFileLocations());
		}
		return defaultInstance;
	}
	
	public static void setDefault(MethodScriptFileLocations provider){
		defaultInstance = provider;
		FileLocations.setDefault(defaultInstance);
	}
	
	/**
	 * Gets the jar location this class was loaded from.
	 * @return 
	 */
	public File getJarFile(){
		if(MethodScriptFileLocations.class.getProtectionDomain().getCodeSource().getLocation() == null){
			//This can happen if we're not running from a jar. Instead, we have to get the folder location.
			URL url = MethodScriptFileLocations.class.getResource("/" + MethodScriptFileLocations.class.getName().replace(".", "/") + ".class");
			String s = url.toString();
			s = s.replaceFirst("file:", "");
			s = StringUtils.replaceLast(s, Pattern.quote(MethodScriptFileLocations.class.getName().replace(".", "/") + ".class"), "");
			return new File(s);
		} else {
			return new File(MethodScriptFileLocations.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		}
	}
	
	/**
	 * Returns the parent folder of getJarFile.
	 * @return 
	 */
	public File getJarDirectory(){
		return getJarFile().getParentFile();
	}
	
	/**
	 * Returns the location of the config directory, i.e. where the preferences
	 * and other config settings are stored.
	 * @return 
	 */
	public File getConfigDirectory(){
		return new File(getJarDirectory(), "CommandHelper/");
	}
	
	/**
	 * Returns the location of the preferences file.
	 * @return 
	 */
	public File getPreferencesFile(){
		return new File(getConfigDirectory(), "preferences.ini");
	}
	
	/**
	 * Returns the location of the cache folder
	 * @return 
	 */
	public File getCacheDirectory(){
		return new File(getConfigDirectory(), ".cache/");
	}
	
	/**
	 * Returns the locations of the extensions.
	 * @return 
	 */
	public File getExtensionsDirectory(){
		return new File(getConfigDirectory(), "extensions/");
	}
	
	/**
	 * Returns the LocalPackages directory.
	 * @return 
	 */
	public File getLocalPackagesDirectory(){
		return new File(getConfigDirectory(), "LocalPackages/");
	}
	
	/**
	 * Returns the Persistance Network config file.
	 * @return 
	 */
	public File getPersistanceConfig(){
		return new File(getConfigDirectory(), "persistance.config");
	}
	
	/**
	 * Returns the default Persistance Network db file location.
	 * @return 
	 */
	public File getDefaultPersistanceDBFile(){
		return new File(getConfigDirectory(), "persistance.db");
	}
	
	/**
	 * Returns the profiler config file location.
	 * @return 
	 */
	public File getProfilerConfigFile(){
		return new File(getConfigDirectory(), "profiler.config");
	}
	
	/**
	 * Returns the SQL profiles config file location.
	 * @return 
	 */
	public File getSQLProfilesFile(){
		return new File(getConfigDirectory(), "sql-profiles.xml");
	}
}
