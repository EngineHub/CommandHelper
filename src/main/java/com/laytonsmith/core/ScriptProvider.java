package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ZipReader;
import java.io.File;
import java.io.IOException;

/**
 * A ScriptProvider is a mechanism that converts a script's File object into a String which can be parsed.
 * By default, this would simply read a file from the file system, however, there are many use cases where this isn't
 * really the correct behavior. Unit testing, LangServ, etc. The correct DocumentProvider is stored in the GlobalEnv,
 * and should be used in all cases instead of reading directly from the file system.
 */
public interface ScriptProvider {
	/**
	 * Returns the script with the given location.
	 * @param file
	 * @return The String contents of the file
	 * @throws java.io.IOException If the file can't be read in.
	 */
	String getScript(File file) throws IOException;

	/**
	 * A ScriptProvider which reads the file from the actual file system, using the ZipReader functionality, so reading
	 * in zips also works.
	 */
	public static class FileSystemScriptProvider implements ScriptProvider {

		@Override
		public String getScript(File file) throws IOException {
			return new ZipReader(file).getFileContents();
		}

	}
}
