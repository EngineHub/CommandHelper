package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.Pair;
import com.laytonsmith.PureUtilities.Version;
import com.laytonsmith.persistence.io.ConnectionMixinFactory;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Wraps a data source, and ensures that it is threadsafe. The static accessor method ensures that all methods that
 * access a data source all operate on the one, threadsafe data source. This does not absolutely ensure that the data
 * source itself is threadsafe, as incompatible code will be able to bypass restrictions, but all code that uses this
 * class can ensure that amongst those classes, the accesses will be threadsafe.
 */
public final class ThreadsafeDataSource implements DataSource {

	private static final WeakHashMap<Pair<URI, ConnectionMixinFactory.ConnectionMixinOptions>, ThreadsafeDataSource>
			SOURCES = new WeakHashMap<>();

	/**
	 * Returns the threadsafe data source for the given uri and options. If an existing reference to a DataSource is
	 * currently cached, it is returned, otherwise a new one is constructed.
	 *
	 * @param uri The URI to be passed to the DataSourceFactory.
	 * @param options The options to be passed to the DataSourceFactory.
	 * @return
	 * @throws DataSourceException If the underlying call to
	 * {@link DataSourceFactory#GetDataSource(java.net.URI, com.laytonsmith.persistence.io.ConnectionMixinFactory.ConnectionMixinOptions)}
	 * throws a DataSourceException, it is re-thrown.
	 */
	public static synchronized ThreadsafeDataSource GetDataSource(URI uri, ConnectionMixinFactory.ConnectionMixinOptions options) throws DataSourceException {
		Pair<URI, ConnectionMixinFactory.ConnectionMixinOptions> pair = new Pair<>(uri, options);
		ThreadsafeDataSource source = SOURCES.get(pair);
		if(source != null) {
			return source;
		} else {
			ThreadsafeDataSource ds = new ThreadsafeDataSource(DataSourceFactory.GetDataSource(uri, options));
			SOURCES.put(pair, ds);
			return ds;
		}
	}

	private final DataSource source;

	private ThreadsafeDataSource(DataSource source) {
		this.source = source;
	}

	@Override
	public synchronized Set<String[]> keySet(String[] keyBase) throws DataSourceException {
		return source.keySet(keyBase);
	}

	@Override
	public synchronized Set<String> stringKeySet(String[] keyBase) throws DataSourceException {
		return source.stringKeySet(keyBase);
	}

	@Override
	public synchronized Set<String[]> getNamespace(String[] namespace) throws DataSourceException {
		return source.getNamespace(namespace);
	}

	@Override
	public synchronized String get(String[] key) throws DataSourceException {
		return source.get(key);
	}

	@Override
	public synchronized Map<String[], String> getValues(String[] leadKey) throws DataSourceException {
		return source.getValues(leadKey);
	}

	@Override
	public synchronized boolean set(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException, IllegalArgumentException {
		return source.set(dm, key, value);
	}

	@Override
	public synchronized void populate() throws DataSourceException {
		source.populate();
	}

	@Override
	public synchronized void addModifier(DataSourceModifier modifier) {
		source.addModifier(modifier);
	}

	@Override
	public synchronized DataSourceModifier[] implicitModifiers() {
		return source.implicitModifiers();
	}

	@Override
	public synchronized DataSourceModifier[] invalidModifiers() {
		return source.invalidModifiers();
	}

	@Override
	public synchronized Set<DataSourceModifier> getModifiers() {
		return source.getModifiers();
	}

	@Override
	public synchronized boolean hasModifier(DataSourceModifier modifier) {
		return source.hasModifier(modifier);
	}

	@Override
	public synchronized boolean hasKey(String[] key) throws DataSourceException {
		return source.hasKey(key);
	}

	@Override
	public synchronized void clearKey(DaemonManager dm, String[] key) throws DataSourceException, ReadOnlyException, IOException {
		source.clearKey(dm, key);
	}

	@Override
	public synchronized void startTransaction(DaemonManager dm) {
		source.startTransaction(dm);
	}

	@Override
	public synchronized void stopTransaction(DaemonManager dm, boolean rollback) throws DataSourceException, IOException {
		source.stopTransaction(dm, rollback);
	}

	@Override
	public synchronized void disconnect() throws DataSourceException {
		source.disconnect();
	}

	@Override
	public synchronized String getName() {
		return source.getName();
	}

	@Override
	public synchronized String docs() {
		return source.docs();
	}

	@Override
	public synchronized Version since() {
		return source.since();
	}

	@Override
	@SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
	public synchronized boolean equals(Object obj) {
		return source.equals(obj);
	}

	@Override
	public synchronized int hashCode() {
		return source.hashCode();
	}

	@Override
	public synchronized String toString() {
		return source.toString();
	}

}
