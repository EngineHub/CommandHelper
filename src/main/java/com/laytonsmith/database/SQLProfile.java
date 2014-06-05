package com.laytonsmith.database;

import com.laytonsmith.core.Profiles;
import java.sql.SQLException;

/**
 *
 */
public abstract class SQLProfile extends Profiles.Profile {

	public SQLProfile(String id) {
		super(id);
	}



	/**
	 * Given the connection details, this should return the proper connection
	 * string that the actual database connector will use to create a connection
	 * with this profile. Additionally, during this step, it should be verified
	 * that the SQL driver is present.
	 *
	 * @return
	 * @throws SQLException If the database driver doesn't exist.
	 */
	public abstract String getConnectionString() throws SQLException;
}
