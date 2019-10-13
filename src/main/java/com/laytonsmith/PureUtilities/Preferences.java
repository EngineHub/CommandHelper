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

	private final Map<GroupData, Map<String, Preference>> prefs = new HashMap<>();
	private final String appName;
	@SuppressWarnings("NonConstantLogger")
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
		@ObjectHelpers.StandardField
		public String name;
		/**
		 * The value of the preference, as a string
		 */
		@ObjectHelpers.ToString
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

		/**
		 * The group name, by default a group named "General" with no description, and a sort order of 0
		 */
		@ObjectHelpers.ToString
		public GroupData group = new GroupData("General").setSortOrder(0);
		/**
		 * The preference sort order, by default 100. Sorting takes place within groups, with preferences
		 * with identical sort values sorted alphabetically.
		 */
		public int sort = 100;

		public Preference(String name, String def, Type allowed, String description) {
			this.name = name;
			this.value = def;
			this.allowed = allowed;
			this.description = description;
		}

		public Preference(String name, String def, Type allowed, String description, GroupData group) {
			this(name, def, allowed, description);
			this.group = group;
		}

		public Preference(String name, String def, Type allowed, String description, int sort) {
			this(name, def, allowed, description);
			this.sort = sort;
		}

		public Preference(String name, String def, Type allowed, String description, GroupData group, int sort) {
			this(name, def, allowed, description, group);
			this.sort = sort;
		}

		@Override
		public String toString() {
			return ObjectHelpers.DoToString(this);
		}

		@Override
		@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
		public boolean equals(Object o) {
			return ObjectHelpers.DoEquals(this, o);
		}

		@Override
		public int hashCode() {
			return ObjectHelpers.DoHashCode(this);
		}

	}

	/**
	 * Provide the name of the app, and logger, for recording errors, and a list of defaults, in case the value is not
	 * provided by the user, or an invalid value is provided. It also writes a custom header at the top of the file.
	 * Newlines are supported, but only \n
	 * @param appName
	 * @param logger
	 * @param defaults
	 * @param header
	 */
	public Preferences(String appName, Logger logger, List<Preference> defaults, String header) {
		this.appName = appName;
		this.logger = logger;
		for(Preference p : defaults) {
			if(!prefs.containsKey(p.group)) {
				prefs.put(p.group, new HashMap<>());
			}
			prefs.get(p.group).put(p.name, p);
		}
		if(!header.trim().isEmpty()) {
			this.header = "#  " + header.replaceAll("\n", "\n#  ");
		}
	}

	/**
	 * A class that represents the groups that preferences can be in. The only required parameter is the name.
	 */
	public static class GroupData implements Comparable<GroupData> {
		private final String name;
		private int sort = 100;
		private String description = null;

		public GroupData(String name) {
			this.name = name;
		}

		/**
		 * Sets the sort order. If the sort order is the same, it is alphabetical.
		 * @param sort
		 * @return
		 */
		public GroupData setSortOrder(int sort) {
			this.sort = sort;
			return this;
		}

		/**
		 * A description of the category itself. If empty, no description is added.
		 * @param description
		 * @return
		 */
		public GroupData setDescription(String description) {
			this.description = description;
			return this;
		}

		public String getName() {
			return name;
		}

		public int getSort() {
			return sort;
		}

		public String getDescription() {
			return description;
		}

		@Override
		public int compareTo(GroupData o) {
			if(this.sort < o.sort) {
				return -1;
			} else if(this.sort > o.sort) {
				return 1;
			} else {
				return this.name.compareTo(o.name);
			}
		}

	}

	/**
	 * Provide the name of the app, and logger, for recording errors, and a list of defaults, in case the value is not
	 * provided by the user, or an invalid value is provided.
	 * @param appName
	 * @param logger
	 * @param defaults
	 */
	public Preferences(String appName, Logger logger, List<Preference> defaults) {
		this(appName, logger, defaults, "");
	}

	/**
	 * Searches through all preferences, regardless of group, and finds the Preference with the name.
	 * @param key
	 * @return
	 */
	private Preference getPrefFromKey(String key) {
		for(Map<String, Preference> m : prefs.values()) {
			if(m.containsKey(key)) {
				return m.get(key);
			}
		}
		return null;
	}

	/**
	 * Given a file that the preferences are supposedly stored in, this function will try to load the preferences. If
	 * the preferences don't exist, or they are incomplete, this will also fill in the missing values, and store the now
	 * complete preferences in the file location specified.
	 *
	 * @param prefFile
	 * @throws IOException
	 */
	public void init(File prefFile) throws IOException {
		this.prefFile = prefFile;
		if(prefFile != null && prefFile.exists()) {
			Properties userProperties = new Properties();
			try(FileInputStream in = new FileInputStream(prefFile)) {
				userProperties.load(in);
			}
			for(String key : userProperties.stringPropertyNames()) {
				if(key.startsWith("[")) {
					// group name, skip it.
					continue;
				}
				Preference p = getPrefFromKey(key);
				String val = userProperties.getProperty(key);
				String value = Objects.toString(getObject(val, p), null);
				Object ovalue = getObject(val, p);
				Preference p2;
				if(p != null) {
					p2 = new Preference(p.name, value, p.allowed, p.description, p.group, p.sort);
				} else {
					p2 = new Preference(key, val, Type.STRING, "");
				}
				p2.objectValue = ovalue;
				if(!prefs.containsKey(p2.group)) {
					prefs.put(p2.group, new HashMap<>());
				}
				prefs.get(p2.group).put(key, p2);
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

	private Object getSafePreference(String name, Type type) {
		Preference p = getPrefFromKey(name);
		if(p.allowed != type) {
			throw new IllegalArgumentException("Expecting " + p.allowed + " but " + type + " was requested");
		}
		if(p.objectValue == null) {
			p.objectValue = getObject(p.value, p);
		}
		return p.objectValue;
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

	@SuppressWarnings("UseSpecificCatch")
	private void save() {
		try {
			StringBuilder b = new StringBuilder();
			String nl = System.getProperty("line.separator");

			b.append("# This file is generated automatically. Changes made to the values of this file")
					.append(nl)
					.append("# will persist, but changes to comments will not. For windows file paths,")
					.append(nl)
					.append("# use either / or \\\\, but not a single \\.")
					.append(nl).append(nl);
			if(!header.trim().isEmpty()) {
				b.append(header).append(nl).append(nl);
			}
			SortedSet<Preference> prfs = new TreeSet<>((Preference t, Preference t1) -> {
				int groupSort = t.group.compareTo(t1.group);
				if(groupSort != 0) {
					return groupSort;
				}
				if(t.sort == t1.sort) {
					return t.name.compareTo(t1.name);
				}
				return t.sort < t1.sort ? -1 : 1;
			});
			for(Map<String, Preference> m : prefs.values()) {
				prfs.addAll(m.values());
			}
			GroupData currentGroup = null;
			for(Preference p : prfs) {
				if(!p.group.equals(currentGroup)) {
					b.append("[").append(p.group.getName()).append("]\n");
					if(p.group.getDescription() != null && !p.group.getDescription().trim().equals("")) {
						for(String line2 : StringUtils.lineSplit(p.group.getDescription(), lineLength)) {
							b.append("# ").append(line2).append(nl);
						}
						b.append(nl);
					}
					currentGroup = p.group;
				}
//				Preference p = getPrefFromKey(key);
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
				b.append(c).append(nl).append(p.name).append("=");
				if(p.allowed == Type.FILE && p.value != null) {
					b.append(p.value.replace("\\", "\\\\"));
				} else {
					b.append(p.value);
				}
				b.append(nl).append(nl);
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
