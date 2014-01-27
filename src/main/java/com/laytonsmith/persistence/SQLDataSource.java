package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public abstract class SQLDataSource extends AbstractDataSource {
	private static final String KEY_COLUMN = "key";
	private static final String VALUE_COLUMN = "value";
	private Connection connection;
	private long lastConnected = 0;

	protected SQLDataSource() {
		//
	}

	protected SQLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
	}

	/**
	 * Gets the connection object. There is no guarantee it will be connected or
	 * valid, so {@link #connect()} should be called first if necessary.
	 *
	 * @return
	 */
	protected Connection getConnection() {
		return connection;
	}

	/**
	 * Gets the name of the column that holds the keys.
	 *
	 * @return
	 */
	protected String getKeyColumn() {
		return KEY_COLUMN;
	}

	/**
	 * Gets the name of the column that holds the values.
	 *
	 * @return
	 */
	protected String getValueColumn() {
		return VALUE_COLUMN;
	}

	/**
	 * Gets the table name that the values should be stored in.
	 *
	 * @return
	 */
	protected abstract String getTable();

	/**
	 * Gets the connection string that is used to establish a new connection, if
	 * needed.
	 *
	 * @return
	 */
	protected abstract String getConnectionString();

	/**
	 * Gets the escaped table name.
	 *
	 * @return
	 */
	private String getEscapedTable() {
		return getTable().replace("`", "``");
	}

	/**
	 * All calls to connect must have a corresponding call to disconnect() in a
	 * finally block.
	 */
	protected void connect() throws IOException, SQLException {
		boolean needToConnect = false;
		if (connection == null) {
			needToConnect = true;
		} else if (connection.isClosed()) {
			needToConnect = true;
		} else if (lastConnected < System.currentTimeMillis() - 10000) {
			// If we connected more than 10 seconds ago, we should re-test
			// the connection explicitely, because isClosed may return false,
			// even if the connection will fail. The only real way to test
			// if the connection is actually open is to run a test query, but
			// doing that too often will cause unneccessary delay, so we
			// wait an arbitrary amount, in this case, 10 seconds.
			try {
				if(!connection.isValid(3)){
					needToConnect = true;
				}
			} catch(AbstractMethodError ex){
				// isValid was added later, some connection types may not have that method.
				try {
					connection.createStatement().execute(getTestQuery());
				} catch(SQLException e){
					needToConnect = true;
				}
			}
		}
		if (needToConnect) {
			connection = DriverManager.getConnection(getConnectionString());
		}
	}
	
	/**
	 * If a connection type doesn't support isValid, and "SELECT 1" won't work
	 * as a test query, this should be overridden.
	 * @return 
	 */
	protected String getTestQuery(){
		return "SELECT 1";
	}

	@Override
	public void disconnect() throws DataSourceException {
		try {
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}
	
	@Override
	public Set<String[]> keySet(String[] keyBase) throws DataSourceException {
		String searchPrefix = StringUtils.Join(keyBase, ".");
		try {
			connect();
			PreparedStatement statement = connection.prepareStatement("SELECT `" + KEY_COLUMN + "` FROM `" + getEscapedTable() + "` WHERE `" + KEY_COLUMN + "` LIKE ?");
			statement.setString(1, StringUtils.Join(keyBase, ".") + "%");
			Set<String[]> set = new HashSet<>();
			try(ResultSet result = statement.executeQuery()){
				while(result.next()){
					set.add(result.getString(KEY_COLUMN).split("\\."));
				}
			}
			lastConnected = System.currentTimeMillis();
			return set;
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	public String get0(String[] key) throws DataSourceException {
		try {
			connect();
			PreparedStatement statement = connection.prepareStatement("SELECT `" + VALUE_COLUMN + "` FROM `" + getEscapedTable() + "` WHERE `" + KEY_COLUMN + "`=? LIMIT 1");
			statement.setString(1, StringUtils.Join(key, "."));
			String ret = null;
			try (ResultSet result = statement.executeQuery()) {
				if(result.next()){
					ret = result.getString(VALUE_COLUMN);
				}
			}
			lastConnected = System.currentTimeMillis();
			return ret;
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		try {
			connect();
			if(value == null){
				clearKey0(dm, key);
			} else {
				PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + getEscapedTable() + "` (`" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "`) VALUES (?, ?)");
				statement.setString(1, StringUtils.Join(key, "."));
				statement.setString(2, value);
				statement.executeUpdate();
			}
			lastConnected = System.currentTimeMillis();
			return true;
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	protected Map<String[], String> getValues0(String[] leadKey) throws DataSourceException {
		try {
			connect();
			PreparedStatement statement = connection.prepareStatement("SELECT `" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "` FROM `" + getEscapedTable() + "` WHERE `" + KEY_COLUMN + "` LIKE ?");
			statement.setString(1, StringUtils.Join(leadKey, ".") + "%");
			Map<String[], String> map = new HashMap<>();
			try (ResultSet results = statement.executeQuery()){
				while(results.next()){
					map.put(results.getString(KEY_COLUMN).split("\\."), results.getString(VALUE_COLUMN));
				}
			}
			lastConnected = System.currentTimeMillis();
			return map;
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}
	
	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		if(hasKey(key)){
			try{
				connect();				
				PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + getTable() + "` WHERE `" + KEY_COLUMN + "`=?");
				statement.setString(1, StringUtils.Join(key, "."));
				statement.executeUpdate();
				lastConnected = System.currentTimeMillis();
			} catch(Exception e){
				throw new DataSourceException(e.getMessage(), e);
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
	
	protected void updateLastConnected(){
		lastConnected = System.currentTimeMillis();
	}

}
