package com.laytonsmith.persistance;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.annotations.MustUseOverride;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Documentation;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * All data sources must implement this interface. It provides methods to gather
 * data about a data source, gather data from the data source, and (possibly)
 * write to the data source.
 *
 * @author lsmith
 */
@MustUseOverride
public interface DataSource extends Documentation {

	/**
	 * Returns a list of keys stored in this interface.
	 *
	 * @return
	 */
	public Set<String[]> keySet() throws DataSourceException;

	/**
	 * Returns a list of keys, pre-concatenated into dot notation. This may
	 * be equally inefficient for all data sources (getting keySet, then
	 * doing the concatenation one at a time), however if a data source is
	 * able to optimize for this, it is able.
	 *
	 * @return
	 */
	public Set<String> stringKeySet() throws DataSourceException;

	/**
	 * Given a namespace, returns all the keys in this data source that are
	 * in the namespace. For instance, if a.b.c is requested, then both the
	 * keys a.b.c.d and a.b.c.e would be returned.
	 *
	 * @param namespace
	 * @return
	 */
	public Set<String[]> getNamespace(String[] namespace) throws DataSourceException;

	/**
	 * Retrieves a value from the data source. This should be the same
	 * as get(key, false), which is generally the default usage.
	 * @param key
	 * @return 
	 */
	public String get(String[] key) throws DataSourceException;

	/**
	 * Sets a value in the data source. If value is null, the key is
	 * removed.
	 *
	 * @param key
	 * @param value
	 * @return True if the value was changed, false otherwise.
	 * @throws ReadOnlyException If this data source is inherently read
	 * only, it will throw a read only exception if this method is called.
	 * @throws IllegalArgumentException If the key is invalid
	 */
	public boolean set(DaemonManager dm, String[] key, String value) throws ReadOnlyException, DataSourceException, IOException, IllegalArgumentException;

	/**
	 * Instructs this data source to repopulate its internal structure based
	 * on this data provided. The method will be called if the data source
	 * needs to refresh itself, though for inherently transient data sources
	 * (or if the transient data source flag is set), this method may do
	 * nothing. If the data source is unable to populate itself, it may
	 * throw an exception informing the user that there is no way to read
	 * the data at this time.
	 */
	public void populate() throws DataSourceException;

	/**
	 * For this instance of the data source, adds a modifier flag to the
	 * data source. This does not preclude a data source from inherently
	 * having certain flags, nor will having a flag in the array returned by
	 * invalidModifiers() preclude it from being set here. Some settings may
	 * not need to be inherently acted upon, however they may be referenced
	 * for informational purposes if nothing else.
	 *
	 * @param modifier
	 */
	public void addModifier(DataSourceModifier modifier);

	/**
	 * If a data source always has a particular modifier, it should return
	 * those here. This is used to determine when to display configuration
	 * warnings, if a modifier is used in cases where it is implied. If the
	 * array would be empty, null may be returned.
	 *
	 * @param modifier
	 * @return
	 */
	public DataSourceModifier[] implicitModifiers();

	/**
	 * If a data source has no possible way of acting on a modifier, it
	 * should return those here. This is used to determine when to display
	 * configuration warnings, if a modifier is used in cases where it can't
	 * be acted on. If the array would be empty, null may be returned.
	 *
	 * @return
	 */
	public DataSourceModifier[] invalidModifiers();

	/**
	 * Returns a list of modifiers attached to this data source instance.
	 *
	 * @return
	 */
	public Set<DataSourceModifier> getModifiers();
	
	/**
	 * Returns true if this data source has the specified modifier.
	 * @param modifier The modifier to check
	 * @return True if the modifier is present
	 */
	public boolean hasModifier(DataSourceModifier modifier);

	/**
	 * Returns true if the data source contains this key or not.
	 * @param key
	 * @return 
	 */
	public boolean hasKey(String[] key) throws DataSourceException;
	
	/**
	 * Removes the key entirely from this data source.
	 * @param key
	 * @throws DataSourceException 
	 */
	public void clearKey(DaemonManager dm, String [] key) throws DataSourceException, ReadOnlyException, IOException;
	
	/**
	 * Starts a transaction for this data source. If the data source is not
	 * transient, this has no effect, but if it is, then all reads and writes
	 * will not be transient until the transaction is stopped.
	 */
	public void startTransaction(DaemonManager dm);
	
	/**
	 * When stopping the transaction, any pending writes will potentially
	 * occur then, so this can throw an exception at that point,
	 * much like set could, however, it will not throw ReadOnlyExceptions. 
	 * If an exception is thrown during the middle of the transaction, it is up 
	 * to the calling code to call stopTransaction(). If rollback is true, then
	 * any changes since the last startTransaction call will be rolled
	 * back. If any exceptions are thrown from this method, and rollback is
	 * true, then no changes will have been made to the data set.
	 * @param rollback If true, changes since the transaction started will 
	 * be rolled back.
	 * @throws IOException If any cached data couldn't be written out
	 * @throws DataSourceException If any other exception occurs
	 */
	public void stopTransaction(DaemonManager dm, boolean rollback) throws DataSourceException, IOException;

	/**
	 * These are the valid modifiers for a generic connection. Not all data
	 * sources can support all of these, and some are inherently present or
	 * unsupportable on certain connection types.
	 */
	public enum DataSourceModifier implements Documentation {

		READONLY("Makes the connection read-only. That is to say, calls to store_data() on the keys mapped to this data source will always fail.", CHVersion.V3_3_1),
		TRANSIENT("The data from this source is not cached. Note that for file based data sources, this makes it incredibly inefficient for large data sources,"
		+ " but makes it possible for multiple things to read and write to a source at the same time. If the connection is not read-only, a lock file will"
		+ " be created while the file is being written to (which will be the filename with .lock appended), which should be respected by other applications"
		+ " to prevent corruption. During read/write operations, if the lock file exists, the call to retrieve that data will block until the lock file"
		+ " goes away. File based connections that are NOT transient are loaded up at startup, and only writes require file system access from that point"
		+ " on. It is assumed that nothing else will be editing the data source, and so data is not re-read again, which means that leaving off the transient"
		+ " flag makes connections much more efficient. Database driven connections are always transient. ", CHVersion.V3_3_1),
		HTTP("Makes the connection source be retrieved via http instead of assuming a local file. Connections via http are always read-only."
		+ " If the connection is also transient, a call to get_value() cannot be used in synchronous mode, and will fail if async"
		+ " mode is not used. ", CHVersion.V3_3_1),
		HTTPS("Makes the connection source be retrieved via https instead of assuming a local file. Connections via http are always read-only."
		+ " If the connection is also transient, a call to get_value() cannot be used in synchronous mode, and will fail if async"
		+ " mode is not used. ", CHVersion.V3_3_1),
		ASYNC("Forces retrievals to this connection to require asyncronous usage. This is handy if an otherwise blocking data source has gotten"
		+ " too large to allow synchonous connections, or if you are using a medium/large data source transiently.", CHVersion.V3_3_1),
		PRETTYPRINT("For text based files, where it is applicable and possible, if there is a way to \"Pretty Print\" the data, do so. This usually comes"
		+ " at the cost of file size, but makes it easier to read in a text editor. For some data sources, this is not possible, due to the file"
		+ " layout requirements of the protocol itself.", CHVersion.V3_3_1),
		SSH("Retrieves the file via SSH. This cannot be used in combination with the HTTP or HTTPS flags. The file path must match the syntax used"
		+ " by SCP connections, for instance: ssh:yml://user@host:/path/to/file/over/ssh.yml. This will only work with public-key authentication"
		+ " however, since there is no practical way to input your password otherwise. Since this is a remote IO connection, async is implied if this"
		+ " modifier is set.", CHVersion.V3_3_1);
		private CHVersion since;
		private String documentation;

		private DataSourceModifier(String documentation, CHVersion since) {
			this.documentation = documentation;
			this.since = since;
		}

		public String getName() {
			return name().toLowerCase();
		}

		public String docs() {
			return documentation;
		}

		public CHVersion since() {
			return since;
		}
		
		@Override
		public URL getSourceJar() {
			return null;
		}

		public static boolean isModifier(String scheme) {
			for (DataSourceModifier modifier : DataSourceModifier.values()) {
				if (modifier.getName().equalsIgnoreCase(scheme)) {
					return true;
				}
			}
			return false;
		}

		public static DataSourceModifier getModifier(String scheme) {
			return DataSourceModifier.valueOf(scheme.toUpperCase());
		}
	}
}
