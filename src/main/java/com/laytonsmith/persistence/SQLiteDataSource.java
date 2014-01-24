package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistence.io.ConnectionMixin;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private static final String TABLE_NAME = "persistance"; //Note the misspelling!
	private Connection connection;
	private String path;
	private ConnectionMixin mixin;
	private long lastConnected = 0;
	
	private SQLiteDataSource(){
		
	}
	
	public SQLiteDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);		
		mixin = getConnectionMixin();		
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
			path = mixin.getPath();
			connect();
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (`" + KEY_COLUMN + "` TEXT PRIMARY KEY,"
					+ " `" + VALUE_COLUMN + "` TEXT)");
			lastConnected = System.currentTimeMillis();
		} catch (ClassNotFoundException | UnsupportedOperationException | IOException | SQLException ex) {
			throw new DataSourceException("An error occured while setting up a connection to the SQLite database", ex);
		} 
	}
	
	private void connect() throws IOException, SQLException{
		boolean needToConnect = false;
		if(connection == null){
			needToConnect = true;
		} else if(connection.isClosed()){
			needToConnect = true;
		} else if(lastConnected < System.currentTimeMillis() - 10000){
			// If we connected more than 10 seconds ago, we should re-test
			// the connection explicitely, because isClosed may return false,
			// even if the connection will fail. The only real way to test
			// if the connection is actually open is to run a test query, but
			// doing that too often will cause unneccessary delay, so we
			// wait an arbitrary amount, in this case, 10 seconds.
			// http://stackoverflow.com/questions/3668506/efficient-sql-test-query-or-validation-query-that-will-work-across-all-or-most
			try {
				connection.createStatement().execute("SELECT 1");
				// Nope, don't need to connect.
			} catch(SQLException ex){
				// Need to connect, since this broke.
				needToConnect = true;
			}
		}
		if(needToConnect){
			connection = DriverManager.getConnection("jdbc:sqlite:" + mixin.getPath());
		}
	}
	
	@Override
	public void disconnect() throws DataSourceException {
		try {
			if(connection != null){
					connection.close();
					connection = null; // Speeds up re-initialization
			}
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	public Set<String[]> keySet(String[] keyBase) throws DataSourceException{
		try{
			connect();
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT `" + KEY_COLUMN + "` FROM `" + TABLE_NAME + "` WHERE `" 
					+ KEY_COLUMN + "` LIKE '" + StringUtils.Join(keyBase, ".") + "%'");
			lastConnected = System.currentTimeMillis();
			Set<String[]> list = new HashSet<>();
			while(rs.next()){
				list.add(rs.getString(KEY_COLUMN).split("\\."));
			}
			return list;
		} catch (IOException | SQLException ex) {
			throw new DataSourceException("Could not retrieve key set from SQLite connection " + path, ex);
		}
	}

	@Override
	public String get0(String[] key) throws DataSourceException {
		try{
			connect();
			PreparedStatement statement = connection.prepareStatement("SELECT `" + VALUE_COLUMN + "` FROM `" + TABLE_NAME + "` WHERE `" + KEY_COLUMN + "`=?");
			statement.setString(1, StringUtils.Join(key, "."));
			ResultSet rs = statement.executeQuery();
			lastConnected = System.currentTimeMillis();
			if(rs.next()){
				return rs.getString(VALUE_COLUMN);
			} else {
				return null;
			}
		} catch(IOException | SQLException e){
			throw new DataSourceException("Could not get key from SQLite connection " + path, e);
		}
	}

	@Override
	protected Map<String[], String> getValues0(String[] leadKey) throws DataSourceException {
		try {
			connect();
			PreparedStatement statement = connection.prepareStatement("SELECT `" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "` FROM `" + TABLE_NAME
				+ "` WHERE `" + KEY_COLUMN + "` LIKE '" + StringUtils.Join(leadKey, ".") + "%'");
			ResultSet rs = statement.executeQuery();
			lastConnected = System.currentTimeMillis();
			Map<String[], String> ret = new HashMap<>();
			while(rs.next()){
				String key = rs.getString(KEY_COLUMN);
				String value = rs.getString(VALUE_COLUMN);
				ret.put(key.split("\\."), value);
			}
			return ret;
		} catch(IOException | SQLException e){
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
			PreparedStatement statement = connection.prepareStatement("INSERT OR REPLACE INTO `" + TABLE_NAME + "` (`" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "`) VALUES (?, ?)");
			statement.setString(1, StringUtils.Join(key, "."));
			statement.setString(2, value);
			boolean success = statement.executeUpdate() > 0;
			lastConnected = System.currentTimeMillis();
			return success;
		} catch(Exception e){
			throw new DataSourceException("Could not set key in SQLite connection " + path, e);			
		}
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		if(hasKey(key)){
			try{
				connect();				
				PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + TABLE_NAME + "` WHERE `" + KEY_COLUMN + "`=?");
				statement.setString(1, StringUtils.Join(key, "."));
				statement.executeUpdate();
				lastConnected = System.currentTimeMillis();
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

	@Override
	public String docs() {
		return "SQLite {sqlite://path/to/db/file.db} This type store data in a SQLite database."
			+ " All the pros and cons of MySQL apply here. The database will contain a lone table"
				+ " named " + TABLE_NAME + ", with two columns, " + KEY_COLUMN + " and " + VALUE_COLUMN;
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			connection.prepareStatement("BEGIN TRANSACTION").execute();
			lastConnected = System.currentTimeMillis();
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
			lastConnected = System.currentTimeMillis();
		} catch (SQLException ex) {
			Logger.getLogger(SQLiteDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
