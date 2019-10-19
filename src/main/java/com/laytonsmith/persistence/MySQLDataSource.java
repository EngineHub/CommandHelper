package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@datasource("mysql")
public class MySQLDataSource extends SQLDataSource {

	/* These values may not be changed without creating an upgrade routine */
	private static final String KEY_HASH_COLUMN = "key_hash";
	private String host;
	private int port;
	private String username;
	private String password;
	private String database;
	private String table;
	private Map<String, String> extraParameters = new HashMap<>();

	private MySQLDataSource() {
		super();
	}

	public MySQLDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
		try {
			Class.forName(com.mysql.cj.jdbc.Driver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new DataSourceException("Could not instantiate a MySQL data source, no driver appears to exist.", ex);
		}
		host = uri.getHost();
		if(host == null) {
			throw new DataSourceException("Invalid URI specified for data source \"" + uri.toString() + "\"");
		}
		port = uri.getPort();
		if(port < 0) {
			port = 3306;
		}
		if(uri.getUserInfo() != null) {
			String[] split = uri.getUserInfo().split(":");
			username = split[0];
			if(split.length > 1) {
				password = split[1];
			}
		}
		if(uri.getPath().split("/").length != 3 || !uri.getPath().startsWith("/")) {
			throw new DataSourceException("Invalid path information for mysql connection \"" + uri.toString() + "\"."
					+ " Path requires a database name and a table name, for instance \"/testDatabase/tableName");
		} else {
			String[] split = uri.getPath().split("/");
			//First one should be empty
			database = split[1];
			table = split[2];
		}
		//Escape any quotes in the table name, because we can't use prepared statements here
		table = table.replace("`", "``");
		extraParameters.putAll(WebUtility.getQueryMap(uri.getQuery()));
		try {
			connect();
			//Create the table if it doesn't exist
			//The columns in the table
			try(Statement statement = getConnection().createStatement()) {
				statement.executeUpdate(getTableCreationQuery(table));
			}
		} catch (IOException | SQLException ex) {
			throw new DataSourceException("Could not connect to MySQL data source \""
					+ (password != null ? uri.toString().replace(password, "<password>") : uri.toString()) + "\""
					+ " (using \""
					+ (password != null ? getConnectionString().replace(password, "<password>") : getConnectionString())
					+ "\" to connect): " + ex.getMessage(), ex);
		}

	}

	/**
	 * Returns the table creation query that should be used to create the table specified. This is public for
	 * documentation, but is used internally.
	 *
	 * @param table
	 * @return
	 */
	public final String getTableCreationQuery(String table) {
		return "CREATE TABLE IF NOT EXISTS `" + table + "` (\n"
				+ " -- This is an UNHEX(MD5('key')) binary hash of the unlimited\n"
				+ " -- length key column, so the table may have a primary key.\n"
				+ " `" + KEY_HASH_COLUMN + "` BINARY(16) PRIMARY KEY NOT NULL,\n"
				+ " -- This is the key itself, stored for plaintext readability,\n"
				+ " -- and for full text searches for getting values\n"
				+ " `" + getKeyColumn() + "` TEXT NOT NULL,\n"
				+ " -- The value itself, which may be null\n"
				+ " `" + getValueColumn() + "` MEDIUMTEXT\n"
				+ ")\n"
				+ " -- The engine is InnoDB, to support transactions\n"
				+ "ENGINE = InnoDB,\n"
				+ " -- The charset is utf8, since all keys are utf8, and values are utf8 json\n"
				+ "CHARACTER SET = utf8,\n"
				+ " -- The collation is case sensitive\n"
				+ "COLLATE = utf8_bin,\n"
				+ " -- Table comment\n"
				+ "COMMENT = 'MethodScript storage table'\n"
				+ ";";
	}

	@Override
	protected String getConnectionString() {
		try {
			String s = "jdbc:mysql://" + host + ":" + port + "/" + database + "?generateSimpleParameterMetadata=true"
					+ "&jdbcCompliantTruncation=false"
					+ (username == null ? "" : "&user=" + URLEncoder.encode(username, "UTF-8"))
					+ (password == null ? "" : "&password=" + URLEncoder.encode(password, "UTF-8"));
			if(!extraParameters.isEmpty()) {
				s += "&" + WebUtility.encodeParameters(extraParameters);
			}
			return s;
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
	}

	@Override
	public String get0(String[] key) throws DataSourceException {
		try {
			connect();
			String ret;
			try(PreparedStatement statement = getConnection().prepareStatement("SELECT `" + getValueColumn() + "` FROM `"
					+ getEscapedTable() + "` WHERE `" + KEY_HASH_COLUMN + "`=UNHEX(MD5(?))"
					+ " LIMIT 1")) {
				String joinedKey = StringUtils.Join(key, ".");
				statement.setString(1, joinedKey);
				ret = null;
				try(ResultSet result = statement.executeQuery()) {
					if(result.next()) {
						ret = result.getString(getValueColumn());
					}
				}
			}
			updateLastConnected();
			return ret;
		} catch (SQLException | IOException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		try {
			connect();
			if(value == null) {
				clearKey0(dm, key);
			} else {
				try(PreparedStatement statement = getConnection().prepareStatement("REPLACE INTO"
						+ " `" + getEscapedTable() + "`"
						+ " (`" + KEY_HASH_COLUMN + "`, `" + getKeyColumn() + "`, `" + getValueColumn() + "`)"
						+ " VALUES (UNHEX(MD5(?)), ?, ?)")) {
					String joinedKey = StringUtils.Join(key, ".");
					statement.setString(1, joinedKey);
					statement.setString(2, joinedKey);
					statement.setString(3, value);
					statement.executeUpdate();
				}
			}
			updateLastConnected();
			return true;
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		if(hasKey(key)) {
			try {
				connect();
				try(PreparedStatement statement = getConnection().prepareStatement("DELETE FROM `" + getEscapedTable() + "`"
						+ " WHERE `" + KEY_HASH_COLUMN + "`=UNHEX(MD5(?))")) {
					String joinedKey = StringUtils.Join(key, ".");
					statement.setString(1, joinedKey);
					statement.executeUpdate();
				}
				updateLastConnected();
			} catch (Exception e) {
				throw new DataSourceException(e.getMessage(), e);
			}
		}
	}

	@Override
	public String docs() {
		return "MySQL {mysql://[user[:password]@]host[:port]/database/table?extraParameters}"
				+ " This type stores data in a MySQL database. Unlike the"
				+ " file based systems, this is extremely efficient, but"
				+ " requires a database connection already set up to work."
				+ " This also always allows for simultaneous connections"
				+ " from multiple data sink/sources at once, which is not"
				+ " possible without the potential for corruption in file"
				+ " based data sources, without risking either data corruption,"
				+ " or extremely low efficiency. The layout of the table"
				+ " in the database is required to be of a specific format: <%SYNTAX|sql|"
				+ getTableCreationQuery("testTable") + "%>\n\n"
				+ "Extra parameters may provided to the MySQL connection, and they are"
				+ " merged with the existing required parameters and sent through as"
				+ " is to the server. They should be in the format \"a=1&b=2\".";
	}

	@Override
	public MSVersion since() {
		return MSVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			try(Statement statement = getConnection().createStatement()) {
				statement.execute("START TRANSACTION");
			}
		} catch (SQLException ex) {
			Logger.getLogger(MySQLDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		try {
			if(rollback) {
				try(PreparedStatement statement = getConnection().prepareStatement("ROLLBACK")) {
					statement.execute();
				}
			} else {
				try(PreparedStatement statement = getConnection().prepareStatement("COMMIT")) {
					statement.execute();
				}
			}
			updateLastConnected();
		} catch (SQLException ex) {
			Logger.getLogger(MySQLDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected String getTable() {
		return table;
	}

}
