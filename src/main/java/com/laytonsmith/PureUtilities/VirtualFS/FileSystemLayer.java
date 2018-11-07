package com.laytonsmith.PureUtilities.VirtualFS;

import com.laytonsmith.PureUtilities.Common.Annotations.ForceImplementation;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A file system layer is a layer between a VirtualFile and the real file
 * system. This allows for non-traditional file systems to be transparently
 * added to the VFS system. These layers are specified by symlinks in the VFS,
 * which use a specific URI to denote the path. Once a FSL is implemented, it is
 * trivial for a user to add a new symlink to make use of the new FSL. All
 * functions may throw an IOException, which is not something real File objects
 * normally do (for instance, delete() will simply return false) but to give the
 * user more information, this class throws exceptions instead.
 *
 */
public abstract class FileSystemLayer {

	protected final VirtualFile path;
	protected final VirtualFileSystem fileSystem;

	@ForceImplementation
	protected FileSystemLayer(VirtualFile path, VirtualFileSystem fileSystem, String symlink) {
		this.path = path;
		this.fileSystem = fileSystem;
	}

	/**
	 * Returns an input stream to the underlying resource.
	 * @return
	 * @throws IOException If the file cannot be read
	 */
	public abstract InputStream getInputStream() throws IOException;

	/**
	 * Given the byte array, writes it to the underlying resource.
	 * @param bytes
	 * @throws IOException If the file cannot be written, for instance, the underlying resource is not available, or
	 * the file is read only.
	 */
	public abstract void writeByteArray(byte[] bytes) throws IOException;

	/**
	 * Returns a list of files in this directory. If this is not a directory, this should throw an exception.
	 * @return
	 * @throws IOException If this file is not a directory, or the user does not have permission to list the
	 * files.
	 */
	public abstract VirtualFile[] listFiles() throws IOException;

	/**
	 * Deletes the file immediately.
	 * @throws IOException If the file could not in fact be deleted, either because of permissions issues, or because
	 * the file does not exist.
	 */
	public abstract void delete() throws IOException;

	/**
	 * This may work the exact same as delete in some cases, but otherwise, the
	 * file will be deleted upon exit of the virtual machine. This method will not necessarily throw an exception if the
	 * operation should succeed, but doesn't when the deletion actually is attempted. However, in cases where the
	 * attempt will definitely never succeed, this should throw an exception.
	 *
	 * @throws IOException If the file cannot under any circumstance be deleted, for instance, if the file does not
	 * exist, or the user does not have permission, or the underlying resource is not available.
	 */
	public abstract void deleteEventually() throws IOException;

	/**
	 * Returns true if this file exists, false otherwise.
	 * @return
	 * @throws IOException If the underlying resource is not available, or the user does not have permission to check
	 * existence of a file.
	 */
	public abstract boolean exists() throws IOException;

	/**
	 * Returns true if the user can read this file.
	 * @return True if the file exists, and the user can read it. False if the file does exist, but the user cannot
	 * read it.
	 * @throws IOException If the file does not exist, the underlying resource is not available, or the user does
	 * not have permission to check if this file can be read. If the user has permission to see the existence of the
	 * file, but simply is not allowed to read it, then this will not throw an exception, but return false instead.
	 */
	public abstract boolean canRead() throws IOException;

	/**
	 * Returns true if the user can write to this file.
	 * @return True if the file exists, and the user can read it. False if the file does exist, but the user cannot
	 * write to it.
	 * @throws IOException If the file does not exist, the underlying resource is not available, or the user does
	 * not have permission to check if this can be written to. If the user has permission to see the existence of the
	 * file, but simply is not allowed to write to it, then this will not throw an exception, but return false instead.
	 */
	public abstract boolean canWrite() throws IOException;

	/**
	 * Returns true if this is a directory. In some cases, on some platforms, this is not guaranteed to return the
	 * opposite of {@link #isFile()}.
	 * @return True if this is a directory, false otherwise.
	 * @throws IOException If the path does not exist, the underlying resource is not available, or the user does
	 * not have permission to check if this is a directory.
	 */
	public abstract boolean isDirectory() throws IOException;

	/**
	 * Returns true if this is a file. In some cases, on some platforms, this is not guaranteed to return the
	 * opposite of {@link #isDirectory()}.
	 * @return True if this is a file, false otherwise.
	 * @throws IOException If the path does not exist, the underlying resource is not available, or the user does
	 * not have permission to check if this is a file.
	 */
	public abstract boolean isFile() throws IOException;

	/**
	 * Creates the specified directory, and any parent directories necessary. Directories that already exist will not be
	 * touched, and it is not an error to call this on an already existing directory.
	 * @throws IOException If the directory could not be created. Note that even in the case where the call fails, it
	 * may be that some of the parent directories were created. This will also throw an exception if the path specified
	 * already exists, but it is not a directory. It will also be thrown if the underlying resource is not available.
	 */
	public abstract void mkdirs() throws IOException;

	/**
	 * Creates a new file, if the path does not point to an existing file.
	 * @throws IOException If the path already exists, or the file could otherwise not be created, or if the underlying
	 * resource is not available.
	 */
	public abstract void createNewFile() throws IOException;

	/**
	 * Used to denote a FileSystemLayer protocol
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@SuppressWarnings("checkstyle:typename") // Fixing this violation might break dependents.
	public static @interface fslayer {

		/**
		 * The protocol identifier, for instance, "file", which would map to a
		 * file://uri type uri.
		 *
		 * @return
		 */
		String value();
	}

}
