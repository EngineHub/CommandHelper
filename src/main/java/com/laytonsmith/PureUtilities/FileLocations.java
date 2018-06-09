package com.laytonsmith.PureUtilities;

import java.io.File;

/**
 * This class contains several constant file locations, which can be used throughout the rest of the application. It
 * also includes a static factory method, which can be used to get the default class. Subclasses should be sure to call
 * the parent's static getter method, so that chaining will work properly throughout the application. This particular
 * class is agnostic to the application itself, and so only provides generic locations that may be useful to most Java
 * applications. The controller for the application should use the most specific subclass available when starting up the
 * application, though classes themselves should use as generic a class as possible when requesting file information.
 * Files are immutable, and likely are cached.
 */
public class FileLocations {

	private static FileLocations defaultInstance = null;

	private static final File USER_HOME;
	private static final File USER_DIR;
	private static final File JAVA_HOME;

	static {
		File userHome = null;
		File userDir = null;
		File javaHome = null;
		try {
			userHome = new File(System.getProperty("user.home"));
			userDir = new File(System.getProperty("user.dir"));
			javaHome = new File(System.getProperty("java.home"));
		} catch (SecurityException e) {
			//This could happen in applets or some other wierd security configuration.
			//Regardless, we don't want this to ever fail.
		}
		USER_HOME = userHome;
		USER_DIR = userDir;
		JAVA_HOME = javaHome;
	}

	/**
	 * Returns the default FileLocations instance. If the controller has set a subclass, it will be returned, but by
	 * default it will be an instance of FileLocations.
	 *
	 * @return
	 */
	public static FileLocations getDefault() {
		if(defaultInstance == null) {
			defaultInstance = new FileLocations();
		}
		return defaultInstance;
	}

	/**
	 * Sets the default FileLocations provider.
	 *
	 * @param provider
	 */
	public static void setDefault(FileLocations provider) {
		defaultInstance = provider;
	}

	/**
	 * Returns the user's home directory.
	 *
	 * @return
	 */
	public File getUserHome() {
		return USER_HOME;
	}

	/**
	 * Returns the user's working directory.
	 *
	 * @return
	 */
	public File getUserDir() {
		return USER_DIR;
	}

	/**
	 * Returns the installation directory for the Java Runtime Environment (JRE).
	 *
	 * @return
	 */
	public File getJavaHome() {
		return JAVA_HOME;
	}
}
