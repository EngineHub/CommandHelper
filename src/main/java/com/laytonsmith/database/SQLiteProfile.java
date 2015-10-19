package com.laytonsmith.database;

import com.laytonsmith.core.Profiles;
import com.laytonsmith.core.MethodScriptFileLocations;
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
		if(!elements.containsKey("file")){
			throw new Profiles.InvalidProfileException("\"file\" parameter is required for profile \"" + id + "\"");
		}
		file = elements.get("file");
	}

	public File getFile(){
		File f = new File(file);
		if(!f.isAbsolute()){
			f = new File(MethodScriptFileLocations.getDefault().getProfilesFile(), f.getPath());
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
		return "jdbc:sqlite:" + getFile();
	}

}
