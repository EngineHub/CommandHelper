package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.persistance.io.ConnectionMixinFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A persistance network is a group of data sources that can act transparently
 * as a single data source. The defining feature of a network is the filter
 * configuration, which defines where exactly all the keys are mapped to, and
 * the type of data sources in the network. To build a network, all you need it
 * the configuration and a default storage protocol. Everything else is handled
 * accordingly. The main operations of a persistance network are setting values,
 * getting values, and getting multiple values at once, based on a namespace
 * match. All other aspects of how the data is stored and retrieved are
 * abstracted, so you needn't worry about any of those details.
 *
 * @author lsmith
 */
public class PersistanceNetwork {

	private DataSourceFilter filter;
	private Map<URI, DataSource> dsCache;
	private ConnectionMixinFactory.ConnectionMixinOptions options;

	/**
	 * Given a configuration and a default URI, constructs a new persistance
	 * network. The defaultURI is used in the event that the configuration does
	 * not specify a "**" key, to ensure that all keys will be matched.
	 *
	 * @param configuration
	 * @param defaultURI
	 */
	public PersistanceNetwork(File configuration, URI defaultURI, ConnectionMixinFactory.ConnectionMixinOptions options) throws IOException, DataSourceException {
		this(FileUtil.read(ensureCreated(configuration)), defaultURI, options);
	}

	private static File ensureCreated(File f) throws IOException {
		if (!f.exists()) {
			if (f.getParentFile() != null) {
				f.getParentFile().mkdirs();
			}
			f.createNewFile();
		}
		return f;
	}

	/**
	 * Given a configuration and a default URI, constructs a new persistance
	 * network. The defaultURI is used in the event that the configuration does
	 * not specify a "**" key, to ensure that all keys will be matched.
	 *
	 * @param configuration
	 * @param defaultURI
	 */
	public PersistanceNetwork(String configuration, URI defaultURI, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		filter = new DataSourceFilter(configuration, defaultURI);
		dsCache = new TreeMap<URI, DataSource>();
		this.options = options;
		//Data sources are lazily loaded, so we don't need to do anything right now to load them.
	}
	
	/**
	 * Returns the URI describing where this key currently lives, given the filter
	 * configuration. This is meant for debug purposes only, and not for general use,
	 * as it breaks the transparency of the PersistanceNetwork.
	 * @param key
	 * @return 
	 */
	public URI getKeySource(String [] key){
		return filter.getConnection(key);
	}

	/**
	 * Returns the data source object for this URI.
	 *
	 * @param uri
	 * @return
	 * @throws DataSourceException
	 */
	private DataSource getDataSource(URI uri) throws DataSourceException {
		if (!dsCache.containsKey(uri)) {
			dsCache.put(uri, DataSourceFactory.GetDataSource(uri, options));
		}
		return dsCache.get(uri);
	}

	/**
	 * Returns the value for this key, or null if it doesn't exist.
	 *
	 * @param key
	 * @param isMainThread If true, then async connections will fail. This should be set by the calling 
	 * code to determine whether or not this thread is considered the "main thread" and if blocking calls
	 * are acceptable.
	 * @return
	 * @throws DataSourceException
	 * @throws IllegalArgumentException If the key is invalid
	 */
	public synchronized String get(String[] key/*boolean isMainThread*/) throws DataSourceException, IllegalArgumentException {
		//TODO: Use isMainThread here
		DataSource ds = getDataSource(filter.getConnection(key));
		return ds.get(key);
	}

	/**
	 * Sets the value for this key, and returns true if it was actually changed.
	 * (Which typically implies that the model was changed.)
	 *
	 * @param key
	 * @param value
	 * @return
	 * @throws DataSourceException
	 * @throws ReadOnlyException
	 * @throws IOException
	 * @throws IllegalArgumentException If the key is invalid
	 */
	public synchronized boolean set(DaemonManager dm, String[] key, String value) throws DataSourceException, ReadOnlyException, IOException, IllegalArgumentException {
		DataSource ds = getDataSource(filter.getConnection(key));
		return ds.set(dm, key, value);
	}

	/**
	 * Returns true if the key is actually set; that is if a call to get() would
	 * not return null.
	 *
	 * @param key
	 * @return
	 * @throws DataSourceException
	 * @throws IllegalArgumentException If the key is invalid
	 */
	public synchronized boolean hasKey(String[] key) throws DataSourceException, IllegalArgumentException {
		DataSource ds = getDataSource(filter.getConnection(key));
		return ds.hasKey(key);
	}

	/**
	 * Removes the key entirely.
	 *
	 * @param key
	 * @return
	 * @throws DataSourceException
	 * @throws IllegalArgumentException If the key is invalid
	 */
	public synchronized void clearKey(DaemonManager dm, String[] key) throws DataSourceException, ReadOnlyException, IOException, IllegalArgumentException {
		DataSource ds = getDataSource(filter.getConnection(key));
		ds.clearKey(dm, key);
	}

	/**
	 * This method returns a list of all keys and values that match the
	 * namespace. If a.b.c is requested, then keys (and values) a.b.c.d and
	 * a.b.c.e would be returned. If the namespace is empty, it will match
	 * all keys.
	 *
	 * @param namespace
	 * @return
	 * @throws IllegalArgumentException If the key is invalid
	 */
	public synchronized Map<String[], String> getNamespace(String[] namespace /*, boolean isMainThread*/) throws DataSourceException, IllegalArgumentException {
		//TODO: isMainThread needs to be used here somewhere, I think?
		List<URI> uris = filter.getAllConnections(namespace);
		//First we have to get the namespaces. We can get a list of all the connections
		//we need to search, then grab all the data in them, but then we need to use
		//just a plain get() to grab the data, based on each key, because we don't
		//want to accidentally grab a "hidden" value in another data source.
		List<String[]> keysToGrab = new ArrayList<String[]>();
		for (URI uri : uris) {
			keysToGrab.addAll(getDataSource(uri).getNamespace(namespace));
		}
		//Ok, now the keys to grab are all populated, so let's walk through them and build our map
		Map<String[], String> map = new HashMap<String[], String>();
		for (String[] key : keysToGrab) {
			map.put(key, get(key));
		}
		return map;
	}
}
