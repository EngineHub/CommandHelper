package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class allows an application to more easily manage user preferences. As an application grows, more preferences
 * will likely be added, but if the application uses flat file storage, managing these preferences while adding new
 * preferences can be difficult. This class manages, documents, and provides default values, all the while not
 * interfering with changes the user has made, meaning that you are free to add new preferences, or change default
 * values, without fear of changing values that the user has specifically set. For sample usage, see
 * https://gist.github.com/1042094
 */
public class Preferences {

	private final Map<String, Preference> prefs = new HashMap<String, Preference>();
	private final String appName;
	private final Logger logger;

	private File prefFile;

	private String header = "";

	private int lineLength = 120;

	/**
	 * The type a particular preference can be. The value will be cast to the given type if possible. NUMBER and DOUBLE
	 * are guaranteed to be castable to a Double. NUMBER can also sometimes be cast to an int. BOOLEAN is cast to a
	 * boolean, and may be stored in the preferences file as either true/false, yes/no, on/off, or a number, which get
	 * parsed accordingly. STRING can be any value. FILE is interpreted as a File object (whether or not it exists).
	 */
	public enum Type {
		/**
		 * This is a number, either a double or an int, depending on the input
		 */
		NUMBER,
		/**
		 * This is a true or false value. The following words mean true: true, yes, on. The following words mean false:
		 * false, no, off. Case does not matter.
		 */
		BOOLEAN,
		/**
		 * This can be any value, and is returned as a string as is.
		 */
		STRING,
		/**
		 * This must an integer.
		 */
		INT,
		/**
		 * This must be a double.
		 */
		DOUBLE,
		/**
		 * This represents a file on the file system. The existence of the file does not matter. If the input is an
		 * empty string, null is returned.
		 */
		FILE
	}

	/**
	 * An object corresponding to a single preference
	 */
	public static class Preference {

		/**
		 * The name of the preference
		 */
		public String name;
		/**
		 * The value of the preference, as a string
		 */
		public String value;
		/**
		 * The allowed type of this value
		 */
		public Type allowed;
		/**
		 * The description of this preference. Used to write out to file.
		 */
		public String description;

		/**
		 * The object representation of this value. Should not be used directly.
		 */
		public Object objectValue;

		public Preference(String name, String def, Type allowed, String description) {
			this.name = name;
			this.value = def;
			this.allowed = allowed;
			this.description = description;
		}
	}

	/**
	 * Provide the name of the app, and logger, for recording errors, and a list of defaults, in case the value is not
	 * provided by the user, or an invalid value is provided. It also writes a custom header at the top of the file.
	 * Newlines are supported, but only \n
	 */
	public Preferences(String appName, Logger logger, List<Preference> defaults, String header) {
		this.appName = appName;
		this.logger = logger;
		for(Preference p : defaults) {
			prefs.put(p.name, p);
		}
		if(!header.trim().isEmpty()) {
			this.header = "#  " + header.replaceAll("\n", "\n#  ");
		}
	}

	/**
	 * Provide the name of the app, and logger, for recording errors, and a list of defaults, in case the value is not
	 * provided by the user, or an invalid value is provided.
	 */
	public Preferences(String appName, Logger logger, List<Preference> defaults) {
		this(appName, logger, defaults, "");
	}

	/**
	 * Given a file that the preferences are supposedly stored in, this function will try to load the preferences. If
	 * the preferences don't exist, or they are incomplete, this will also fill in the missing values, and store the now
	 * complete preferences in the file location specified.
	 *
	 * @param prefFile
	 * @throws Exception
	 */
	public void init(File prefFile) throws IOException {
		this.prefFile = prefFile;
		if(prefFile != null && prefFile.exists()) {
			Properties userProperties = new Properties();
			FileInputStream in = new FileInputStream(prefFile);
			userProperties.load(in);
			in.close();
			for(String key : userProperties.stringPropertyNames()) {
				String val = userProperties.getProperty(key);
				String value = Objects.toString(getObject(val, prefs.get(key)), null);
				Object ovalue = getObject(val, prefs.get(key));
				Preference p1 = prefs.get(key);
				Preference p2;
				if(p1 != null) {
					p2 = new Preference(p1.name, value, p1.allowed, p1.description);
				} else {
					p2 = new Preference(key, val, Type.STRING, "");
				}
				p2.objectValue = ovalue;
				prefs.put(key, p2);
			}
		}
		save();
	}

	@SuppressWarnings("LoggerStringConcat")
	private Object getObject(String value, Preference p) {
		if(p == null) {
			return value;
		}
		if("null".equalsIgnoreCase(value)) {
			return getObject(p.value, p);
		}
		switch(p.allowed) {
			case INT:
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be an integer. Using the default of " + p.value);
					return Integer.parseInt(p.value);
				}
			case DOUBLE:
				try {
					return Double.parseDouble(value);
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be an double. Using the default of " + p.value);
					return Double.parseDouble(p.value);
				}
			case BOOLEAN:
				try {
					return getBoolean(value);
				} catch (NumberFormatException e) {
					logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be an boolean. Using the default of " + p.value);
					return getBoolean(p.value);
				}
			case NUMBER:
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					try {
						return Double.parseDouble(value);
					} catch (NumberFormatException f) {
						logger.log(Level.WARNING, "[" + appName + "] expects the value of " + p.name + " to be a number. Using the default of " + p.value);
						try {
							return Integer.parseInt(p.value);
						} catch (NumberFormatException g) {
							return Double.parseDouble(p.value);
						}
					}
				}
			case FILE:
				if(value == null || "".equals(value.trim())) {
					return null;
				}
				return new File(value);
			case STRING:
			default:
				return value;
		}

	}

	private Boolean getBoolean(String value) {
		if(value.equalsIgnoreCase("true")) {
			return true;
		} else if(value.equalsIgnoreCase("false")) {
			return false;
		} else if(value.equalsIgnoreCase("yes")) {
			return true;
		} else if(value.equalsIgnoreCase("no")) {
			return false;
		} else if(value.equalsIgnoreCase("on")) {
			return true;
		} else if(value.equalsIgnoreCase("off")) {
			return false;
		} else {
			double d = Double.parseDouble(value);
			return d != 0;
		}
	}

	/**
	 * Returns the value of a preference, cast to the appropriate type.
	 *
	 * @deprecated Instead of using this type-unsafe version, you should use the typesafe versions instead, which add an
	 * extra check to make sure the values will be returned correctly, and throw a more meaningful exception if not.
	 * @param name
	 * @return
	 */
	@Deprecated
	public Object getPreference(String name) {
		if(prefs.get(name).objectValue == null) {
			prefs.get(name).objectValue = getObject(prefs.get(name).value, prefs.get(name));
		}
		return prefs.get(name).objectValue;
	}

	private Object getSafePreference(String name, Type type) {
		if(prefs.get(name).allowed != type) {
			throw new IllegalArgumentException("Expecting " + prefs.get(name).allowed + " but " + type + " was requested");
		}
		return getPreference(name);
	}

	/**
	 * Returns the given boolean preference.
	 *
	 * @param name The name of the preference
	 * @return The preference value, as a boolean
	 * @throws IllegalArgumentException If the preference was not defined as being a boolean
	 */
	public Boolean getBooleanPreference(String name) {
		return (Boolean) getSafePreference(name, Type.BOOLEAN);
	}

	/**
	 * Returns the given double preference.
	 *
	 * @param name The name of the preference
	 * @return The preference value, as a double
	 * @throws IllegalArgumentException If the preference was not defined as being a double
	 */
	public Double getDoublePreference(String name) {
		return (Double) getSafePreference(name, Type.DOUBLE);
	}

	/**
	 * Returns the given File preference. If the preference was blank, then null is returned.
	 *
	 * @param name The name of the preference
	 * @return The preference value, as a File
	 * @throws IllegalArgumentException If the preference was not defined as being a File
	 */
	public File getFilePreference(String name) {
		return (File) getSafePreference(name, Type.FILE);
	}

	/**
	 * Returns the given integer preference.
	 *
	 * @param name The name of the preference
	 * @return The preference value, as an integer
	 * @throws IllegalArgumentException If the preference was not defined as being an integer
	 */
	public Integer getIntegerPreference(String name) {
		return (Integer) getSafePreference(name, Type.INT);
	}

	/**
	 * Returns the given number preference.
	 *
	 * @param name The name of the preference
	 * @return The preference value, as a number
	 * @throws IllegalArgumentException If the preference was not defined as being a number
	 */
	public Number getNumberPreference(String name) {
		return (Number) getSafePreference(name, Type.NUMBER);
	}

	/**
	 * Returns the given string preference.
	 *
	 * @param name The name of the preference
	 * @return The preference value, as a string
	 * @throws IllegalArgumentException If the preference was not defined as being a string
	 */
	public String getStringPreference(String name) {
		return (String) getSafePreference(name, Type.STRING);
	}

	private void save() {
		try {
			StringBuilder b = new StringBuilder();
			String nl = System.getProperty("line.separator");

			b.append("# This file is generated automatically. Changes made to the values of this file")
					.append(nl)
					.append("# will persist, but changes to comments will not.")
					.append(nl).append(nl);
			if(!header.trim().isEmpty()) {
				b.append(header).append(nl).append(nl);
			}
			SortedSet<String> keys = new TreeSet<String>(prefs.keySet()) {
			};
			for(String key : keys) {
				Preference p = prefs.get(key);
				String description = "This value is not used in " + appName;
				if(!p.description.trim().isEmpty()) {
					description = p.description;
				}
				StringBuilder c = new StringBuilder();
				boolean first = true;
				for(String line : description.split("\n|\r\n|\n\r")) {
					for(String line2 : StringUtils.lineSplit(line, lineLength)) {
						if(first) {
							c.append("# ").append(line2);
							first = false;
						} else {
							c.append(nl).append("# ").append(line2);
						}
					}
				}
				b.append(c).append(nl).append(p.name).append("=").append(p.value).append(nl).append(nl);
			}
			if(prefFile != null && !prefFile.exists()) {
				prefFile.getAbsoluteFile().getParentFile().mkdirs();
				prefFile.createNewFile();
			}
			if(prefFile != null) {
				FileUtil.write(b.toString(), prefFile);
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, "[" + appName + "] Could not write out preferences file: " + (prefFile != null ? prefFile.getAbsolutePath() : "null"), ex);
		}
	}

	/**
	 * Sets the comment line length.
	 *
	 * @param lineLength The length, an integer greater than 0.
	 * @throws IllegalArgumentException If {@code lineLength} is less than 1.
	 */
	public void setLineLength(int lineLength) {
		if(lineLength < 1) {
			throw new IllegalArgumentException();
		}
		this.lineLength = lineLength;
	}

}
