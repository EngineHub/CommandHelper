package com.laytonsmith.database;

import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.MethodScriptFileLocations;
import org.sqlite.SQLiteJDBCLoader;
import org.sqlite.util.OSInfo;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
@Profiles.ProfileType(type = "sqlite")
public class SQLiteProfile extends SQLProfile {

	private String file;

	public SQLiteProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
		super(id, elements);
		if(!elements.containsKey("file")) {
			throw new Profiles.InvalidProfileException("\"file\" parameter is required for profile \"" + id + "\"");
		}
		file = elements.get("file");
	}

	public File getFile() {
		File f = new File(file);
		if(!f.isAbsolute()) {
			f = new File(MethodScriptFileLocations.getDefault().getProfilesFile().getParentFile(), f.getPath());
		}
		return f;
	}

	@Override
	public String getConnectionString() throws SQLException {
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new SQLException("Cannot load SQLite. Check your installation and try again");
		}
		// Set native library override path if not already set. (e.g. -Dorg.sqlite.lib.path="/path/to/lib")
		if(System.getProperty("org.sqlite.lib.path") == null) {
			System.setProperty("org.sqlite.lib.path", new File(MethodScriptFileLocations.getDefault().getConfigDirectory(),
					"sqlite/native/" + OSInfo.getNativeLibFolderPathForCurrentOS()).getAbsolutePath());
		}
		try {
			// Load native library before connection to detect when the library is missing.
			// This is done in SQLiteDataSource as well.
			SQLiteJDBCLoader.initialize();
		} catch (Exception ex) {
			throw new SQLException("Failed to load a native sqlite library for your platform."
					+ " You can download the library file from"
					+ " https://github.com/xerial/sqlite-jdbc/tree/master/src/main/resources/org/sqlite/native/"
					+ OSInfo.getNativeLibFolderPathForCurrentOS() + " and place it into "
					+ System.getProperty("org.sqlite.lib.path"));
		}
		return "jdbc:sqlite:" + getFile();
	}

	@Override
	public boolean providesParameterTypes() {
		return false;
	}

}
