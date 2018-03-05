package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Goes through all the files in a zip (not the directories), and provides a callback with the input stream at each file
 * in a callback.
 */
public class ZipIterator {

	private final File zip;

	public ZipIterator(File zip) {
		this.zip = zip;
	}

	/**
	 * Iterates a zip file.
	 *
	 * @param callback
	 * @throws FileNotFoundException
	 */
	public void iterate(ZipIteratorCallback callback) throws IOException {
		iterate(callback, null);
	}

	/**
	 * Iterates a zip file.
	 *
	 * @param callback
	 * @throws FileNotFoundException
	 */
	public void iterate(ZipIteratorCallback callback, final ProgressIterator progressIterator) throws IOException {
		final ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
		final double size = zip.length();
		ZipEntry entry;
		while((entry = zis.getNextEntry()) != null) {
			if(!entry.isDirectory()) {
				callback.handle(entry.getName(), new InputStream() {
					private double soFar = 0;

					@Override
					public int read() throws IOException {
						if(progressIterator != null) {
							++soFar;
							if(soFar % 128 == 0) {
								progressIterator.progressChanged(soFar, size);
							}
						}
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
