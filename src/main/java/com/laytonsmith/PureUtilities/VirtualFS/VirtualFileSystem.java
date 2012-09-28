package com.laytonsmith.PureUtilities.VirtualFS;

import com.laytonsmith.PureUtilities.StreamUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * <p>
 * A virtual file system allows for strict control over a corresponding
 * real file system. Reads and writes from the file system can be granularly controlled
 * by a configuration, and things like file quotas, file creation, and things can
 * be restricted. All files in the virtual file system map to a real file, but where
 * exactly on the real file system that is, is not exposed to the API user. Reads
 * and writes will not be allowed outside of the root file system, so to delete a
 * virtual file system simply requires deletion of that folder. All virtual files
 * use a Unix style file path, and the root is whatever the root of the file system
 * is. Primitive operations include reading and writing to the file system, iterating
 * through the files, deleting files, and reading meta information about files.
 * 
 * <p>
 * All accesses can be controlled on a per file or per directory basis, and limits
 * can be placed on individual file sizes, folder sizes, folder depth, or total file
 * system size.
 * 
 * <p>
 * The file system (or parts of it) can also be <em>cordoned off</em>, meaning that the
 * files that are created by outside processes don't appear as part of the virtual
 * file system. In this case, a virtual manifest will used to determine which files are actually
 * in the virtual file system. Reads and writes to files not in this manifest will
 * be denied, however creation of new files will be allowed, assuming a file doesn't
 * already exist there, and non-included files will not be shown in file listings.
 * External processes will not inherently be blocked from accessing these manifested
 * files, however, so only accesses through the virtual file system will be restricted.
 * 
 * <p>
 * The virtual file system will create a directory at the root of the file system,
 * <code>.vfsmeta</code>, which will contain all the information stored by the virtual
 * file system itself, and reads and writes to this special directory will always
 * fail.
 * 
 * <p>
 * Symlinks can be added, which map directories inside the virtual file system to
 * other directories on the real file system, and these links appear completely
 * transparent to the file system. This allows for non-continuous file systems
 * to appear continuous internally.
 * @author lsmith
 */
public class VirtualFileSystem {
	private static final String META_DIRECTORY_PATH = ".vfsmeta";
	public static final VirtualFile META_DIRECTORY = new VirtualFile("/" + META_DIRECTORY_PATH);
	
	private final VirtualFileSystemSettings settings;
	private final File root;
	private BigInteger quota = new BigInteger("-1");
	private BigInteger FSSize = new BigInteger("0");
	private Thread fsSizeThread;
	
	
	/**
	 * Creates a new VirtualFileSystem, at the root specified. If the root
	 * doesn't exist, it will automatically be created.
	 * @param root
	 * @param settings 
	 * @throws IOException If the file system cannot be initialized at this location
	 */
	public VirtualFileSystem(final File root, VirtualFileSystemSettings settings) throws IOException{
		this.settings = settings;
		this.root = root;
		install();
		//TODO: If it is cordoned off, we don't need this thread either, we need a different
		//thread, but it only needs to run once
		if(settings.hasQuota()){
			//We need to kick off a thread to determine the current FS size.
			fsSizeThread = new Thread(new Runnable() {

				public void run() {
					while(true){
						try {
							FSSize = FileUtils.sizeOfDirectoryAsBigInteger(root);
							//Sleep for a minute before running again.
							Thread.sleep(60 * 1000);
						} catch (InterruptedException ex) {
							Logger.getLogger(VirtualFileSystem.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
			}, "VirtualFileSystem-QuotaEnforcer-" + root.getAbsolutePath());
			fsSizeThread.setDaemon(true);
			fsSizeThread.setPriority(Thread.MIN_PRIORITY);
			fsSizeThread.start();
		}
	}
	
	private void install() throws IOException{
		if(!root.exists()){
			root.mkdirs();
		}
		File meta = new File(root, META_DIRECTORY_PATH);
		meta.mkdir();
		
		File settingsFile = new File(meta, "settings.config");
		File manifest = new File(meta, "manifest.txt");
		File symlinks = new File(meta, "symlinks.txt");
		
		if(!settingsFile.exists()){
			settingsFile.createNewFile();
		}
		
		if(!manifest.exists()){
			manifest.createNewFile();
		}
		
		if(!symlinks.exists()){
			symlinks.createNewFile();
		}
		
	}
	
	/**
	 * Reads bytes from a file.
	 * @param file
	 * @return 
	 */
	public byte[] read(VirtualFile file){
		try {
			return StreamUtils.GetBytes(readAsStream(file));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Writes an InputStream to a file.
	 * @param file
	 * @param data 
	 */
	public void write(VirtualFile file, InputStream data){
		try {
			write(file, StreamUtils.GetBytes(data));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Reads a file as a stream
	 * @param file
	 * @return 
	 */
	public InputStream readAsStream(VirtualFile file){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Writes the bytes out to a file.
	 * @param file
	 * @param bytes 
	 */
	public void write(VirtualFile file, byte[] bytes){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Lists the files and folders in this directory. Note that the
	 * . and .. directory normally present in file listings will not be
	 * present. If this is cordoned off, it will be the virtual file
	 * listing.
	 * @param directory
	 * @return 
	 */
	public VirtualFile [] list(VirtualFile directory){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Deletes a file or folder. Note that if this is cordoned off, and
	 * this is a directory, the directory may appear to be empty according
	 * to {@see #list}, but it won't be deleted if other files are actually
	 * living in it, but regardless, the entry will be removed from the manifest,
	 * and further calls to list will show it having been deleted.
	 * @param file
	 * @return 
	 */
	public boolean delete(VirtualFile file){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Works the same as {@see #delete}, but the file will be
	 * deleted upon exit of the JVM.
	 * @param file
	 * @return 
	 */
	public boolean deleteOnExit(VirtualFile file){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Returns true if the file represented by this VirtualFile
	 * actually exists on the file system.
	 * @param file
	 * @return 
	 */
	public boolean exists(VirtualFile file){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Returns true if this file can be read. If the file doesn't
	 * exist, returns false.
	 * @param file
	 * @return 
	 */
	public boolean canRead(VirtualFile file){
		if(!exists(file)){
			return false;
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Returns true if this file can be written to. 
	 * @param file
	 * @return 
	 */
	public boolean canWrite(VirtualFile file){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Returns true if the abstract path represented by this file
	 * is absolute, that is, if it starts with a forward slash.
	 * @param file
	 * @return 
	 */
	public boolean isAbsolute(VirtualFile file){
		return file.isAbsolute();
	}
	
	/**
	 * Returns true if this path represented by the VirtualFile path is a directory.
	 * If no file or folder exists, false is returned.
	 * @param fileOrFolder
	 * @return 
	 */
	public boolean isDirectory(VirtualFile fileOrFolder){
		if(!exists(fileOrFolder)){
			return false;
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Returns true if this path represented by the VirtualFile path is
	 * a file. If no file or folder exists, false is returned.
	 * @param fileOrFolder
	 * @return 
	 */
	public boolean isFile(VirtualFile fileOrFolder){
		if(!exists(fileOrFolder)){
			return false;
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates the directory specified by the VirtualFile path, and any
	 * parent directories as needed.
	 * @param directory 
	 */
	public void mkdirs(VirtualFile directory){
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	/**
	 * Creates a new, empty file at this location, if no file already
	 * exists at this location.
	 * @param file 
	 */
	public void createEmptyFile(VirtualFile file){
		if(exists(file)){
			return;
		}
		throw new UnsupportedOperationException("Not supported yet.");
	}
					
}
