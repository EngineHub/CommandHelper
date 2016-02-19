package com.laytonsmith.persistence.io;

import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.persistence.ReadOnlyException;

import java.io.IOException;

/**
 * A connection mixin is used for string based file type connections to read and write the data.
 * Various subclasses will implement the actual connection to the data source.
 */
public interface ConnectionMixin {
	/**
	 * Gets the full data from this connection. This call is always blocking until the
	 * read finishes, and the data is returned. Read/write combinations are guaranteed
	 * to complete in the order they were registered, so if {@link #writeData} isn't
	 * blocking, any queued calls to writeData will first be resolved before this
	 * method returns.
	 * @return The full data returned from the connection.
	 * @throws IOException If some IOException occurs.
	 */
	public String getData() throws IOException;
	
	/**
	 * Writes out all the data to this connection. This might be a non-blocking
	 * call, in which case the operation is queued up, and 
	 * @param dm If the call is non-blocking, this is used to ensure that the thread
	 * is not marked as daemon.
	 * @param data The full data to write out
	 * @throws ReadOnlyException If the data source is read only. This is not
	 * to say that the datasource is marked as read only, but that the datasource
	 * is truly read only, for instance, a zip archive, or a remote file, etc. If
	 * the datasource is intrinsically read only, not because the configuration for
	 * this particular datasource makes it read only, then UnsupportedOperationException
	 * will be thrown instead. In general, that should be taken to mean that this
	 * is read only anyways.
	 * @throws IOException If some IOException occurs.
	 * @throws UnsupportedOperationException If the datasource is intrinsically
	 * read only, this may also throw an UnsupportedOperationException in that case.
	 */
	public void writeData(DaemonManager dm, String data) throws ReadOnlyException, IOException, UnsupportedOperationException;
	
	/**
	 * Some connections just need to get the path information, but don't want the mixin to
	 * get the data, so in that case, we just need to return our connection information.
	 * If it is completely unacceptable to return the connection info, an UnsupportedOperationException
	 * may be thrown.
	 * @return 
	 */
	public String getPath() throws UnsupportedOperationException, IOException;
}
