package com.laytonsmith.PureUtilities.VirtualFS;

import com.laytonsmith.PureUtilities.StreamUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

/**
 * <p>
 * A virtual file system allows for strict control over a corresponding
 * real file system. Reads and writes from the file system can be granularly controlled
 * by a configuration, and things like file system quotas, file creation, and things can
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
 * can be placed on folder depth, or total file system size.
 * 
 * <p>
 * The file system as a whole can also be <em>cordoned off</em>, meaning that the
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
 * to appear continuous internally. Additionally, remote file systems can be mounted
 * via ssh, and they will appear continuous.
 * @author lsmith
 */
public class VirtualFileSystem {
	private static final String META_DIRECTORY_PATH = ".vfsmeta";
	public static final VirtualFile META_DIRECTORY = new VirtualFile("/" + META_DIRECTORY_PATH);
	private static final String TMP_DIRECTORY_PATH = META_DIRECTORY_PATH + "/tmp";
	public static final VirtualFile TMP_DIRECTORY = new VirtualFile("/" + TMP_DIRECTORY_PATH);
	
	private final VirtualFileSystemSettings settings;
	protected final File root;
	private BigInteger quota = new BigInteger("-1");
	private BigInteger FSSize = new BigInteger("0");
	private Thread fsSizeThread;
	private List<FileSystemLayer> currentTmpFiles = new ArrayList<FileSystemLayer>();
	
	
	/**
	 * Creates a new VirtualFileSystem, at the root specified. If the root
	 * doesn't exist, it will automatically be created.
	 * @param root
	 * @param settings The settings object, which represents this file system's settings. If null,
	 * it is assumed this is a fresh installation, and will be handled accordingly.
	 * @throws IOException If the file system cannot be initialized at this location
	 */
	public VirtualFileSystem(final File root, VirtualFileSystemSettings settings) throws IOException{
		this.settings = settings==null?new VirtualFileSystemSettings(""):settings;
		this.root = root;
		install();
		//TODO: If it is cordoned off, we don't need this thread either, we need a different
		//thread, but it only needs to run once
		if(this.settings.hasQuota()){
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
		//TODO: Kick off the tmp file deleter thread
	}
	
	private void install() throws IOException{
		if(!root.exists()){
			root.mkdirs();
		}
		File meta = new File(root, META_DIRECTORY_PATH);
		meta.mkdir();
		
		File settingsFile = new File(meta, "settings.yml");
		File manifest = new File(meta, "manifest.txt");
		File symlinks = new File(meta, "symlinks.txt");
		File tmpDir = new File(meta, "tmp");
		
		if(!settingsFile.exists()){
			settingsFile.createNewFile();
		}
		
		if(!manifest.exists()){
			manifest.createNewFile();
		}
		
		if(!symlinks.exists()){
			symlinks.createNewFile();
		}
		
		if(!tmpDir.exists()){
			tmpDir.mkdirs();
		}
		
	}
	
	private void assertReadPermission(VirtualFile file){
		//TODO: Finish
		throw new PermissionException(file.getPath() + " cannot be read.");
	}
	
	private void assertWritePermission(VirtualFile file){
		//TODO: Finish
		throw new PermissionException(file.getPath() + " cannot be written to.");
	}
	
	private FileSystemLayer normalize(VirtualFile virtual) throws IOException{
		return null;
		//TODO: See if thi points to a symlinked file. If so, we
		//have to take that into account too.
//		File real = new File(root, virtual.getPath());
//		if(!real.getCanonicalPath().startsWith(root.getCanonicalPath())){
//			throw new PermissionException(virtual.getPath() + " extends above the root directory of this file system, and does not point to a valid file.");
//		} else {
//			return real;
//		}
	}
	
	/**
	 * Reads bytes from a file.
	 * Requires read permission.
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
	 * Requires write permission.
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
	 * Reads a file as a stream.
	 * Requires read permission.
	 * @param file
	 * @return 
	 */
	public InputStream readAsStream(VirtualFile file) throws IOException{
		assertReadPermission(file);
		FileSystemLayer real = normalize(file);
		return real.getInputStream();
	}
	
	/**
	 * Writes the bytes out to a file.
	 * Requires write permission.
	 * @param file
	 * @param bytes 
	 */
	public void write(VirtualFile file, byte[] bytes) throws IOException{
		assertWritePermission(file);
		FileSystemLayer real = normalize(file);
		real.writeByteArray(bytes);
	}
	
	/**
	 * Lists the files and folders in this directory. Note that the
	 * . and .. directory will not be
	 * present. If this is cordoned off, it will be the virtual file
	 * listing.
	 * Requires read permission.
	 * @param directory
	 * @return 
	 */
	public VirtualFile [] list(VirtualFile directory) throws IOException{
		assertReadPermission(directory);
		if(settings.isCordonedOff()){
			throw new UnsupportedOperationException("Not yet implemented.");
		} else {
			FileSystemLayer real = normalize(directory);
			return real.listFiles();
		}
	}
	
	/**
	 * Deletes a file or folder. Note that if this is cordoned off, and
	 * this is a directory, the directory may appear to be empty according
	 * to {@see #list}, but it won't be deleted if other files are actually
	 * living in it, but regardless, the entry will be removed from the manifest,
	 * and further calls to list will show it having been deleted.
	 * Requires write permission.
	 * @param file
	 * @return 
	 */
	public void delete(VirtualFile file) throws IOException{
		assertWritePermission(file);
		if(settings.isCordonedOff()){
			throw new UnsupportedOperationException("Not implemented yet.");
		} else {
			normalize(file).delete();
		}
	}
	
	/**
	 * Works the same as {@see #delete}, but the file will be
	 * deleted upon exit of the JVM.
	 * Requires write permission.
	 * @param file
	 */
	public void deleteOnExit(VirtualFile file) throws IOException{
		assertWritePermission(file);
		if(settings.isCordonedOff()){
			throw new UnsupportedOperationException("Not implemented yet.");
		} else {
			normalize(file).deleteOnExit();
		}
	}
	
	/**
	 * Returns true if the file represented by this VirtualFile
	 * actually exists on the file system.
	 * Requires read permission.
	 * @param file
	 * @return 
	 */
	public boolean exists(VirtualFile file) throws IOException{
		assertReadPermission(file);
		return normalize(file).exists();
	}
	
	/**
	 * Returns true if this file can be read. If the file doesn't
	 * exist, returns false.
	 * Requires read permission.
	 * @param file
	 * @return 
	 */
	public boolean canRead(VirtualFile file) throws IOException{
		assertReadPermission(file);
		if(!exists(file)){
			return false;
		}
		return normalize(file).canRead();
	}
	
	/**
	 * Returns true if this file can be written to. 
	 * Requires read permission.
	 * @param file
	 * @return 
	 */
	public boolean canWrite(VirtualFile file) throws IOException{
		assertReadPermission(file);
		return normalize(file).canWrite();
	}
	
	/**
	 * Returns true if the abstract path represented by this file
	 * is absolute, that is, if it starts with a forward slash.
	 * Does not require any permissions, because it simply deals with
	 * the virtual file path.
	 * @param file
	 * @return 
	 */
	public boolean isAbsolute(VirtualFile file){
		return file.isAbsolute();
	}
	
	/**
	 * Returns true if this path represented by the VirtualFile path is a directory.
	 * If no file or folder exists, false is returned.
	 * Requires read permissions.
	 * @param fileOrFolder
	 * @return 
	 */
	public boolean isDirectory(VirtualFile fileOrFolder) throws IOException{
		assertReadPermission(fileOrFolder);
		if(!exists(fileOrFolder)){
			return false;
		}
		return normalize(fileOrFolder).isDirectory();
	}
	
	/**
	 * Returns true if this path represented by the VirtualFile path is
	 * a file. If no file or folder exists, false is returned.
	 * Requires read permissions.
	 * @param fileOrFolder
	 * @return 
	 */
	public boolean isFile(VirtualFile fileOrFolder) throws IOException{
		assertReadPermission(fileOrFolder);
		if(!exists(fileOrFolder)){
			return false;
		}
		return normalize(fileOrFolder).isFile();
	}
	
	/**
	 * Creates the directory specified by the VirtualFile path, and any
	 * parent directories as needed.
	 * Requires write permissions.
	 * @param directory 
	 */
	public void mkdirs(VirtualFile directory) throws IOException{
		assertWritePermission(directory);
		normalize(directory).mkdirs();
	}
	
	/**
	 * Creates a new, empty file at this location, if no file already
	 * exists at this location.
	 * Requires write permissions.
	 * @param file 
	 */
	public void createEmptyFile(VirtualFile file) throws IOException{
		assertWritePermission(file);
		if(exists(file)){
			return;
		}
		normalize(file).createNewFile();
	}
	
	/**
	 * Creates a new temporary file, which is guaranteed to be unique, and
	 * will definitely exist for this session. The file is likely to be deleted
	 * at the start of the next session however, and so must not be relied on to
	 * continue to exist. Temporary files do count towards the quota if enabled,
	 * but will be deleted by the system automatically. You must have read and
	 * write permissions to / to create a temp file.
	 * @return
	 * @throws IOException 
	 */
	public VirtualFile createTempFile() throws IOException{
		assertWritePermission(new VirtualFile("/"));
		assertReadPermission(new VirtualFile("/"));
		String filename = "/" + TMP_DIRECTORY_PATH + "/" + UUID.randomUUID().toString() + ".tmp";
		VirtualFile path = new VirtualFile(filename);
		FileSystemLayer real = normalize(path);
		//Add this to the current session's list, so it doesn't get hosed by the file deletion thread.
		currentTmpFiles.add(real);
		real.createNewFile();
		return path;
	}
					
}
