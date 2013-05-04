package com.laytonsmith.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This abstract class defines the highest level functionality that all database
 * connections supported by CH provide. Inputs and outputs are specified as
 * POJOs, but they are all Construct compatible, and can easily be converted
 * with the static methods in Construct.
 *
 * @author layton
 */
public abstract class DB {
	
	protected DB(){}

    private static class Statics {

        private static CConnection lastConnection = null;

        protected static void SetLastConnection(CConnection lastConnection) {
            Statics.lastConnection = lastConnection;
        }

        protected static CConnection GetLastConnection() throws SQLException {
            if (lastConnection == null) {
                throw new SQLException("No connection specified!");
            }
            return Statics.lastConnection;
        }
    }

    public enum SupportedDBConnectors {
		MYSQL,
        SQLITE;
    }

    final public static class CConnection {

        private static Map<String, CConnection> cache = new HashMap<String, CConnection>();

        public static CConnection GetConnection(String connectionName) throws SQLException {
            if (cache.containsKey(connectionName)) {
                return cache.get(connectionName);
            }
            CConnection c = ConnectionLookup(connectionName);
            cache.put(connectionName, c);
            return c;
        }
		
		public static CConnection GetConnection(SupportedDBConnectors type, String hostename, String database, int port, String username, String password){
			return new CConnection(type, hostename, database, port, username, password);
		}

        private static CConnection ConnectionLookup(String name) throws SQLException {
			return null;
			//Don't use the file path directly.
//            ZipReader zr = new ZipReader(new File("plugins/CommandHelper/Connections/" + name));
//            Properties p = new Properties();
//            try {
//                p.load(zr.getInputStream());
//            }
//            catch (IOException ex) {
//                throw new SQLException(ex.getMessage());
//            }
//            String filePath = zr.getFile().getAbsolutePath();
//            String hostname = p.getProperty("hostname");
//            Integer port;
//            try {
//                String sport = p.getProperty("port", "");
//                if (sport.isEmpty()) {
//                    port = null;
//                } else {
//                    port = Integer.parseInt(sport);
//                    if (port < 1 || port > 65535) {
//                        throw new NumberFormatException();
//                    }
//                }
//            }
//            catch (NumberFormatException e) {
//                throw new SQLException("Could not convert port in " + filePath + " to a number, or the number was less than 1 or greater than 65535");
//            }
//            String username = p.getProperty("username", "");
//            String password = p.getProperty("password", "");
//            if (hostname == null) {
//                throw new SQLException("No hostname provided in " + filePath);
//            }
//            try {
//                SupportedDBConnectors type = SupportedDBConnectors.valueOf(p.getProperty("type").toUpperCase());
//                return new CConnection(type, hostname, port, username, password);
//            }
//            catch (IllegalArgumentException e) {
//                throw new SQLException("Unsupported type " + p.getProperty("type"));
//            }
        }
        SupportedDBConnectors type;
        String hostname;
		String database;
        Integer port;
        String username;
        String password;

        private CConnection(SupportedDBConnectors type, String hostname, String database, int port, String username, String password) {
            this.type = type;
            this.hostname = hostname;
			this.database = database;
            this.port = port;
            this.username = username;
            this.password = password;
        }
    }

    /**
     * Performs a raw query to the database. The parameters have already been
     * inserted into the query at this point.
     *
     * @param c
     * @param query
     * @return
     */
    protected abstract Set raw_query(CConnection c, String query);

    /**
     * Note to subclasses: If prepared queries are more efficiently handled
     * directly in the particular implementation, this method can be overridden,
     * and raw_query and sanitize will not be used. This method should return a
     * java.util.Set if it is a select or other query that returns a result set,
     * or an Integer if it was an update query, which specifies the number of
     * results affected, or an int if it was an insert query with an
     * auto-increment.
     *
     * @param c
     * @param query
     * @param params
     * @return
     * @throws SQLException
     */
    protected Object do_query(CConnection c, String query, Object[] params) throws SQLException {
        return this.raw_query(c, escape(c, query, params));
    }

    /**
     * Performs a query to the database, using the specified connection. The
     * query should be a prepared query, that is, parameters to be filled in
     * should be question marks, and the objects in the params array will be
     * filled in to the question marks.
     *
     *
     * @param c
     * @param query
     * @param params
     * @return
     * @throws SQLException
     */
    final public Object query(CConnection c, String query, Object... params) throws SQLException {
        Statics.SetLastConnection(c);
        return this.do_query(c, query, params);
    }

    /**
     * Performs a query to the database, using the last connection.
     *
     * @param query
     * @param params
     * @return
     * @throws SQLException
     */
    final public Object query(String query, Object... params) throws SQLException {
        return query(Statics.GetLastConnection(), query, params);
    }

    /**
     * Performs a query to the database, using the last connection.
     *
     * @param query
     * @return
     * @throws SQLException
     */
    final public Object query(String query) throws SQLException {
        return query(Statics.GetLastConnection(), query);
    }

    /**
     * Looks through the query and escapes the parameters. The sanitize function
     * is used to actually create the sanitized parameter, but this function
     * handles the replacement. The returned string is the fully prepared query,
     * and is safe to run raw.
     *
     * @param c
     * @param query
     * @param params
     * @return
     * @throws SQLException
     */
    final protected String escape(CConnection c, String query, Object[] params) throws SQLException {
        int prepared = 0;
        boolean failure = false;
        for (char ch : query.toCharArray()) {
            if (ch == '?') {
                prepared++;
            }
        }
        if (prepared != params.length) {
            failure = true;
        }
        StringBuilder b = new StringBuilder();
        int index = 0;
        for (char ch : query.toCharArray()) {
            if (ch == '?') {
                try {
                    Object o = params[index];
                    index++;
                    b.append(this.sanitize(c, o));
                }
                catch (IndexOutOfBoundsException e) {
                    failure = true;
                    break;
                }
            } else {
                b.append(ch);
            }
        }
        if (failure) {
            throw new SQLException("Invalid number of parameters sent to query. Expected " + prepared + " argument"
                    + ( prepared != 1 ? "s" : "" ) + " but recieved " + params.length);
        }
        return b.toString();
    }

    /**
     * Provides a short, efficient query that allows for a connection test to
     * happen, without changing the database at all. <a
     * href='http://stackoverflow.com/questions/3668506/efficient-sql-test-query-that-will-work-across-all-or-most-databases'>See
     * here.</a>
     *
     * @return
     */
    protected abstract String testQuery();

    /**
     * Given an object, this function should sanitize the value, such that it
     * can be inserted directly into a query.
     *
     * @param c
     * @param o
     * @return
     * @throws SQLException
     */
    protected abstract String sanitize(CConnection c, Object o) throws SQLException;

    public void connect(CConnection c) throws SQLException {
        query(c, testQuery());
    }
}
