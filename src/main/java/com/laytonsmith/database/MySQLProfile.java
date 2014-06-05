package com.laytonsmith.database;

import com.laytonsmith.core.Profiles;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
@Profiles.ProfileType(type = "mysql")
public class MySQLProfile extends SQLProfile {

	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;

	public MySQLProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
		super(id);
		if (!elements.containsKey("database")) {
			throw new Profiles.InvalidProfileException("Required \"database\" tag is missing for profile \"" + id + "\"");
		}
		database = elements.get("database");
		if (elements.containsKey("username")) {
			username = elements.get("username");
		} else {
			username = null;
		}
		if (elements.containsKey("password")) {
			password = elements.get("password");
		} else {
			password = null;
		}
		if (elements.containsKey("host")) {
			host = elements.get("host");
		} else {
			host = "localhost";
		}
		if (elements.containsKey("port")) {
			try {
				port = Integer.parseInt(elements.get("port"));
			} catch (NumberFormatException ex) {
				throw new Profiles.InvalidProfileException(ex.getMessage());
			}
		} else {
			port = 3306;
		}
	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String getConnectionString() throws SQLException {
		try {
			Class.forName(com.mysql.jdbc.Driver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new SQLException("Cannot load MySQL. Check your installation and try again");
		}
		try {
			return "jdbc:mysql://" + host + ":" + port + "/" + database + "?generateSimpleParameterMetadata=true"
					+ "&jdbcCompliantTruncation=false"
					+ (username == null ? "" : "&user=" + URLEncoder.encode(username, "UTF-8"))
					+ (password == null ? "" : "&password=" + URLEncoder.encode(password, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new Error();
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + host + ":" + port + " username=" + username;
	}

}
