package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
@datasource("mysql")
public class MySQLDataSource extends SQLDataSource {
	
	/* These values may not be changed without creating an upgrade routine */

	private String host;
	private int port;
	private String username;
	private String password;
	private String database;
	private String table;
	
	private MySQLDataSource(){
		super();
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
			connect();
			//Create the table if it doesn't exist
			Statement statement = getConnection().createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + table + "` (`" + getKeyColumn() + "` TEXT, `" + getValueColumn() + "` TEXT)");
		} catch (IOException | SQLException ex) {
			throw new DataSourceException("Could not connect to MySQL data source \"" + uri.toString() + "\": " + ex.getMessage(), ex);
		}
		
	}

	@Override
	protected String getConnectionString() {
		try {
			return "jdbc:mysql://" + host + ":" + port + "/" + database + "?generateSimpleParameterMetadata=true"
					+ "&jdbcCompliantTruncation=false"
					+ (username == null ? "" : "&user=" + URLEncoder.encode(username, "UTF-8"))
					+ (password == null ? "" : "&password=" + URLEncoder.encode(password, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			throw new Error(ex);
		}
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
			+ " CREATE TABLE IF NOT EXISTS `table` (`" + getKeyColumn() + "` TEXT, `" + getValueColumn() + "` TEXT);";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			getConnection().createStatement().execute("START TRANSACTION");
		} catch (SQLException ex) {
			Logger.getLogger(MySQLDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		try {
			if(rollback){
				getConnection().createStatement().execute("ROLLBACK");
			} else {
				getConnection().createStatement().execute("COMMIT");
			}
		} catch (SQLException ex) {
			Logger.getLogger(MySQLDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected String getTable() {
		return table;
	}
	
}
