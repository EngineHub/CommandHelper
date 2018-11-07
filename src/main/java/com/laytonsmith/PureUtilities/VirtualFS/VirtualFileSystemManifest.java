package com.laytonsmith.PureUtilities.VirtualFS;

import java.io.IOException;

/**
 *
 */
public interface VirtualFileSystemManifest {
	/**
	 * Returns whether or not this file is listed in the manifest
	 * @param file The file to check
	 * @return True if the files is in the manifest, false otherwise.
	 */
	public boolean fileInManifest(VirtualFile file);

	/**
	 * Removes the given file from the manifest
	 * @param file
	 * @throws IOException
	 */
	public void removeFromManifest(VirtualFile file) throws IOException;

	/**
	 * Adds the given file to the manifest
	 * @param file
	 * @throws IOException
	 */
	public void addToManifest(VirtualFile file) throws IOException;

	/**
	 * Refreshes the manifest from the source (if applicable)
	 * @throws IOException
	 */
	public void refresh() throws IOException;
}
