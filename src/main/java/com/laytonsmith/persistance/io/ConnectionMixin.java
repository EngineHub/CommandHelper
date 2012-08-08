package com.laytonsmith.persistance.io;

import com.laytonsmith.persistance.DataSourceException;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.IOException;

/**
 *
 * @author lsmith
 */
public interface ConnectionMixin {
	/**
	 * Gets the full data from this connection.
	 * @return
	 * @throws DataSourceException
	 * @throws IOException 
	 */
	public String getData() throws IOException;
	
	/**
	 * Writes out all the data to this connection.
	 * @throws DataSourceException
	 * @throws IOException
	 * @throws UnsupportedOperationException 
	 */
	public void writeData(String data) throws ReadOnlyException, IOException, UnsupportedOperationException;
}
