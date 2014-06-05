
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.FileLocations;
import com.laytonsmith.abstraction.Implementation;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
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
			try {
				return new File(URLDecoder.decode(MethodScriptFileLocations.class.getProtectionDomain().getCodeSource().getLocation().getFile(), "UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new Error(ex);
			}
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
		return new File(getJarDirectory(), Implementation.GetServerType().getBranding() + "/");
	}

	/**
	 * Returns the location of the prefs directory, i.e. where the actual preferences
	 * files are stored. This is the prefs directory, not the main preferences file.
	 * @return
	 */
	public File getPreferencesDirectory(){
		return new File(getConfigDirectory(), "prefs/");
	}

	/**
	 * Returns the location of the preferences file. This is the main
	 * preferences file, not the prefs directory.
	 * @return
	 */
	public File getPreferencesFile(){
		return new File(getPreferencesDirectory(), "preferences.ini");
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
	 * Returns the locations of the extension cache.
	 * @return
	 */
	public File getExtensionCacheDirectory(){
		return new File(getCacheDirectory(), "extensions/");
	}

	/**
	 * Returns the LocalPackages directory.
	 * @return
	 */
	public File getLocalPackagesDirectory(){
		return new File(getConfigDirectory(), "LocalPackages/");
	}

	/**
	 * Returns the Persistence Network config file.
	 * @return
	 */
	public File getPersistenceConfig(){
		return new File(getPreferencesDirectory(), "persistence.ini");
	}

	/**
	 * Returns the default Persistence Network db file location.
	 * @return
	 */
	public File getDefaultPersistenceDBFile(){
		return new File(getConfigDirectory(), "persistence.db");
	}

	/**
	 * Returns the profiler config file location.
	 * @return
	 */
	public File getProfilerConfigFile(){
		return new File(getPreferencesDirectory(), "profiler.ini");
	}

	/**
	 * Returns the SQL profiles config file location.
	 * @return
	 * @deprecated Use {@link #getProfilesFile()} instead, as the method name is
	 * more accurate, since the profiles file contains profiles for other features,
	 * not necessarily SQL.
	 */
	@Deprecated
	public File getSQLProfilesFile(){
		return getProfilesFile();
	}

	/**
	 * Returns the profiles config file location. The profiles config is a universal
	 * repository for saving credentials in a standard location, that isn't tied
	 * to the code base. In general, this file should be ignored by version control
	 * systems.
	 * @return
	 */
	public File getProfilesFile(){
		return new File(getPreferencesDirectory(), "profiles.xml");
	}

	/**
	 * Returns the location of the logger preferences file.
	 * @return
	 */
	public File getLoggerPreferencesFile(){
		return new File(getPreferencesDirectory(), "logger-preferences.ini");
	}

	/**
	 * Returns the location of the cmdline interpreter folder.
	 * @return
	 */
	public File getCmdlineInterpreterDirectory() {
		return new File(System.getProperty("user.home") + "/.mscript");
	}

	/**
	 * Returns the location of the cmdline interpreter auto_include file.
	 * @return
	 */
	public File getCmdlineInterpreterAutoIncludeFile(){
		return new File(getCmdlineInterpreterDirectory(), "auto_include.ms");
	}
}
