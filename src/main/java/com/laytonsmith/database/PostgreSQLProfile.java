package com.laytonsmith.database;

import com.laytonsmith.core.Profiles;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Map;

/**
 *
 */
@Profiles.ProfileType(type = "postgresql")
public class PostgreSQLProfile extends SQLProfile {

	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;
	private final boolean ssl;

	public PostgreSQLProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
		super(id, elements);
		if(!elements.containsKey("database")) {
			throw new Profiles.InvalidProfileException(
					"Required \"database\" tag is missing for profile \"" + id + "\"");
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
		if(elements.containsKey("host")) {
			host = elements.get("host");
		} else {
			host = "localhost";
		}
		if(elements.containsKey("port")) {
			try {
				port = Integer.parseInt(elements.get("port"));
			} catch (NumberFormatException ex) {
				throw new Profiles.InvalidProfileException(ex.getMessage());
			}
		} else {
			port = 5432;
		}
		ssl = elements.containsKey("ssl");
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
	public boolean getAutogeneratedKeys(String query) {
		return query.matches("(?i)insert into.*");
	}

	@Override
	public String getConnectionString() throws SQLException {
		try {
			Class.forName(org.postgresql.Driver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new SQLException("Could not load PostgreSQL, check your installation and try again");
		}
		try {
			return "jdbc:postgresql://" + host + ":" + port + "/" + database + "?"
					+ (username == null ? "" : "&user=" + URLEncoder.encode(username, "UTF-8"))
					+ (password == null ? "" : "&password=" + URLEncoder.encode(password, "UTF-8"))
					+ (ssl == false ? "" : "&ssl=true");
		} catch (UnsupportedEncodingException ex) {
			throw new Error();
		}
	}

	@Override
	public String toString() {
		return super.toString() + " " + host + ":" + port + " username=" + username;
	}

}
