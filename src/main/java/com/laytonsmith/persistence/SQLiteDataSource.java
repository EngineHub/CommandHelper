package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistence.io.ConnectionMixin;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
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
 *
 */
@datasource("sqlite")
public class SQLiteDataSource extends SQLDataSource {

	/* These values may not be changed without creating an upgrade routine */
	private static final String TABLE_NAME = "persistance"; //Note the misspelling!
	private String path;
	private ConnectionMixin mixin;
	/**
	 * If true, the connection will not be recycled, and will be disconnected
	 * after each call, and re-established before each call. If false, the
	 * connection is recycled using the AbstractDataSource connect logic.
	 *
	 * It appears as though this needs to remain true for connections to work correctly
	 * if multiple connections are established. Further research may need to be done
	 * to establish if this is a Windows only problem, or if there is some other
	 * complicating factor that causes connections to have to continually retry.
	 * Additionally, it may be that the issue could be solved using a more complex
	 * transaction system. Regardless, it seems to work in all cases without transactions,
	 * so long as this remains true.
	 */
	private final static boolean DO_DISCONNECTS = true;
	/**
	 * If the connection takes this long to connect, it will give up, throw an exception,
	 * and continue on. This timeout should be large enough to never cause false positives,
	 * but small enough that the host itself won't decide the connection has completely stalled
	 * out.
	 */
	private final static int TIMEOUT = 30000;

	private SQLiteDataSource(){

	}

	@SuppressWarnings("SleepWhileInLoop")
	public SQLiteDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException{
		super(uri, options);
		mixin = getConnectionMixin();
		try {
			Class.forName(org.sqlite.JDBC.class.getName());
			path = mixin.getPath();
			connect();
			long startTime = System.currentTimeMillis();
			while(true){
				if(System.currentTimeMillis() - TIMEOUT > startTime){
					throw new DataSourceException("Data source at " + uri + " could not connect for "
							+ (TIMEOUT / 1000) + " seconds, so we're giving up on retrying.");
				}
				try {
					try (Statement statement = getConnection().createStatement()) {
						statement.executeUpdate(getTableCreationQuery());
					}
					updateLastConnected();
					break;
				} catch(SQLException ex){
					if(ex.getMessage().startsWith("[SQLITE_BUSY]") || ex.getMessage().equals("database is locked")){
						try {
							Thread.sleep(getRandomSleepTime());
						} catch (InterruptedException ex1) {
							//
						}
					} else {
						throw ex;
					}
				}
			}
		} catch (ClassNotFoundException | UnsupportedOperationException | IOException | SQLException ex) {
			throw new DataSourceException("An error occured while setting up a connection to the SQLite database", ex);
		} finally {
			if(DO_DISCONNECTS){
				disconnect();
			}
		}
	}

	@Override
	protected void connect() throws IOException, SQLException {
		if(DO_DISCONNECTS){
			//Speculative fix. Just kill the connection each time, then renew it.
			if(connection != null){
				connection.close();
			}
			connection = DriverManager.getConnection(getConnectionString());
		} else {
			super.connect();
		}
	}



	/**
	 * {@inheritDoc}
	 *
	 * SQLite connections support INSERT OR REPLACE, which prevents duplicate keys from mattering, so this method needs to be overridden for
	 * SQLite.
	 * @param dm
	 * @param key
	 * @param value
	 * @return
	 * @throws ReadOnlyException
	 * @throws DataSourceException
	 * @throws IOException
	 */
	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		try {
			connect();
			if(value == null){
				clearKey0(dm, key);
			} else {
				long startTime = System.currentTimeMillis();
				while (true) {
					if (System.currentTimeMillis() - TIMEOUT > startTime) {
						throw new DataSourceException("Data source at " + uri + " could not connect for "
								+ (TIMEOUT / 1000) + " seconds, so we're giving up on retrying.");
					}
					try {
						try (PreparedStatement statement = getConnection().prepareStatement("INSERT OR REPLACE INTO `" + TABLE_NAME
								+ "` (`" + getKeyColumn() + "`, `" + getValueColumn() + "`) VALUES (?, ?)")) {
							statement.setString(1, StringUtils.Join(key, "."));
							statement.setString(2, value);
							statement.executeUpdate();
						}
						break;
					} catch(SQLException ex){
						if(ex.getMessage().startsWith("[SQLITE_BUSY]")
								// This one only happens with SETs
								|| ex.getMessage().equals("cannot commit transaction - SQL statements in progress")){
							try {
								Thread.sleep(getRandomSleepTime());
							} catch (InterruptedException ex1) {
								//
							}
						} else {
							throw ex;
						}
					}
				}
			}
			updateLastConnected();
			return true;
		} catch (SQLException ex) {
			throw new DataSourceException(ex.getMessage(), ex);
		} finally {
			if(DO_DISCONNECTS){
				disconnect();
			}
		}
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public Set<String[]> keySet(String[] keyBase) throws DataSourceException {
		String searchPrefix = StringUtils.Join(keyBase, ".");
		try {
			connect();
			Set<String[]> set = new HashSet<>();
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - TIMEOUT > startTime) {
					throw new DataSourceException("Data source at " + uri + " could not connect for "
							+ (TIMEOUT / 1000) + " seconds, so we're giving up on retrying.");
				}
				try {
					try(PreparedStatement statement = getConnection().prepareStatement("SELECT `" + getKeyColumn() + "` FROM `" + getEscapedTable()
							+ "` WHERE `" + getKeyColumn() + "` LIKE ?")){
						statement.setString(1, searchPrefix + "%");
						try(ResultSet result = statement.executeQuery()){
							while(result.next()){
								set.add(result.getString(getKeyColumn()).split("\\."));
							}
						}
					}
					updateLastConnected();
					break;
				} catch(SQLException ex){
					if(ex.getMessage().startsWith("[SQLITE_BUSY]")){
						try {
							Thread.sleep(getRandomSleepTime());
						} catch (InterruptedException ex1) {
							//
						}
					} else {
						throw ex;
					}
				}
			}
			return set;
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		} finally {
			if(DO_DISCONNECTS){
				disconnect();
			}
		}
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	public String get0(String[] key) throws DataSourceException {
		try {
			connect();
			String ret = null;
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - TIMEOUT > startTime) {
					throw new DataSourceException("Data source at " + uri + " could not connect for "
							+ (TIMEOUT / 1000) + " seconds, so we're giving up on retrying.");
				}
				try {
					try (PreparedStatement statement = getConnection().prepareStatement("SELECT `" + getValueColumn() + "` FROM `" + getEscapedTable()
							+ "` WHERE `" + getKeyColumn() + "`=? LIMIT 1")) {
						statement.setString(1, StringUtils.Join(key, "."));
						try (ResultSet result = statement.executeQuery()) {
							if(result.next()){
								ret = result.getString(getValueColumn());
							}
						}
					}
					break;
				} catch(SQLException ex){
					if(ex.getMessage().startsWith("[SQLITE_BUSY]")){
						try {
							Thread.sleep(getRandomSleepTime());
						} catch (InterruptedException ex1) {
							//
						}
					} else {
						throw ex;
					}
				}
			}
			updateLastConnected();
			return ret;
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		} finally {
			if(DO_DISCONNECTS){
				disconnect();
			}
		}
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	protected Map<String[], String> getValues0(String[] leadKey) throws DataSourceException {
		try {
			connect();
			Map<String[], String> map = new HashMap<>();
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - TIMEOUT > startTime) {
					throw new DataSourceException("Data source at " + uri + " could not connect for "
							+ (TIMEOUT / 1000) + " seconds, so we're giving up on retrying.");
				}
				try {
					try (PreparedStatement statement = getConnection().prepareStatement("SELECT `" + getKeyColumn() + "`, `" + getValueColumn() + "` FROM `"
							+ getEscapedTable() + "`" + " WHERE `" + getKeyColumn() + "` LIKE ?")){
						statement.setString(1, StringUtils.Join(leadKey, ".") + "%");
						try (ResultSet results = statement.executeQuery()){
							while(results.next()){
								map.put(results.getString(getKeyColumn()).split("\\."), results.getString(getValueColumn()));
							}
						}
					}
					break;
				} catch(SQLException ex){
					if(ex.getMessage().startsWith("[SQLITE_BUSY]")){
						try {
							Thread.sleep(getRandomSleepTime());
						} catch (InterruptedException ex1) {
							//
						}
					} else {
						throw ex;
					}
				}
			}
			updateLastConnected();
			return map;
		} catch(SQLException | IOException ex){
			throw new DataSourceException(ex.getMessage(), ex);
		} finally {
			if(DO_DISCONNECTS){
				disconnect();
			}
		}
	}

	@Override
	@SuppressWarnings("SleepWhileInLoop")
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		try{
		    dm.activateThread(Thread.currentThread());
			connect();
			long startTime = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - TIMEOUT > startTime) {
					throw new DataSourceException("Data source at " + uri + " could not connect for "
							+ (TIMEOUT / 1000) + " seconds, so we're giving up on retrying.");
				}
				try {
					try (PreparedStatement statement = getConnection().prepareStatement("DELETE FROM `" + getEscapedTable()
							+ "` WHERE `" + getKeyColumn() + "`=?")) {
						statement.setString(1, StringUtils.Join(key, "."));
						statement.executeUpdate();
						updateLastConnected();
					}
					break;
				} catch(SQLException ex){
					if(ex.getMessage().startsWith("[SQLITE_BUSY]")
							// This one only happens with SETs
							|| ex.getMessage().equals("cannot commit transaction - SQL statements in progress")){
						try {
							Thread.sleep(getRandomSleepTime());
						} catch (InterruptedException ex1) {
							//
						}
					} else {
						throw ex;
					}
				}
			}
		} catch(IOException | SQLException e){
			throw new DataSourceException(e.getMessage(), e);
		} finally {
			if(DO_DISCONNECTS){
				disconnect();
			}
			dm.deactivateThread(Thread.currentThread());
		}
	}

	@Override
	public String docs() {
		return "SQLite {sqlite://path/to/db/file.db} This type store data in a SQLite database."
			+ " All the pros and cons of MySQL apply here. The database will contain a lone table,"
				+ " and the table should be created with the query: "
				+ getTableCreationQuery();
	}

	/**
	 * Returns the table creation query that should be used to create the table specified.
	 * This is public for documentation, but is used internally.
	 * @return
	 */
	public final String getTableCreationQuery(){
		return "CREATE TABLE IF NOT EXISTS `" + TABLE_NAME + "` (`" + getKeyColumn() + "` TEXT PRIMARY KEY,"
					+ " `" + getValueColumn() + "` TEXT)";
	}

	@Override
	public CHVersion since() {
		return CHVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		try {
			try (PreparedStatement statement = getConnection().prepareStatement("BEGIN EXCLUSIVE TRANSACTION")) {
				statement.execute();
			}
			updateLastConnected();
		} catch (SQLException ex) {
			Logger.getLogger(SQLiteDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		try {
			if (rollback) {
				try(PreparedStatement statement = getConnection().prepareStatement("ROLLBACK TRANSACTION")){
					statement.execute();
				}
			} else {
				try(PreparedStatement statement = getConnection().prepareStatement("END TRANSACTION")){
					statement.execute();
				}
			}
			updateLastConnected();
		} catch (SQLException ex) {
			Logger.getLogger(SQLiteDataSource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	protected String getTable() {
		return TABLE_NAME;
	}

	@Override
	protected String getConnectionString() {
		return "jdbc:sqlite:" + path;
	}

	private int getRandomSleepTime(){
		return ((int)(Math.random() * 10) % 10);
	}
}
