package com.laytonsmith.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        public static Connection GetConnection(String connectionName){
            if(cache.containsKey(connectionName)){
                return cache.get(connectionName);
            }
            Connection c = ConnectionLookup(connectionName);
            cache.put(connectionName, c);
            return c;
        }
        private static Connection ConnectionLookup(String name){
            Connection c = new Connection();
            
            return c;
        }
    }
    
    /**
     * 
     * @param c
     * @param query
     * @param params
     * @return 
     */
    protected abstract Set query0(Connection c, String query, Object [] params);
    
    public Set query(Connection c, String query, Object ... params){
        Statics.SetLastConnection(c);
        return this.query0(c, query, params);
    }
    
    public Set query(Connection c, String query){
        return query(c, query);
    }
    
    public Set query(String query, Object ... params){
        return query(Statics.GetLastConnection(), query, params);
    }
    
    public Set query(String query){
        return query(Statics.GetLastConnection(), query);
    }
    
    /**
     * Provides a short, efficient query that allows for a connection test to happen,
     * without changing the database at all. 
     * <a href='http://stackoverflow.com/questions/3668506/efficient-sql-test-query-that-will-work-across-all-or-most-databases'>See here.</a>
     * @return 
     */
    protected abstract String testQuery();
    
    public void connect(Connection c){
        query(c, testQuery());
    }
}
