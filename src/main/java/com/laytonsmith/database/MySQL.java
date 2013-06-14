package com.laytonsmith.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MySQL extends DB {

	@Override
	protected String testQuery() {
		return "SELECT 1";
	}

	@Override
	protected Object do_query(CConnection c, String query, Object[] params) throws SQLException {
		try {
            Class.forName(com.mysql.jdbc.Driver.class.getName());
        }
        catch (ClassNotFoundException ex) {
            throw new SQLException("Cannot load MySQL. Check your installation and try again");
        }
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://" + c.hostname + "/" + c.database);
        PreparedStatement prep = MySQL.GetPreparedStatement(query, c, conn);
        int i = 0;
        for(Object o : params){
            i++;
            prep.setObject(i, o);
        }
        prep.addBatch();
        if(prep.execute()){
            return prep.getResultSet();
        } else {
            return prep.getUpdateCount();
        }
	}
	
	private static PreparedStatement GetPreparedStatement(String query, CConnection cconn, Connection conn) throws SQLException{
        PreparedStatement prep = conn.prepareStatement(query);
        return prep;        
    }
	
	@Override
	protected Set raw_query(CConnection c, String query) {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}


	@Override
	protected String sanitize(CConnection c, Object o) throws SQLException {
		throw new UnsupportedOperationException("TODO: Not supported yet.");
	}
	
}
