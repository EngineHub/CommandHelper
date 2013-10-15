package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.StringUtils;
import com.laytonsmith.PureUtilities.Web.WebUtility;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
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
 * @author lsmith
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
	
	private RedisDataSource(){
		
	}
	
	public RedisDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		super(uri, options);
		try{
			host = uri.getHost();
			port = uri.getPort();
			Map<String, String> queryString = WebUtility.getQueryMap(uri.getQuery());
			if(port == -1){
				shardInfo = new JedisShardInfo(host);
			} else {
				shardInfo = new JedisShardInfo(host, port);
			}
			if(queryString.containsKey("timeout")){
				timeout = Integer.parseInt(queryString.get("timeout"));
				shardInfo.setTimeout(timeout);
			}
			if(queryString.containsKey("password")){
				password = queryString.get("password");
				shardInfo.setPassword(password);
			}
			connect();
		} catch(Exception e){
			throw new DataSourceException(e.getMessage(), e);
		}
	}
	
	private void connect(){
		if(connection == null || !connection.isConnected()){
			connection = new Jedis(shardInfo);
		}
	}

	@Override
	protected boolean set0(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		String status;
		try{
			if(inTransaction()){
				status = transaction.set(ckey, value).get();
			} else {
				status = connection.set(ckey, value);
			}
		} catch(JedisConnectionException e){
			throw new DataSourceException(e);
		}
		return "OK".equals(status);
	}

	@Override
	protected void clearKey0(DaemonManager dm, String[] key) throws ReadOnlyException, DataSourceException, IOException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		try{
			if(inTransaction()){
				transaction.del(ckey);
			} else {
				connection.del(ckey);				
			}
		} catch(JedisConnectionException e){
			throw new DataSourceException(e);
		}
	}

	@Override
	protected String get0(String[] key) throws DataSourceException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		try{
			if(inTransaction()){
				return transaction.get(ckey).get();
			} else {
				return connection.get(ckey);
			}
		} catch(JedisConnectionException e){
			throw new DataSourceException(e);
		}
	}

	@Override
	public Set<String[]> keySet() throws DataSourceException {
		connect();
		Set<String> ret;
		try{
			if(inTransaction()){
				ret = transaction.keys("*").get();
			} else {
				ret = connection.keys("*");
			}
		} catch(JedisConnectionException e){
			throw new DataSourceException(e);
		}
		Set<String[]> parsed = new HashSet<String[]>();
		for(String s : ret){
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

	public String docs() {
		return "Redis {redis://host:port?timeout=90&password=pass} This type allows a connection to a "
				+ " redis server. A redis server must be set up and running, and if not \"localhost,\" it is heavily"
				+ " recommended to be async as well. Instructions for download and setup"
				+ " can be found at http://redis.io/download though"
				+ " Windows does not appear to be officially supported. The options in the url may be set to provide"
				+ " additional connection information.";
	}

	public CHVersion since() {
		return CHVersion.V3_3_1;
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
		if(rollback){
			transaction.discard();
		} else {
			transaction.exec();
		}
		dm.deactivateThread(null);
	}
	
}
