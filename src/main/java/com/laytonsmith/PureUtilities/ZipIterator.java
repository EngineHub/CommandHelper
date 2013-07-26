package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Goes through all the files in a zip (not the directories), and provides a callback with the input stream
 * at each file in a callback.
 */
public class ZipIterator {

	private File zip;
	
	public ZipIterator(File zip) {
		this.zip = zip;
	}
	
	/**
	 * Iterate
	 * @param callback
	 * @throws FileNotFoundException 
	 */
	public void iterate(ZipIteratorCallback callback) throws IOException{
		final ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
		ZipEntry entry;
		while((entry = zis.getNextEntry()) != null){
			if(!entry.isDirectory()){
				callback.handle(entry.getName(), new InputStream() {

					@Override
					public int read() throws IOException {
						return zis.read();
					}

					@Override
					public void close() throws IOException {
						//Do nothing, we will close this later, ourselves.
					}
					
				});
			}
		}
		zis.close();
	}
	
	public static interface ZipIteratorCallback {
		void handle(String filename, InputStream in);
	}
}
