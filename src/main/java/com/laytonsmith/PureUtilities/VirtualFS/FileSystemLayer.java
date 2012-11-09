package com.laytonsmith.PureUtilities.VirtualFS;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A file system layer is a layer between a VirtualFile and the real file
 * system. This allows for non-traditional file systems to be transparently added
 * to the VFS system. These layers are specified by symlinks in the VFS, which
 * use a specific URI to denote the path. Once a FSL is implemented,
 * it is trivial for a user to add a new symlink to make use of the new FSL.
 * All functions may throw an IOException, which is not something real File objects
 * normally do (for instance, delete() will simply return false) but to give the user
 * more information, this class throws exceptions instead.
 * @author lsmith
 */
public abstract class FileSystemLayer {
	
	protected final VirtualFile path;
	protected final VirtualFileSystem fileSystem;
	protected FileSystemLayer(VirtualFile path, VirtualFileSystem fileSystem){
		this.path = path;
		this.fileSystem = fileSystem;
	}

	public abstract InputStream getInputStream() throws IOException;

	public abstract void writeByteArray(byte[] bytes) throws IOException;

	public abstract VirtualFile[] listFiles() throws IOException;

	public abstract void delete() throws IOException;

	/**
	 * This may work the exact same as delete in some cases, but otherwise,
	 * the file will be deleted upon exit of the virtual machine.
	 * @throws IOException 
	 */
	public abstract void deleteOnExit() throws IOException;

	public abstract boolean exists() throws IOException;

	public abstract boolean canRead() throws IOException;

	public abstract boolean canWrite() throws IOException;

	public abstract boolean isDirectory() throws IOException;

	public abstract boolean isFile() throws IOException;

	public abstract void mkdirs() throws IOException;

	public abstract void createNewFile() throws IOException;
	
	/**
	 * Used to denote a FileSystemLayer protocol
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface fslayer {
		/**
		 * The protocol identifier, for instance, "file", which would
		 * map to a file://uri type uri.
		 * @return 
		 */
		String value();
	}
	
}
