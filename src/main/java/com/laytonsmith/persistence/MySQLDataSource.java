package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
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
 */
@datasource("mysql")
public class MySQLDataSource extends AbstractDataSource{
	
	/* These values may not be changed without creating an upgrade routine */
	private static final String KEY_COLUMN = "key";
	private static final String VALUE_COLUMN = "value";
	Connection connection;
	private String host;
	private int port;
	private String username;
	private String password;
	private String database;
	private String table;
	
	private MySQLDataSource(){
		
	}
	
	public MySQLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);
		try {
			Class.forName(com.mysql.jdbc.Driver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new DataSourceException("Could not instantiate a MySQL data source, no driver appears to exist.", ex);
		}
		host = uri.getHost();
		if(host == null){
			throw new DataSourceException("Invalid URI specified for data source \"" + uri.toString() + "\"");
		}
		port = uri.getPort();
		if(port < 0){
			port = 3306;
		}
		if(uri.getUserInfo() != null){
			String[] split = uri.getUserInfo().split(":");
			username = split[0];
			if(split.length > 1){
				password = split[1];
			}
		}
		if(uri.getPath().split("/").length != 3 || !uri.getPath().startsWith("/")){
			throw new DataSourceException("Invalid path information for mysql connection \"" + uri.toString() + "\"."
					+ " Path requires a database name and a table name, for instance \"/testDatabase/tableName");
		} else {
			String [] split = uri.getPath().split("/");
			//First one should be empty
			database = split[1];
			table = split[2];
		}
		//Escape any quotes in the table name, because we can't use prepared statements here
		table = table.replace("`", "``");
		try {
			try {
				connect();
				//Create the table if it doesn't exist
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + table + "` (`" + KEY_COLUMN + "` TEXT, `" + VALUE_COLUMN + "` TEXT)");
			} finally {
				disconnect();
			}
		} catch (IOException | SQLException ex) {
			throw new DataSourceException("Could not connect to MySQL data source \"" + uri.toString() + "\": " + ex.getMessage(), ex);
		}
		
	}
	
	/**
	 * All calls to connect must have a corresponding call to disconnect() in
	 * a finally block.
	 */
	private void connect() throws IOException, SQLException{
		String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + database + "?generateSimpleParameterMetadata=true"
					+ "&jdbcCompliantTruncation=false"
					+ (username == null ? "" : "&user=" + URLEncoder.encode(username, "UTF-8"))
					+ (password == null ? "" : "&password=" + URLEncoder.encode(password, "UTF-8"));
		connection = DriverManager.getConnection(connectionString);		
	}
	
	private void disconnect() throws SQLException{
		if(connection != null){
			connection.close();
		}
	}

	@Override
	public Set<String[]> keySet(String[] keyBase) throws DataSourceException {
		String searchPrefix = StringUtils.Join(keyBase, ".");
		try {
			try {
				connect();
				PreparedStatement statement = connection.prepareStatement("SELECT `" + KEY_COLUMN + "` FROM `" + table + "` WHERE `" + KEY_COLUMN + "` LIKE ?");
				statement.setString(1, StringUtils.Join(keyBase, ".") + "%");
				Set<String[]> set = new HashSet<>();
				try(ResultSet result = statement.executeQuery()){
					while(result.next()){
						set.add(result.getString(KEY_COLUMN).split("\\."));
					}
				}
				return set;
			} finally {
				disconnect();
			}
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	public String get0(String[] key) throws DataSourceException {
		try {
			try {
				connect();
				PreparedStatement statement = connection.prepareStatement("SELECT `" + VALUE_COLUMN + "` FROM `" + table + "` WHERE `" + KEY_COLUMN + "`=? LIMIT 1");
				statement.setString(1, StringUtils.Join(key, "."));
				try (ResultSet result = statement.executeQuery()) {
					String ret = null;
					if(result.next()){
						ret = result.getString(VALUE_COLUMN);
					}
					return ret;
				}
			} finally {
				disconnect();
			}
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		try {
			try {
				connect();
				if(value == null){
					PreparedStatement statement = connection.prepareStatement("DELETE FROM `" + table + "` WHERE `" + KEY_COLUMN + "`=?");
					statement.setString(1, StringUtils.Join(key, "."));
					statement.executeUpdate();
				} else {
					PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + table + "` (`" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "`) VALUES (?, ?)");
					statement.setString(1, StringUtils.Join(key, "."));
					statement.setString(2, value);
					statement.executeUpdate();
				}
				return true;
			} finally {
				disconnect();
			}
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	protected Map<String[], String> getValues0(String[] leadKey) throws DataSourceException {
		try {
			try {
				connect();
				PreparedStatement statement = connection.prepareStatement("SELECT `" + KEY_COLUMN + "`, `" + VALUE_COLUMN + "` FROM `" + table + "` WHERE `" + KEY_COLUMN + "` LIKE ?");
				statement.setString(1, StringUtils.Join(leadKey, ".") + "%");
				Map<String[], String> map = new HashMap<>();
				try (ResultSet results = statement.executeQuery()){
					while(results.next()){
						map.put(results.getString(KEY_COLUMN).split("\\."), results.getString(VALUE_COLUMN));
					}
				}
				return map;
			} finally {
				disconnect();
			}
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
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
		return "MySQL {mysql://[user[:password]@]host[:port]/database/table}"
			+ " This type stores data in a MySQL database. Unlike the"
			+ " file based systems, this is extremely efficient, but"
			+ " requires a database connection already set up to work."
			+ " This also always allows for simultaneous connections"
			+ " from multiple data sink/sources at once, which is not"
			+ " possible without the potential for corruption in file"
			+ " based data sources, without risking either data corruption,"
			+ " or extremely low efficiency. The layout of the table"
			+ " in the database is required to be of a specific format:"
			+ " CREATE TABLE IF NOT EXISTS `table` (`" + KEY_COLUMN + "` TEXT, `" + VALUE_COLUMN + "` TEXT);";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			connection.createStatement().execute("START TRANSACTION");
		} catch (SQLException ex) {
			Logger.getLogger(MySQLDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		try {
			if(rollback){
				connection.createStatement().execute("ROLLBACK");
			} else {
				connection.createStatement().execute("COMMIT");
			}
		} catch (SQLException ex) {
			Logger.getLogger(MySQLDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
}
