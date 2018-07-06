package com.laytonsmith.PureUtilities.VirtualFS;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A collection of file system settings are tied to a glob, and are passed in upon creation of the VirtualFileSystem.
 * Any files or folders that exist (or are attempted to be created) and match the glob are first checked against these
 * settings, upon which the request will either be allowed, or be denied.
 *
 */
public class VirtualFileSystemSettings {

	/**
	 * DO NOT CHANGE THIS. It will break future integrations if you do.
	 *
	 * This is the line that denotes the divider between the top and bottom parts of a settings file.
	 */
	public static final String GENERATED_LINE
			= "This file is automatically generated, to keep up-to-date with new features.\n"
			+ "# Comments you add to the file will not be retained.\n";
	private static final String SETTING_TYPES;

	static {
		List<String> list = new ArrayList<String>();
		for(VirtualFileSystemSetting setting : VirtualFileSystemSetting.values()) {
			String s = "Setting name: " + setting.getName() + "\n"
					+ "# Default value: " + setting.getDef().toString() + "\n"
					+ "# Description: " + setting.getDescription() + "\n";
			list.add(s);
		}
		SETTING_TYPES = StringUtils.Join(list, "\n# ");
	}

	public static String getDefaultSettingsString() {
		return StreamUtils.GetString(VirtualFileSystemSettings.class.getResourceAsStream("example_settings.yml"))
				.replace("%%SETTING_TYPES%%", SETTING_TYPES)
				.replace("%%GENERATED_LINE%%", GENERATED_LINE);
	}

	public static enum VirtualFileSystemSetting {
		HIDDEN("hidden", false, "If true, the file system will not allow the file or directory to be created,"
				+ " and if a file or directory already exists, it will not"
				+ " be exposed. This is essentially a way to revoke both read and write privileges."),
		QUOTA("quota", -1, "Sets the quota for the total list of files or folders that match this glob. Quotas for a"
				+ " cordoned off file system will only affect files that are in the virtual file system, and file"
				+ " sizes of externally created files won't count, but in a uncordoned file system, all files that"
				+ " match this glob are calculated. Due to real time changes in file system size, for directory"
				+ " based globs, this quota may not be enforced precisely, however, it should generally be close."
				+ " If the quota is set to -1, the quota is unrestricted, and if 0, it is \"full\"."
				+ " The unit of measure is bytes, so 1024 is a KB."
				+ " This value is only applicable to the glob **,"
				+ " meaning that the quota can only be applied per entire virtual file system."),
		READONLY("readonly", false, "If true, this file or folder will not be writable."),
		CORDONED_OFF("cordoned-off", false, "If true, files and folders in this directory will not appear to the"
				+ " virtual file system, unless the file was created from within the virtual file system."
				+ " This glob must be the ** glob, meaning that either the whole file system is cordoned off,"
				+ " or the whole file system is not cordoned off."),
		FOLDER_DEPTH("folder-depth", -1, "The number of folders deep that will be allowed to be created in this"
				+ " directory. The glob must be a directory if this is anything other than -1."
				+ " -1 means that the number of sub folders is unrestricted, 0 means that no folders can be created"
				+ " inside this one. This does not affect existing folder structure."),;
		private String name;
		private Object def;
		private String description;

		private VirtualFileSystemSetting(String name, Object def, String description) {
			this.name = name;
			this.def = def;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public Object getDef() {
			return def;
		}

		public String getDescription() {
			return description;
		}

		static VirtualFileSystemSetting getSettingByName(String name) {
			for(VirtualFileSystemSetting s : VirtualFileSystemSetting.values()) {
				if(s.getName().equals(name)) {
					return s;
				}
			}
			return null;
		}

	}

	public static String serialize(Map<VirtualGlob, SettingGroup> settings) {
		DumperOptions options = new DumperOptions();
		options.setPrettyFlow(true);
		Yaml yaml = new Yaml(options);
		Map<String, Map<String, Object>> serializable = new HashMap<String, Map<String, Object>>();
		for(VirtualGlob glob : settings.keySet()) {
			Map<String, Object> inner = new HashMap<String, Object>();
			for(VirtualFileSystemSetting setting : settings.get(glob).settingGroup.keySet()) {
				inner.put(setting.getName(), settings.get(glob).get(setting));
			}
			serializable.put(glob.toString(), inner);
		}
		return yaml.dump(serializable);
	}

	public static Map<VirtualGlob, SettingGroup> deserialize(String settings) {
		Yaml yaml = new Yaml();
		Map<String, Map<String, Object>> unserialized = (Map) yaml.load(settings);
		Map<VirtualGlob, SettingGroup> parsedSettings = new HashMap<VirtualGlob, SettingGroup>();
		if(unserialized != null) {
			for(String glob : unserialized.keySet()) {
				VirtualGlob vglob = new VirtualGlob(glob);
				Map<String, Object> settingGroup = (Map) unserialized.get(glob);
				SettingGroup group = new SettingGroup();
				for(String settingName : settingGroup.keySet()) {
					VirtualFileSystemSetting s = VirtualFileSystemSetting.getSettingByName(settingName);
					Object value = settingGroup.get(settingName);
					group.set(s, value);
				}
				parsedSettings.put(vglob, group);
			}
		}
		return parsedSettings;
	}

	public static class SettingGroup {

		private Map<VirtualFileSystemSetting, Object> settingGroup;

		public SettingGroup() {
			this.settingGroup = new EnumMap<VirtualFileSystemSetting, Object>(VirtualFileSystemSetting.class);
		}

		public SettingGroup(Map<VirtualFileSystemSetting, Object> settingGroup) {
			this.settingGroup = settingGroup;
		}

		public void set(VirtualFileSystemSetting setting, Object value) {
			settingGroup.put(setting, value);
		}

		public Object get(VirtualFileSystemSetting setting) {
			if(settingGroup.containsKey(setting)) {
				return settingGroup.get(setting);
			} else {
				return setting.getDef();
			}
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			for(VirtualFileSystemSetting s : settingGroup.keySet()) {
				b.append(s.getName()).append(": ").append(settingGroup.get(s)).append("; ");
			}
			return b.toString().trim();
		}

	}

	private static final Map<VirtualFileSystemSetting, Object> META_DIRECTORY_SETTINGS =
			new EnumMap<VirtualFileSystemSetting, Object>(VirtualFileSystemSetting.class);

	static {
		META_DIRECTORY_SETTINGS.put(VirtualFileSystemSetting.HIDDEN, true);
	}

	private Map<VirtualGlob, SettingGroup> settings;
	private boolean hasQuota = false;
	private boolean cordonedOff = false;

	public VirtualFileSystemSettings(File settingsFile) throws IOException {
		this(settingsFile.exists() ? FileUtil.read(settingsFile) : "");
		//Here, we will also output the serialized file, with the comment
		//block at top
		FileUtil.write(getDefaultSettingsString() + serialize(settings), settingsFile);
	}

	public VirtualFileSystemSettings(String unparsedSettings) {
		settings = (HashMap<VirtualGlob, SettingGroup>) deserialize(unparsedSettings);
	}

	public VirtualFileSystemSettings(Map<VirtualGlob, SettingGroup> settings) {
		this.settings = new HashMap<VirtualGlob, VirtualFileSystemSettings.SettingGroup>(settings);
		this.settings.put(new VirtualGlob(VirtualFileSystem.META_DIRECTORY), new SettingGroup(META_DIRECTORY_SETTINGS));
		for(VirtualGlob g : settings.keySet()) {
			SettingGroup s = settings.get(g);
			if(s.settingGroup.keySet().contains(VirtualFileSystemSetting.QUOTA)) {
				if((Integer) s.settingGroup.get(VirtualFileSystemSetting.QUOTA) >= 0) {
					if(g.matches(new VirtualFile("/"))) {
						hasQuota = true;
					} else {
						Logger.getLogger(VirtualFileSystemSettings.class.getName()).log(Level.WARNING,
								"The \"quota\" setting can only be applied to the root of the file system at this"
								+ " time. The quota setting for " + g.toString() + " is being ignored.");
					}
				}
			}

			if(s.settingGroup.keySet().contains(VirtualFileSystemSetting.CORDONED_OFF)) {
				if((Boolean) s.settingGroup.get(VirtualFileSystemSetting.CORDONED_OFF) == true) {
					if(g.matches(new VirtualFile("/"))) {
						cordonedOff = true;
					} else {
						Logger.getLogger(VirtualFileSystemSettings.class.getName()).log(Level.WARNING,
								"The \"cordoned-off\" setting can only be applied to the root of the file system at"
								+ " this time. The setting for " + g.toString() + " is being ignored.");
					}
				}
			}
		}
	}

	/**
	 * Gets the most specific value for the specified setting, for the specified file. File specificity will match
	 * whatever is closest, so if this matches both the globs: ** and file/**, then the file/** glob settings will win.
	 *
	 * @param file
	 * @param setting
	 * @return
	 */
	public Object getSetting(VirtualFile file, VirtualFileSystemSetting setting) {
		SortedSet<VirtualGlob> matchedGlobs = new TreeSet<VirtualGlob>();
		for(VirtualGlob glob : settings.keySet()) {
			if(glob.matches(file)) {
				matchedGlobs.add(glob);
			}
		}
		if(matchedGlobs.isEmpty()) {
			//trivial state
			return setting.getDef();
		} else if(matchedGlobs.size() == 1) {
			//trivial state
			return settings.get(matchedGlobs.first()).get(setting);
		} else {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	public boolean hasQuota() {
		return hasQuota;
	}

	public boolean isCordonedOff() {
		return cordonedOff;
	}

}
