package com.laytonsmith.database;

import com.laytonsmith.PureUtilities.Common.OSUtils;
import com.laytonsmith.PureUtilities.Common.ReflectionUtils;
import com.laytonsmith.core.MethodScriptFileLocations;
import com.laytonsmith.core.Profiles;
import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Profiles.ProfileType(type = "mssql")
public class MSSQLProfile extends SQLProfile {

	private final String host;
	private final String instance;
	private final int port;
	private final String database;
	private final String username;
	private final String password;
	private static final List<String> HANDLED_LIST = Arrays.asList(
			"database", "username", "password", "host", "instance", "port"
	);
	private final Map<String, String> extraParameters = new HashMap<>();

	public MSSQLProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
		super(id, elements);
		if(!elements.containsKey("database")) {
			throw new Profiles.InvalidProfileException("Required \"database\" tag is missing for profile \""
					+ id + "\"");
		}
		database = elements.get("database");
		if(elements.containsKey("username")) {
			username = elements.get("username");
		} else {
			username = null;
		}
		if(elements.containsKey("password")) {
			password = elements.get("password");
		} else {
			password = null;
		}
		if(elements.containsKey("azureHost")) {
			String host = elements.get("azureHost") + ".database.windows.net";
			elements.remove("azureHost");
			elements.put("host", host);
		}
		if(elements.containsKey("host")) {
			host = elements.get("host");
		} else {
			host = "localhost";
		}
		if(elements.containsKey("instance")) {
			instance = "\\" + elements.get("instance");
		} else {
			instance = "";
		}

		if(elements.containsKey("port")) {
			try {
				port = Integer.parseInt(elements.get("port"));
			} catch (NumberFormatException ex) {
				throw new Profiles.InvalidProfileException(ex.getMessage());
			}
		} else {
			port = 1433;
		}

		if(elements.containsKey("integratedSecurity")) {
			String dllName = (String) ReflectionUtils.get(SQLServerDriver.class, "AUTH_DLL_NAME");
			if(!new File(MethodScriptFileLocations.getDefault()
					.getWindowsNativeDirectory(), dllName + ".dll").exists()) {
				if(OSUtils.GetOS().isWindows()) {
					throw new Profiles.InvalidProfileException("integratedSecurity was configured, but MethodScript is"
							+ " not properly configured. Please run `mscript -- install-mssql-auth` to automatically"
							+ " configure your system.");
				} else {
					throw new Profiles.InvalidProfileException("Integrated Security is only available on Windows.");
				}
			}
		}

		for(Map.Entry<String, String> entry : elements.entrySet()) {
			if(!HANDLED_LIST.contains(entry.getKey())) {
				extraParameters.put(entry.getKey(), entry.getValue());
			}
		}
	}

	@Override
	public String getConnectionString() throws SQLException {
		try {
			Class.forName(com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new SQLException("Cannot load MSSQL. Check your installation and try again");
		}

		String connectionString;
		connectionString = "jdbc:sqlserver://" + host;
		if(instance != null) {
			connectionString += "\\" + instance;
		}

		connectionString += ":" + port;
		connectionString += ";";
		if(username != null) {
			connectionString += "user=" + username + ";";
		}
		if(password != null) {
			connectionString += "password=" + password + ";";
		}

		connectionString += "databaseName=" + database + ";";

		for(Map.Entry<String, String> params : extraParameters.entrySet()) {
			connectionString += params.getKey() + "=" + params.getValue() + ";";
		}
		return connectionString;
	}

	@Override
	public String toString() {
		return super.toString() + " " + host + instance + ":" + port;
	}

}
