package com.laytonsmith.database;

import com.laytonsmith.PureUtilities.ZipReader;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This abstract class defines the highest level functionality that all database 
 * connections supported by CH provide. Inputs and outputs are specified as
 * POJOs, but they are all Construct compatible, and can easily be converted
 * with the static methods in Construct.
 * @author layton
 */
public abstract class DB {
    protected static class Statics{
        private static Connection lastConnection = null;
        protected static void SetLastConnection(Connection lastConnection){
            Statics.lastConnection = lastConnection;
        }
        protected static Connection GetLastConnection(){
            return Statics.lastConnection;
        }
    }
    
    public static class Connection{
        private static Map<String, Connection> cache = new HashMap<String, Connection>();
        public static Connection GetConnection(String connectionName) throws SQLException{
            if(cache.containsKey(connectionName)){
                return cache.get(connectionName);
            }
            Connection c = ConnectionLookup(connectionName);
            cache.put(connectionName, c);
            return c;
        }
        private static Connection ConnectionLookup(String name) throws SQLException{
            ZipReader zr = new ZipReader(new File("plugins/CommandHelper/Connections/" + name));
            Properties p = new Properties();
            try {
                p.load(zr.getInputStream());
            }
            catch (IOException ex) {
                throw new SQLException(ex.getMessage());
            }
            String filePath = zr.getFile().getAbsolutePath();
            String hostname = p.getProperty("hostname");
            int port;
            try{
                port = Integer.parseInt(p.getProperty("port"));
            } catch(NumberFormatException e){
                throw new SQLException("Could not convert port in " + filePath + " to a number");
            }
            String username = p.getProperty("username", "");
            String password = p.getProperty("password", "");
            if(hostname == null){
                throw new SQLException("No hostname provided in " + filePath);
            }
            return new Connection(hostname, port, username, password);
        }
        
        String hostname;
        int port;
        String username;
        String password;
        private Connection(String hostname, int port, String username, String password){
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;            
        }
    }
    
    /**
     * 
     * @param c
     * @param query
     * @param params
     * @return 
     */
    protected abstract Set raw_query(Connection c, String query);
    
    public Set query(Connection c, String query, Object ... params) throws SQLException{
        Statics.SetLastConnection(c);        
        return this.raw_query(c, escape(c, query, params));
    }    
    
    public Set query(String query, Object ... params) throws SQLException{
        return query(Statics.GetLastConnection(), query, params);
    }
    
    public Set query(String query) throws SQLException{
        return query(Statics.GetLastConnection(), query);
    }
    
    protected String escape(Connection c, String query, Object[] params) throws SQLException{
        int prepared = 0;
        boolean failure = false;
        for(char ch : query.toCharArray()){
            if(ch == '?')
                prepared++;
        }
        if(prepared != params.length){
            failure = true;
        }
        StringBuilder b = new StringBuilder();
        int index = 0;
        for(char ch : query.toCharArray()){
            if(ch == '?'){
                try{
                    Object o = params[index];
                    index++;
                    b.append(this.sanitize(c, o));
                } catch(IndexOutOfBoundsException e){
                    failure = true;
                    break;
                }
            } else {
                b.append(ch);
            }
        }
        if(failure){
            throw new SQLException("Invalid number of parameters sent to query. Expected " + prepared + " argument"
                        + (prepared!=1?"s":"") + " but recieved " + params.length);
        }
        return b.toString();
    }
    
    /**
     * Provides a short, efficient query that allows for a connection test to happen,
     * without changing the database at all. 
     * <a href='http://stackoverflow.com/questions/3668506/efficient-sql-test-query-that-will-work-across-all-or-most-databases'>See here.</a>
     * @return 
     */
    protected abstract String testQuery();
    
    protected abstract String sanitize(Connection c, Object o) throws SQLException;
    
    public void connect(Connection c) throws SQLException{
        query(c, testQuery());
    }
}
