package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.ClassLoading.ClassDiscovery;
import com.laytonsmith.annotations.datasource;
import com.laytonsmith.core.MSLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This utility class provides the means to interact with given data sources.
 *
 *
 */
public class DataSourceFactory {

	private static final Map<URI, DataSource> DATA_SOURCE_POOL = new HashMap<>();
	private static Map<String, Class> protocolHandlers;

	private static void init() {
		if(protocolHandlers == null) {
			protocolHandlers = new HashMap<>();
			Set<Class<?>> classes = ClassDiscovery.getDefaultInstance().loadClassesWithAnnotation(datasource.class);
			for(Class<?> c : classes) {
				if(DataSource.class.isAssignableFrom(c)) {
					protocolHandlers.put((c.getAnnotation(datasource.class)).value(), c);
				} else {
					throw new Error(c.getName() + " does not implement DataSource!");
				}
			}
		}
	}

	/**
	 * Given a connection uri and the connection options, creates and returns a new DataSource object, which can be used
	 * to do direct operations on the data source. Generally, you should go through the PersistenceNetwork class to
	 * perform operations on the network as a whole, however.
	 *
	 * @param uri The full connection uri
	 * @param options The connection mixin options
	 * @return A new DataSource object
	 * @throws DataSourceException If there is a problem connecting to the data source
	 */
	public static DataSource GetDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options)
			throws DataSourceException {
		init();
		URI uriKey = uri;
		DataSource source = DATA_SOURCE_POOL.get(uriKey);
		if(source != null) {
			return source;
		}
		List<DataSource.DataSourceModifier> modifiers = new ArrayList<>();
		while(DataSource.DataSourceModifier.isModifier(uri.getScheme())) {
			modifiers.add(DataSource.DataSourceModifier.getModifier(uri.getScheme()));
			try {
				uri = new URI(uri.getSchemeSpecificPart());
			} catch (URISyntaxException ex) {
				throw new DataSourceException(null, ex);
			}
		}
		Class c = protocolHandlers.get(uri.getScheme());
		if(c == null) {
			throw new DataSourceException("Invalid scheme: " + uri.getScheme());
		}
		try {
			DataSource ds = (DataSource) c.getConstructor(URI.class,
					ConnectionMixinFactory.ConnectionMixinOptions.class).newInstance(uri, options);
			for(DataSource.DataSourceModifier m : modifiers) {
				ds.addModifier(m);
			}
			try {
				if(ds instanceof AbstractDataSource) {
					((AbstractDataSource) ds).checkModifiers();
				}
			} catch (DataSourceException e) {
				//Warning, for invalid modifiers. This isn't an error, invalid modifiers will just be
				//ignored, but the user probably meant something else if they're getting this warning,
				//so we still alert them to the issue.
				MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.WARNING, e.getMessage(), Target.UNKNOWN);
			}
			//If the data source is transient, it will populate itself later, as needed.
			//Otherwise, we can go ahead and populate it now.
			if(!ds.getModifiers().contains(DataSource.DataSourceModifier.TRANSIENT)) {
				ds.populate();
			}
			DATA_SOURCE_POOL.put(uriKey, ds);
			return ds;
		} catch (InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException
				| IllegalAccessException | IllegalArgumentException | DataSourceException ex) {
			if(ex instanceof InvocationTargetException && ex.getCause() instanceof DataSourceException) {
				throw (DataSourceException) ex.getCause();
			}
			throw new DataSourceException("Could not instantiate a DataSource for " + c.getName() + ": "
					+ ex.getMessage(), ex);
		}
	}

	/**
	 * Given a connection uri and the connection options, creates and returns a new DataSource object, which can be used
	 * to do direct operations on the data source. Generally, you should go through the PersistenceNetwork class to
	 * perform operations on the network as a whole, however.
	 *
	 * @param uri The full connection uri
	 * @param options The connection mixin options
	 * @return A new DataSource object
	 * @throws DataSourceException If there is a problem connecting to the data source
	 * @throws URISyntaxException If the URI is invalid
	 */
	public static DataSource GetDataSource(String uri, ConnectionMixinFactory.ConnectionMixinOptions options)
			throws DataSourceException, URISyntaxException {
		return GetDataSource(new URI(uri), options);
	}

	/**
	 * Returns a list of supported protocols.
	 *
	 * @return
	 */
	public static Set<String> GetSupportedProtocols() {
		init();
		return new HashSet<>(protocolHandlers.keySet());
	}

	/**
	 * Internally, DataSourceFactory re-uses connections, for efficiency reasons. When the server is shutdown, a clean
	 * shutdown of all the cached connections is desired. This method will disconnect all persistently connecting
	 * connections, as well as delete them from the cache.
	 */
	public static void DisconnectAll() {
		for(DataSource ds : DATA_SOURCE_POOL.values()) {
			try {
				ds.disconnect();
			} catch (DataSourceException ex) {
				MSLog.GetLogger().Log(MSLog.Tags.PERSISTENCE, LogLevel.WARNING, ex.getMessage(), Target.UNKNOWN);
			}
		}
		DATA_SOURCE_POOL.clear();
	}
}
