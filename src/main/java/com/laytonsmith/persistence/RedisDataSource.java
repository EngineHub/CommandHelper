package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.MSVersion;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 *
 *
 */
@datasource("redis")
public class RedisDataSource extends AbstractDataSource {

	private Jedis connection;
	private Transaction transaction;
	private JedisShardInfo shardInfo;
	private String host;
	private int port;
	private int timeout;
	private String password;
	private long lastConnected = 0;

	private RedisDataSource() {

	}

	public RedisDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
		try {
			host = uri.getHost();
			port = uri.getPort();
			Map<String, String> queryString = WebUtility.getQueryMap(uri.getQuery());
			if(port == -1) {
				shardInfo = new JedisShardInfo(host);
			} else {
				shardInfo = new JedisShardInfo(host, port);
			}
			if(queryString.containsKey("timeout")) {
				timeout = Integer.parseInt(queryString.get("timeout"));
				shardInfo.setSoTimeout(timeout);
			}
			if(queryString.containsKey("password")) {
				password = queryString.get("password");
				shardInfo.setPassword(password);
			}
			connect();
		} catch (Exception e) {
			throw new DataSourceException(e.getMessage(), e);
		}
	}

	private void connect() {
		boolean needToConnect = false;
		if(connection == null) {
			needToConnect = true;
		} else if(!connection.isConnected()) {
			needToConnect = true;
		} else if(lastConnected < System.currentTimeMillis() - 10000) {
			// If we connected more than 10 seconds ago, we should re-test
			// the connection explicitely, because isConnected may return true,
			// even if the connection will fail. The only real way to test
			// if the connection is actually open is to run a test query, but
			// doing that too often will cause unneccessary delay, so we
			// wait an arbitrary amount, in this case, 10 seconds.
			try {
				// We don't actually care if this value exists or not, just
				// that it doesn't break.
				connection.exists("connection.test");
				// Nope, don't need to connect.
			} catch (JedisConnectionException ex) {
				// Need to connect, since this broke.
				needToConnect = true;
			}
		}
		if(needToConnect) {
			connection = new Jedis(shardInfo);
		}
	}

	@Override
	public void disconnect() throws DataSourceException {
		if(connection != null) {
			connection.disconnect();
		}
	}

	@Override
	protected boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		String status;
		try {
			if(inTransaction()) {
				status = transaction.set(ckey, value).get();
			} else {
				status = connection.set(ckey, value);
			}
			lastConnected = System.currentTimeMillis();
		} catch (JedisConnectionException e) {
			throw new DataSourceException(e);
		}
		return "OK".equals(status);
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		try {
			if(inTransaction()) {
				transaction.del(ckey);
			} else {
				connection.del(ckey);
			}
			lastConnected = System.currentTimeMillis();
		} catch (JedisConnectionException e) {
			throw new DataSourceException(e);
		}
	}

	@Override
	protected String get0(String[] key) throws DataSourceException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		try {
			String ret;
			if(inTransaction()) {
				ret = transaction.get(ckey).get();
			} else {
				ret = connection.get(ckey);
			}
			lastConnected = System.currentTimeMillis();
			return ret;
		} catch (JedisConnectionException e) {
			throw new DataSourceException(e);
		}
	}

	@Override
	public Set<String[]> keySet(String[] keyBase) throws DataSourceException {
		connect();
		Set<String> ret;
		String kb = StringUtils.Join(keyBase, ".") + "*";
		try {
			if(inTransaction()) {
				ret = transaction.keys(kb).get();
			} else {
				ret = connection.keys(kb);
			}
			lastConnected = System.currentTimeMillis();
		} catch (JedisConnectionException e) {
			throw new DataSourceException(e);
		}
		Set<String[]> parsed = new HashSet<String[]>();
		for(String s : ret) {
			parsed.add(s.split("\\."));
		}
		return parsed;
	}

	@Override
	public void populate() throws DataSourceException {
		//Unneeded
	}

	@Override
	public DataSourceModifier[] implicitModifiers() {
		return new DataSourceModifier[]{
			DataSourceModifier.TRANSIENT
		};
	}

	@Override
	public DataSourceModifier[] invalidModifiers() {
		return new DataSourceModifier[]{
			DataSourceModifier.HTTP,
			DataSourceModifier.HTTPS,
			DataSourceModifier.PRETTYPRINT,
			DataSourceModifier.SSH
		};
	}

	@Override
	public String docs() {
		return "Redis {redis://host:port?timeout=90&password=pass} This type allows a connection to a "
				+ " redis server. A redis server must be set up and running, and if not \"localhost,\" it is heavily"
				+ " recommended to be async as well. Instructions for download and setup"
				+ " can be found at http://redis.io/download though"
				+ " Windows does not appear to be officially supported. The options in the url may be set to provide"
				+ " additional connection information.";
	}

	@Override
	public MSVersion since() {
		return MSVersion.V3_3_1;
	}

	@Override
	protected void startTransaction0(DaemonManager dm) {
		dm.activateThread(null);
		connection.multi();
		dm.deactivateThread(null);
	}

	@Override
	protected void stopTransaction0(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		dm.activateThread(null);
		if(rollback) {
			transaction.discard();
		} else {
			transaction.exec();
		}
		dm.deactivateThread(null);
	}

}
