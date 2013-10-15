package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixin;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lsmith
 */
@datasource("sqlite")
public class SQLiteDataSource extends AbstractDataSource{
	
	/* These values may not be changed without creating an upgrade routine */
	private static final String KEY_COLUMN = "key";
	private static final String VALUE_COLUMN = "value";
	private static final String TABLE_NAME = "persistance";
	Connection connection;
	String path;
	ConnectionMixin mixin;
	
	private SQLiteDataSource(){
		
	}
	public SQLiteDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);		
		mixin = getConnectionMixin();		
		try {
			try{
				Class.forName(org.sqlite.JDBC.class.getName());
				path = mixin.getPath();
				connect();
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (`" + KEY_COLUMN + "` TEXT PRIMARY KEY,"
						+ " `" + VALUE_COLUMN + "` TEXT)");
			}finally {
				disconnect();
			}
		} catch (Exception ex) {
			throw new DataSourceException("An error occured while setting up a connection to the SQLite database", ex);
		} 
	}
	
	/**
	 * All calls to connect must have a corresponding call to disconnect() in
	 * a finally block.
	 */
	private void connect() throws IOException, SQLException{
		connection = DriverManager.getConnection("jdbc:sqlite:" + mixin.getPath());		
	}
	
	private void disconnect() throws SQLException{
		if(connection != null){
			connection.close();
		}
	}

	@Override
	public Set<String[]> keySet() throws DataSourceException{
		try{
			try {
				connect();
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("SELECT `" + KEY_COLUMN + "` FROM `" + TABLE_NAME + "`");
				Set<String[]> list = new HashSet<String[]>();
				while(rs.next()){
					list.add(rs.getString(KEY_COLUMN).split("\\."));
				}
				return list;
			} finally{
				disconnect();
			}
		} catch (Exception ex) {
			throw new DataSourceException("Could not retrieve key set from SQLite connection " + path, ex);
		}
	}

	@Override
	public String get0(String[] key) throws DataSourceException {
		try{
			try{
				connect();
				PreparedStatement statement = connection.prepareStatement("SELECT `" + VALUE_COLUMN + "` FROM `" + TABLE_NAME + "` WHERE `" + KEY_COLUMN + "`=?");
				statement.setString(1, StringUtils.Join(key, "."));
				ResultSet rs = statement.executeQuery();
				if(rs.next()){
					return rs.getString(VALUE_COLUMN);
				} else {
					return null;
				}
			} finally {
				disconnect();
			}
		} catch(Exception e){
			throw new DataSourceException("Could not get key from SQLite connection " + path, e);
		}
	}

	@Override
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		if(value == null){
			clearKey(dm, key);
			return true;
		}
		try{
			try{
				connect();
				PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `" + TABLE_NAME + "` (`" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "`) VALUES (?, ?)");
				statement.setString(1, StringUtils.Join(key, "."));
				statement.setString(2, value);
				return statement.executeUpdate() > 0;
			} finally {
				disconnect();
			}
		} catch(Exception e){
			throw new DataSourceException("Could not set key in SQLite connection " + path, e);			
		}
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		if(hasKey(key)){
			try{
				try{
					connect();				
					PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + TABLE_NAME + "` WHERE `" + KEY_COLUMN + "`=?");
					statement.setString(1, StringUtils.Join(key, "."));
					statement.executeUpdate();
				} finally{
					disconnect();
				}
			} catch(Exception e){
				throw new DataSourceException("Could not clear key in SQLite connection " + path, e);
			}
		}
	}		

	@Override
	public void populate() throws DataSourceException {
		//All data is transient
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return new DataSourceModifier[]{DataSourceModifier.TRANSIENT};
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		return new DataSourceModifier[]{DataSourceModifier.HTTP, DataSourceModifier.HTTPS, DataSourceModifier.SSH,
			DataSourceModifier.PRETTYPRINT
		};
	}

	public String docs() {
		return "SQLite {sqlite://path/to/db/file.db} This type store data in a SQLite database."
			+ " All the pros and cons of MySQL apply here. The database will contain a lone table"
				+ " named " + TABLE_NAME + ", with two columns, " + KEY_COLUMN + " and " + VALUE_COLUMN;
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			connection.prepareStatement("BEGIN TRANSACTION").execute();
		} catch (SQLException ex) {
			Logger.getLogger(SQLiteDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		try {
			if(rollback){
				connection.prepareStatement("ROLLBACK TRANSACTION").execute();
			} else {
				connection.prepareStatement("END TRANSACTION").execute();
			}
		} catch (SQLException ex) {
			Logger.getLogger(SQLiteDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
