package com.laytonsmith.database;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MySQL extends DB {

//	@Override
//	protected String testQuery() {
//		return "SELECT 1";
//	}
//
//	@Override
//	protected Object do_query(CConnection c, String query, Object[] params) throws SQLException {
//		try {
//            Class.forName(com.mysql.jdbc.Driver.class.getName());
//        }
//        catch (ClassNotFoundException ex) {
//            throw new SQLException("Cannot load MySQL. Check your installation and try again");
//        }
//		
//		Connection conn = DriverManager.getConnection("jdbc:mysql://" + c.hostname + "/" + c.database);
//        PreparedStatement prep = MySQL.GetPreparedStatement(query, c, conn);
//        int i = 0;
//        for(Object o : params){
//            i++;
//            prep.setObject(i, o);
//        }
//        prep.addBatch();
//        if(prep.execute()){
//            return prep.getResultSet();
//        } else {
//            return prep.getUpdateCount();
//        }
//	}
//	
//	private static PreparedStatement GetPreparedStatement(String query, CConnection cconn, Connection conn) throws SQLException{
//        PreparedStatement prep = conn.prepareStatement(query);
//        return prep;        
//    }
//	
//	@Override
//	protected Set raw_query(CConnection c, String query) {
//		throw new UnsupportedOperationException("TODO: Not supported yet.");
//	}
//
//
//	@Override
//	protected String sanitize(CConnection c, Object o) throws SQLException {
//		throw new UnsupportedOperationException("TODO: Not supported yet.");
//	}
	
	@Profiles.ProfileType(type = "mysql")
	public static class MySQLProfile extends Profiles.Profile {
		
		private final String host;
		private final int port;
		private final String database;
		private final String username;
		private final String password;

		public MySQLProfile(String id, Map<String, String> elements) throws Profiles.InvalidProfileException {
			super(id);
			if(!elements.containsKey("database")){
				throw new Profiles.InvalidProfileException("Required \"database\" tag is missing for profile \"" + id + "\"");
			}
			database = elements.get("database");
			if(elements.containsKey("username")){
				username = elements.get("username");
			} else {
				username = null;
			}
			if(elements.containsKey("password")){
				password = elements.get("password");
			} else {
				password = null;
			}
			if(elements.containsKey("host")){
				host = elements.get("host");
			} else {
				host = "localhost";
			}
			if(elements.containsKey("port")){
				try {
					port = Integer.parseInt(elements.get("port"));
				} catch(NumberFormatException ex){
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
			}
			catch (ClassNotFoundException ex) {
				throw new SQLException("Cannot load MySQL. Check your installation and try again");
			}
			try {
				return "jdbc:mysql://" + host + ":" + port + "/" + database + "?generateSimpleParameterMetadata=true"
						+ "&jdbcCompliantTruncation=false"
						+ (username==null?"":"&user=" + URLEncoder.encode(username, "UTF-8"))
						+ (password==null?"":"&password=" + URLEncoder.encode(password, "UTF-8"));
			} catch (UnsupportedEncodingException ex) {
				throw new Error();
			}
		}

		@Override
		public String toString() {
			return super.toString() + " " + host + ":" + port + " username=" + username;
		}
		
	}
	
}
