package com.laytonsmith.persistence.io;

import com.laytonsmith.persistence.ReadOnlyException;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 *
 * 
 */
public class ReadOnlyFileConnection extends ReadWriteFileConnection{
	public ReadOnlyFileConnection(URI uri, File workingDirectory, String blankDataModel) throws IOException{
		super(uri, workingDirectory, blankDataModel);
	}

	public void writeData(String data) throws ReadOnlyException, IOException, UnsupportedOperationException {
		throw new ReadOnlyException(file.getPath() + " is read only, and cannot be written to.");
	}
}
