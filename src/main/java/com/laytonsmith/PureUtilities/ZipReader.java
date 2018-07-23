package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.Common.ArrayUtils;
import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.Common.StreamUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Allows read operations to happen transparently on a zip file, as if it were a folder. Nested zips are also supported.
 * All operations are read only. Operations on a ZipReader with a path in an actual zip are expensive, so it's good to
 * keep in mind this when using the reader, you'll have to balance between memory usage (caching) or CPU use (re-reading
 * as needed).
 */
public class ZipReader {

	/**
	 * The top level zip file, which represents the actual file on the file system.
	 */
	private final File topZip;

	/**
	 * The chain of Files that this file represents.
	 */
	private final Deque<File> chainedPath;

	/**
	 * The actual file object.
	 */
	private final File file;

	/**
	 * Whether or not we have to dig down into the zip, or if we can use trivial file operations.
	 */
	private final boolean isZipped;

	/**
	 * A list of zip entries, which is cached, so we don't need to re-read the zip file each time we want to do
	 * enumerative stuff.
	 */
	private List<File> zipEntries = null;

	/**
	 * The ZipEntry contains the information of whether or not the listed file is a directory, but since we discard that
	 * information, we cache the list of directories here.
	 */
	private List<File> zipDirectories = new ArrayList<>();

	/**
	 * Convenience constructor, which allows for a URL to be passed in instead of a file, which may be useful when
	 * working with resources.
	 *
	 * @param url
	 */
	public ZipReader(URL url) {
		this(new File(url.getFile()));
	}

	/**
	 * Creates a new ZipReader object, which can be used to read from a zip file, as if the zip files were simple
	 * directories. All files are checked to see if they are a zip.
	 *
	 * <p>
	 * {@code new ZipReader(new File("path/to/container.zip/with/nested.zip/file.txt"));}</p>
	 *
	 *
	 * @param file The path to the internal file. This needn't exist, according to File, as the zip file won't appear as
	 * a directory to other classes. This constructor will however throw a FileNotFoundException if it determines that
	 * the file doesn't exist.
	 */
	public ZipReader(File file) {
		chainedPath = new LinkedList<>();

		//We need to remove jar style or uri style things from the file, so do that here
		if(file.getPath().startsWith("jar:")) {
			String newFile = file.getPath().substring(4);
			file = new File(newFile);
		}
		if(file.getPath().startsWith("file:")) {
			String newFile = file.getPath().substring(5);
			//Replace all \ with /, to simply processing, but also replace ! with /, since jar addresses
			//use that to denote the jar. We don't care, it's just a folder, so replace that with a slash.
			newFile = newFile.replace('\\', '/').replace('!', '/');
			while(newFile.startsWith("//")) {
				//We only want up to one slash here
				newFile = newFile.substring(1);
			}
			file = new File(newFile);
		}

		//make sure file is absolute
		file = file.getAbsoluteFile();
		this.file = file;

		//We need to walk up the parents, putting those files onto the stack which are valid Zips
		File f = file;
		chainedPath.addFirst(f); //Gotta add the file itself to the path for everything to work
		File tempTopZip = null;
		while((f = f.getParentFile()) != null) {
			chainedPath.addFirst(f);
			try {
				//If this works, we'll know we have our top zip file. Everything else will have
				//to be in memory, so we'll start with this if we have to dig deeper.
				if(tempTopZip == null) {
					ZipFile zf = new ZipFile(f);
					tempTopZip = f;
				}
			} catch (ZipException ex) {
				//This is fine, it's just not a zip file
			} catch (IOException | AccessControlException ex) {
				//This is fine too, it may mean we don't have permission to access this directory,
				//but that's ok, we don't need access yet.
			}
		}

		//If it's not a zipped file, this will make operations easier to deal with,
		//so let's save that information
		isZipped = tempTopZip != null;
		if(isZipped) {
			topZip = tempTopZip;
		} else {
			topZip = file;
		}

	}

	/**
	 * Returns the top level file for the underlying file. If this is not zipped, the file returned will be the file
	 * this object was constructed with. Otherwise, the File representing the actual file on the filesystem will be
	 * returned. This is mostly useful for the case where locks need to be implemented, or to find the "root" of the
	 * directory.
	 *
	 * @return
	 */
	public File getTopLevelFile() {
		return topZip;
	}

	/**
	 * Returns if this file exists or not. Note this is a non-trivial operation.
	 *
	 * @return
	 */
	public boolean exists() {
		if(!topZip.exists()) {
			return false; //Don't bother trying
		}
		try {
			getInputStream().close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Returns true if this file is read accessible. Note that if the file is a zip, the permissions are checked on the
	 * topmost zip file.
	 *
	 * @return
	 */
	public boolean canRead() {
		return topZip.canRead();
	}

	/**
	 * Returns true if this file has write permissions. Note that if the file is nested in a zip, then this will always
	 * return false. If the file doesn't exist, this will also return false, but that doesn't imply that you won't be
	 * able to create file here, so you may also need to check isZipped().
	 *
	 * @return
	 */
	public boolean canWrite() {
		if(isZipped) {
			return false;
		} else {
			return topZip.canWrite();
		}
	}

	/**
	 * Returns whether or not the file is inside of a zip file or not.
	 *
	 * @return
	 */
	public boolean isZipped() {
		return isZipped;
	}

	/**
	 * Returns a raw input stream for this file. If you just need the string contents, it would probably be easer to use
	 * getFileContents instead, however, this method is necessary for accessing binary files.
	 *
	 * @return An InputStream that will read the specified file
	 * @throws FileNotFoundException If the file is not found
	 * @throws IOException If you specify a file that isn't a zip file as if it were a folder
	 */
	public InputStream getInputStream() throws FileNotFoundException, IOException {
		if(!isZipped) {
			return new FileInputStream(file);
		} else {
			return getFile(chainedPath, topZip.getAbsolutePath(), new ZipInputStream(new FileInputStream(topZip)));
		}
	}

	/**
	 * If the file is a simple text file, this function is your best option. It returns the contents of the file as a
	 * string.
	 *
	 * @return
	 * @throws FileNotFoundException If the file is not found
	 * @throws IOException If you specify a file that isn't a zip file as if it were a folder
	 */
	public String getFileContents() throws FileNotFoundException, IOException {
		if(!isZipped) {
			return FileUtil.read(file);
		} else {
			return StreamUtils.GetString(getInputStream());
		}
	}

	/**
	 * Delegates the equals check to the underlying File object.
	 *
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		final ZipReader other = (ZipReader) obj;
		return other.file.equals(this.file);
	}

	/**
	 * Delegates the hashCode to the underlying File object.
	 *
	 * @return
	 */
	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public String toString() {
		return file.toString();
	}

	public File getFile() {
		return file;
	}

	/*
	 * This function recurses down into a zip file, ultimately returning the InputStream for the file,
	 * or throwing exceptions if it can't be found.
	 */
	private InputStream getFile(Deque<File> fullChain, String zipName, final ZipInputStream zis) throws FileNotFoundException, IOException {
		ZipEntry entry;
		InputStream zipReader = new BufferedInputStream(zis);
		boolean isZip = false;
		List<String> recurseAttempts = new ArrayList<>();
		while((entry = zis.getNextEntry()) != null) {
			//This is at least a zip file
			isZip = true;
			Deque<File> chain = new LinkedList<>(fullChain);
			File chainFile = null;
			while((chainFile = chain.pollFirst()) != null) {
				if(chainFile.equals(new File(zipName + File.separator + entry.getName()))) {
					//We found it. Now, chainFile is one that is in our tree
					//We have to do some further analyzation on it
					break;
				}
			}
			if(chainFile == null) {
				//It's not in the chain at all, which means we don't care about it at all.
				continue;
			}
			if(chain.isEmpty()) {
				//It was the last file in the chain, so no point in looking at it at all.
				//If it was a zip or not, it doesn't matter, because this is the file they
				//specified, precisely. Read it out, and return it.
				return zipReader;
			}

			//It's a single file, it's in the chain, and the chain isn't finished, so that
			//must mean it's a container (or it's being used as one, anyways).
			//It could be that either this is just a folder in the entry list, or it could
			//mean that this is a zip. We will make note of this as one we need to attempt to
			//recurse, but only if it doesn't pan out that this is a file.
			recurseAttempts.add(zipName + File.separator + entry.getName());

		}
		for(String recurseAttempt : recurseAttempts) {
			ZipInputStream inner = new ZipInputStream(zipReader);
			try {
				return getFile(fullChain, recurseAttempt, inner);
			} catch (IOException e) {
				//We don't care if this breaks, we'll throw out own top level exception
				//in a moment if we got here. We still need to finish going through
				//out recurse attempts.
			}
		}
		//If we get down here, it means either we recursed into not-a-zip file, or
		//the file was otherwise not found
		if(isZip) {
			//if this is the terminal node in the chain, it's due to a file not found.
			throw new FileNotFoundException(zipName + " could not be found!");
		} else {
			//if not, it's due to this not being a zip file
			throw new IOException(zipName + " is not a zip file!");
		}
	}

	private void initList() throws IOException {
		if(!isZipped) {
			return;
		}
		if(this.zipEntries == null) {
			zipEntries = new ArrayList<>();
			try (ZipInputStream zis = new ZipInputStream(new FileInputStream(topZip))){
				ZipEntry entry;
				while((entry = zis.getNextEntry()) != null) {
					File f = new File(topZip, entry.getName());
					zipEntries.add(f);
					if(entry.isDirectory()) {
						zipDirectories.add(f);
					}
				}
			}
		}
	}

	public boolean isDirectory() throws IOException {
		if(!isZipped) {
			return file.isDirectory();
		} else {
			initList();
			return zipDirectories.contains(file);
		}
	}

	public String getName() {
		return file.getName();
	}

	/**
	 * Returns a list of File objects that are subfiles or directories in this directory. This method does not
	 * recurse, to match the behavior of
	 *
	 * @return
	 * @throws IOException
	 */
	public File[] listFiles() throws IOException {
		if(!isZipped) {
			return file.listFiles();
		} else {
			initList();
			List<File> files = new ArrayList<>();
			for(File f : zipEntries) {
				//If the paths start with the same thing...
				if(f.getPath().startsWith(file.getPath())) {
					//...and it's not the file we're looking from to begin with...
					if(!file.equals(f)) {
						//...and it's not in a sub-sub folder of this file...
						if(!f.getPath().matches(Pattern.quote(file.getPath() + File.separatorChar) + "[^" + Pattern.quote(File.separator) + "]*" + Pattern.quote(File.separator) + ".*")) {
							//...add it to the list.
							files.add(f);
						}
					}
				}
			}
			return ArrayUtils.asArray(File.class, files);
		}
	}

	/**
	 * Shortcut to getting a ZipReader object for the files returned by {@link #listFiles()}
	 *
	 * @return
	 * @throws IOException
	 */
	public ZipReader[] zipListFiles() throws IOException {
		File[] ret = listFiles();
		ZipReader[] zips = new ZipReader[ret.length];
		for(int i = 0; i < ret.length; i++) {
			if(ret[i].isAbsolute()) {
				zips[i] = new ZipReader(ret[i]);
			} else {
				zips[i] = new ZipReader(new File(file, ret[i].getPath()));
			}
		}
		return zips;
	}

	/**
	 * Copies all the files from this directory to the source directory. If create is false, and the folder doesn't
	 * already exist, and IOException will be thrown. Sub directories will always be created, however.
	 * This is similar to an "unzip" operation.
	 *
	 * @param dstFolder
	 * @param create
	 * @throws java.io.IOException
	 */
	public void recursiveCopy(File dstFolder, boolean create) throws IOException {
		if(create) {
			dstFolder.mkdirs();
		}
		if(!dstFolder.isDirectory()) {
			throw new IOException("Destination folder is not a directory!");
		}
		for(ZipReader r : zipListFiles()) {
			if(r.isDirectory()) {
				File newFile = new File(dstFolder, r.getName());
				// Unlike the first mkdirs, we only want to mkdir here. If create was false, and the parent directory
				// did not already exist, we do not want this call to succeed.
				newFile.mkdir();
				r.recursiveCopy(newFile, create);
			} else {
				File newFile = new File(dstFolder, r.file.getName());
				newFile.getParentFile().mkdir();
				try (FileOutputStream fos = new FileOutputStream(newFile, false);
						InputStream fis = r.getInputStream()){
					StreamUtils.Copy(fis, fos);
				}
			}
		}
	}

}
