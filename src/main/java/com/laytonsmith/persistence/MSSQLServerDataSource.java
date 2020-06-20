package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StreamUtils;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
@datasource("mssql")
public final class MSSQLServerDataSource extends SQLDataSource {

	private MSSQLServerDataSource() {
		super();
	}

	private String host;
	private String instance;
	private int port;
	private String username;
	private String password;
	private String database;
	private String table;
	private Map<String, String> extraParameters = new HashMap<>();

	private String connectionString;

	public MSSQLServerDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options)
			throws DataSourceException {
		super(uri, options);
		try {
			Class.forName(com.microsoft.sqlserver.jdbc.SQLServerDriver.class.getName());
		} catch (ClassNotFoundException ex) {
			throw new DataSourceException("Could not instantiate a MSSQL data source, no driver appears to exist.", ex);
		}
		host = uri.getHost();
		if(host == null) {
			throw new DataSourceException("Invalid URI specified for data source \"" + uri.toString() + "\"");
		}
		if(host.contains("\\")) {
			String[] split = host.split("\\\\");
			host = split[0];
			instance = split[1];
		}
		port = uri.getPort();
		if(port < 0) {
			port = 1433;
		}
		if(uri.getUserInfo() != null) {
			String[] split = uri.getUserInfo().split(":");
			username = split[0];
			if(split.length > 1) {
				password = split[1];
			}
		}
		if(uri.getPath().split("/").length != 3 || !uri.getPath().startsWith("/")) {
			throw new DataSourceException("Invalid path information for mssql connection \"" + uri.toString() + "\"."
					+ " Path requires a database name and a table name, for instance \"/testDatabase/tableName");
		} else {
			String[] split = uri.getPath().split("/");
			//First one should be empty
			database = split[1];
			table = split[2];
		}

		extraParameters.putAll(WebUtility.getQueryMap(uri.getQuery()));
		connectionString = "jdbc:sqlserver://" + host;
		if(instance != null) {
			connectionString += "\\" + instance;
		}

		connectionString += ":" + port;
		connectionString += ";";
		if(username != null) {
			connectionString += "user=" + username + ";";
		}
		if(password != null) {
			connectionString += "password=" + password + ";";
		}

		connectionString += "databaseName=" + database + ";";

		for(Map.Entry<String, String> params : extraParameters.entrySet()) {
			connectionString += params.getKey() + "=" + params.getValue() + ";";
		}

		try {
			connect();
			//Create the table if it doesn't exist
			//The columns in the table
			for(String query : getTableCreationQueries(database, table)) {
				try(Statement statement = getConnection().createStatement()) {
					statement.executeUpdate(query);
				}
			}
		} catch (IOException | SQLException ex) {
			throw new DataSourceException("Could not connect to MySQL data source \""
					+ (password != null ? uri.toString().replace(password, "<password>") : uri.toString()) + "\""
					+ " (using \""
					+ (password != null ? getConnectionString().replace(password, "<password>") : getConnectionString())
					+ "\" to connect): " + ex.getMessage(), ex);
		}
	}

	@Override
	protected String getTable() {
		return table;
	}

	@Override
	protected String getConnectionString() {
		return connectionString;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			try(Statement statement = getConnection().createStatement()) {
				statement.execute("BEGIN TRANSACTION");
			}
		} catch (SQLException ex) {
			Logger.getLogger(MSSQLServerDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		try {
			try(Statement statement = getConnection().createStatement()) {
				statement.execute(rollback ? "ROLLBACK" : "COMMIT");
			}
		} catch (SQLException ex) {
			Logger.getLogger(MSSQLServerDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private InputStream getKeyHash(String key) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(key.getBytes());
			return StreamUtils.GetInputStream(digest.digest());
		} catch (NoSuchAlgorithmException ex) {
			throw new Error(ex);
		}
	}

	@Override
	protected boolean set0(DaemonManager dm, String[] key, String value)
			throws ReadOnlyException, DataSourceException, IOException {
		try {
			connect();
			if(value == null) {
				clearKey0(dm, key);
			} else {
				try(PreparedStatement statement = getConnection()
						.prepareStatement("EXEC [dbo].[" + table + "_upsert] @keyHash = ?, @key = ?, @value = ?")) {
					String joinedKey = StringUtils.Join(key, ".");
					statement.setBinaryStream(1, getKeyHash(joinedKey));
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
	protected Map<String[], String> getValues0(String[] leadKey) throws DataSourceException {
		try {
			connect();
			Map<String[], String> map;
			try(PreparedStatement statement = connection.prepareStatement("SELECT [key], [value] FROM [dbo].["
					+ table + "]"
					+ " WHERE [key] LIKE ?")) {
				statement.setString(1, StringUtils.Join(leadKey, ".") + "%");
				map = new HashMap<>();
				try(ResultSet results = statement.executeQuery()) {
					while(results.next()) {
						map.put(results.getString(getKeyColumn()).split("\\."), results.getString(getValueColumn()));
					}
				}
				updateLastConnected();
			}
			return map;
		} catch (SQLException | IOException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key)
			throws ReadOnlyException, DataSourceException, IOException {
		try {
			connect();
			try(PreparedStatement statement = getConnection()
					.prepareStatement("DELETE FROM [dbo].[" + table + "] WHERE [key_hash]=?")) {
				String joinedKey = StringUtils.Join(key, ".");
				statement.setBinaryStream(1, getKeyHash(joinedKey));
				statement.executeUpdate();
			}
			updateLastConnected();
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		}
	}

	@Override
	protected String get0(String[] key) throws DataSourceException {
		try {
			connect();
			String ret;
			try(PreparedStatement statement = getConnection().prepareStatement("SELECT TOP(1) [" + getValueColumn()
					+ "] FROM [dbo].[" + getTable() + "] WHERE [key_hash]=?")) {
				String joinedKey = StringUtils.Join(key, ".");
				statement.setBinaryStream(1, getKeyHash(joinedKey));
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
	public String docs() {
		String docs = "MSSQL {mssql://[user[:password]@]host[\\instanceName][:port]/database/table?extraParameters}"
				+ " This type stores data in a MSSQL database. Unlike the"
				+ " file based systems, this is extremely efficient, but"
				+ " requires a database connection already set up to work."
				+ " This also always allows for simultaneous connections"
				+ " from multiple data sink/sources at once, which is not"
				+ " possible without the potential for corruption in file"
				+ " based data sources, without risking either data corruption,"
				+ " or extremely low efficiency. To set up the database properly, it is required to"
				+ " run several commands, these are run automatically on first run, but you may choose"
				+ " to manually run the following sequence yourself: <%SYNTAX|sql|"
				+ StringUtils.Join(getTableCreationQueries("testDatabase", "testTable"), "%>\n\n<%SYNTAX|sql|")
				+ "%>\n\n"
				+ "The allowed extra parameters in the connection string follows the general values described"
				+ " [https://docs.microsoft.com/en-us/sql/connect/jdbc/setting-the-connection-properties"
				+ "?view=sql-server-ver15 here],"
				+ " but the format follows a standard URI query string syntax,"
				+ " and the protocol is \"mssql\" rather than \"jdbc:sqlserver\"."
				+ " For instance, the following connection string"
				+ " <code>jdbc:sqlserver://localhost\\MSSQLDB;user=username;password=1234;applicationIntent=ReadOnly;"
				+ "applicationName=myApp</code>"
				+ " with a PN connection to a table named \"myTable\" would be written as"
				+ " <code>mssql://user:password@localhost\\myInstance:1433/myDatabase/myTable"
				+ "?applicationIntent=ReadOnly&applicationName=myApp</code>."
				+ " The host information is not optional in MethodScript. The port, if not specified, defaults to 1433."
				+ " Additional configuration is needed for Windows Authentication, and for general configuration of"
				+ " SQL Server, but it is the same for general SQL connections using query()."
				+ " Please see the detailed information under the SQL Server section on the [[SQL]] page for further"
				+ " information.";
		return docs;
	}

	@SuppressWarnings({"checkstyle:operatorwrap", "checkstyle:nowhitespacebefore", "checkstyle:separatorwrap"})
	public String[] getTableCreationQueries(String database, String tableName) {
		return new String[] {
				"USE [" + database + "]\n"
				,
				"/****** Object:  Table [dbo].[" + tableName + "] ******/\n" +
				"SET ANSI_NULLS ON\n"
				,
				"SET QUOTED_IDENTIFIER ON\n"
				,
				"IF NOT (EXISTS (SELECT * \n" +
				"                 FROM INFORMATION_SCHEMA.TABLES \n" +
				"                 WHERE TABLE_SCHEMA = 'dbo' \n" +
				"                 AND  TABLE_NAME = '" + tableName + "'))\n" +
				"BEGIN\n" +
				"\n" +
				"	CREATE TABLE [dbo].[" + tableName + "](\n" +
				"		-- This is the binary hash of the unlimited length key column, so the table may have a"
				+ " primary key.\n" +
				"		[key_hash] [BINARY](16) NOT NULL,\n" +
				"		-- The key itself, stored for plaintext readability, and full text searches for getting"
				+ " values.\n" +
				"		[key] [VARCHAR](MAX) NOT NULL,\n" +
				"		-- The value itself, which may be null.\n" +
				"		[value] [NVARCHAR](MAX) NULL,\n" +
				"	 CONSTRAINT [PK_" + tableName + "] PRIMARY KEY CLUSTERED \n" +
				"	(\n" +
				"		[key_hash] ASC\n" +
				"	)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON,"
				+ " ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]\n" +
				"	) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]\n" +
				"END\n"
				,
				"DROP PROCEDURE IF EXISTS [dbo].[" + tableName + "_upsert]\n"
				,
				"CREATE PROCEDURE [dbo].[" + tableName + "_upsert] ( @keyHash BINARY(16), @key VARCHAR(MAX),"
				+ " @value NVARCHAR(MAX) )\n" +
				"AS \n" +
				"  SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;\n" +
				"  BEGIN TRAN\n" +
				" \n" +
				"    IF EXISTS ( SELECT * FROM [dbo].[" + tableName + "] WITH (UPDLOCK)"
				+ " WHERE [key_hash] = @keyHash )\n" +
				" \n" +
				"      UPDATE [dbo].[" + tableName + "]\n" +
				"         SET [value] = @value\n" +
				"       WHERE [key] = @key;\n" +
				" \n" +
				"    ELSE \n" +
				" \n" +
				"      INSERT [dbo].[" + tableName + "] ( [key_hash], [key], [value] )\n" +
				"      VALUES (  @keyHash, @key, @value );\n" +
				" \n" +
				"  COMMIT"
		};
	}

	@Override
	public Version since() {
		return MSVersion.V3_3_4;
	}

}
