package com.laytonsmith.persistence;

import com.laytonsmith.PureUtilities.DaemonManager;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 *
 */
public interface PersistenceNetwork {

	/**
	 * Removes the key entirely.
	 *
	 * @param dm The system DaemonManager.
	 * @param key The key to clear.
	 * @throws DataSourceException If there is an error with the data source
	 * @throws ReadOnlyException If the data source is marked as read only
	 * @throws IOException If an IO exception occurs during the operation
	 * @throws IllegalArgumentException If the key is invalid
	 */
	void clearKey(DaemonManager dm, String[] key)
			throws DataSourceException, ReadOnlyException, IOException, IllegalArgumentException;

	/**
	 * Returns the value for this key, or null if it doesn't exist.
	 *
	 * @param key
	 * @return
	 * @throws DataSourceException If there is an error with the data source
	 * @throws IllegalArgumentException If the key is invalid
	 */
	String get(String[] key) throws DataSourceException, IllegalArgumentException;

	/**
	 * Returns the URI describing where this key currently lives, given the filter configuration. This is meant for
	 * debug purposes only, and not for general use, as it breaks the transparency of the PersistenceNetwork.
	 *
	 * @param key
	 * @return
	 */
	URI getKeySource(String[] key);

	/**
	 * This method returns a list of all keys and values that match the namespace. If a.b.c is requested, then keys (and
	 * values) a.b.c.d and a.b.c.e would be returned. If the namespace is empty, it will match all keys.
	 *
	 * @param namespace
	 * @return
	 * @throws DataSourceException If there is an error with the data source
	 * @throws IllegalArgumentException If the key is invalid
	 */
	Map<String[], String> getNamespace(String[] namespace) throws DataSourceException, IllegalArgumentException;

	/**
	 * Returns true if the key is actually set; that is if a call to get() would not return null.
	 *
	 * @param key
	 * @return
	 * @throws DataSourceException If there is an error with the data source
	 * @throws IllegalArgumentException If the key is invalid
	 */
	boolean hasKey(String[] key) throws DataSourceException, IllegalArgumentException;

	/**
	 * Sets the value for this key, and returns true if it was actually changed. (Which typically implies that the model
	 * was changed.)
	 *
	 * @param dm The system DaemonManager
	 * @param key
	 * @param value
	 * @return
	 * @throws DataSourceException If there is an error with the data source
	 * @throws ReadOnlyException If the data source is marked as read only
	 * @throws IOException If an IO exception occurs during the operation
	 * @throws IllegalArgumentException If the key is invalid
	 */
	boolean set(DaemonManager dm, String[] key, String value)
			throws DataSourceException, ReadOnlyException, IOException, IllegalArgumentException;

}
