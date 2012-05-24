package com.laytonsmith.database;

import com.mysql.jdbc.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author layton
 */
public class SQLite extends DB{

    @Override
    protected Set raw_query(CConnection c, String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String testQuery() {
        return "SELECT 1";
    }
    
    private static PreparedStatement GetPreparedStatement(String query, CConnection cconn, Connection conn) throws SQLException{
        PreparedStatement prep = conn.prepareStatement(query);
        return prep;        
    }

    @Override
    protected String sanitize(CConnection c, Object o) throws SQLException {       
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Set do_query(CConnection c, String query, Object[] params) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            throw new SQLException("Cannot load SQLite. Check your installation and try again");
        }
        
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + c.hostname);
        PreparedStatement prep = SQLite.GetPreparedStatement(query, c, conn);
        int i = 0;
        for(Object o : params){
            i++;
            prep.setObject(i, o);
        }
        prep.addBatch();
        throw new UnsupportedOperationException("Not supported yet.");
    }   
    
}
