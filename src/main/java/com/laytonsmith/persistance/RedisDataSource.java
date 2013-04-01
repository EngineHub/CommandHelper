package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.StringUtils;
import com.laytonsmith.PureUtilities.WebUtility;
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

/**
 *
 * @author lsmith
 */
@datasource("redis")
public class RedisDataSource extends AbstractDataSource {

	private Jedis connection;
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
	protected boolean set0(String[] key, String value) throws ReadOnlyException, DataSourceException, IOException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		String status = connection.set(ckey, value);
		return "OK".equals(status);
	}

	@Override
	protected void clearKey0(String[] key) throws ReadOnlyException, DataSourceException, IOException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		connection.del(ckey);
	}

	@Override
	protected String get0(String[] key, boolean bypassTransient) throws DataSourceException {
		connect();
		String ckey = StringUtils.Join(key, ".");
		return connection.get(ckey);
	}

	public Set<String[]> keySet() throws DataSourceException {
		connect();
		Set<String> ret = connection.keys("*");
		Set<String[]> parsed = new HashSet<String[]>();
		for(String s : ret){
			parsed.add(s.split("\\."));
		}
		return parsed;
	}

	public void populate() throws DataSourceException {
		//Unneeded
	}

	public DataSourceModifier[] implicitModifiers() {
		return new DataSourceModifier[]{
			DataSourceModifier.TRANSIENT
		};
	}

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
	
}
