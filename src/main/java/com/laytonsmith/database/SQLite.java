package com.laytonsmith.database;

import java.util.Set;
import java.sql.Connection;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class SQLite extends DB{

    @Override
    protected Set raw_query(com.laytonsmith.database.DB.Connection c, String query) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String testQuery() {
        return "SELECT 1";
    }

    @Override
    protected String sanitize(com.laytonsmith.database.DB.Connection c, Object o) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ex) {
            throw new SQLException("Cannot load SQLite. Check your installation and try again");
        }
        
        java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");       
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
