package com.laytonsmith.persistance.io;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistance.DataSourceException;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lsmith
 */
public class ReadWriteFileConnection implements ConnectionMixin{
	//Do not change the name of this. It is read reflectively during testing
	protected File file = null;
	protected ZipReader reader = null;
	protected String blankDataModel = null;
	public ReadWriteFileConnection(URI uri, File workingDirectory, String blankDataModel){
		file = new File(workingDirectory, (uri.getHost() == null ? "" : uri.getHost()) + uri.getPath());
		reader = new ZipReader(file);
		this.blankDataModel = blankDataModel;
	}

	public String getData() throws IOException {
		//TODO: Use a ReadWriteLock
		try {
			return reader.getFileContents();
		} catch (FileNotFoundException e) {
			if (!reader.isZipped()) {
				try {
					writeData(blankDataModel);
					return blankDataModel;
				} catch (Exception ex) {
					//We don't actually care, the file
					//wasn't found, and so if we otherwise
					//can't write this out, that means that
					//the file not found exception is sufficient
				}
			}
			throw e;
		}
	}

	public void writeData(String data) throws  ReadOnlyException, IOException, UnsupportedOperationException {
		//TODO: Use a ReadWriteLock
		File outputFile = reader.getFile();
		if (outputFile.getParentFile() != null) {
			outputFile.getParentFile().mkdirs();
		}
		FileUtility.write(data, outputFile);
	}
}
