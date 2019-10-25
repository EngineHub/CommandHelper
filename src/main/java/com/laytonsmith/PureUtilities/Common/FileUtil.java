package com.laytonsmith.PureUtilities.Common;

import com.laytonsmith.PureUtilities.GCUtil;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 *
 */
public final class FileUtil {

	private FileUtil() {
	}
	public static final int OVERWRITE = 0;
	public static final int APPEND = 1;

	private static final Map<String, Object> FILE_LOCKS = new HashMap<>();
	private static final Map<String, Integer> FILE_LOCK_COUNTER = new HashMap<>();

	/**
	 * A more complicated mechanism is required to ensure global access across the JVM is synchronized, so file system
	 * accesses do not throw OverlappingFileLockExceptions. Though process safe, file locks are not thread safe -.-
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static synchronized Object getLock(File file) throws IOException {
		String canonical = file.getAbsoluteFile().getCanonicalPath();
		if(!FILE_LOCKS.containsKey(canonical)) {
			FILE_LOCKS.put(canonical, new Object());
			FILE_LOCK_COUNTER.put(canonical, 0);
		}
		FILE_LOCK_COUNTER.put(canonical, FILE_LOCK_COUNTER.get(canonical) + 1);
		return FILE_LOCKS.get(canonical);
	}

	private static synchronized void freeLock(File file) throws IOException {
		String canonical = file.getAbsoluteFile().getCanonicalPath();
		FILE_LOCK_COUNTER.put(canonical, FILE_LOCK_COUNTER.get(canonical) - 1);
		if(FILE_LOCK_COUNTER.get(canonical) == 0) {
			FILE_LOCK_COUNTER.remove(canonical);
			FILE_LOCKS.remove(canonical);
		}
	}

	public static String read(File f) throws IOException {
		return org.apache.commons.io.FileUtils.readFileToString(f, "UTF-8");
//		try {
//			return read(f, "UTF-8");
//		} catch (UnsupportedEncodingException ex) {
//			throw new Error(ex);
//		}
	}

	public static String read(File file, String charset) throws IOException {
		return StreamUtils.GetString(readAsStream(file), charset);
	}

	/**
	 * Returns the contents of this file as a string
	 *
	 * @param file The file to read
	 * @return a string with the contents of the file
	 * @throws FileNotFoundException
	 */
	public static InputStream readAsStream(File file) throws IOException {
		try {
			byte[] bytes = org.apache.commons.io.FileUtils.readFileToByteArray(file);
			return new ByteArrayInputStream(bytes);
		} catch (IOException ex) {
			//Apache IO has an interesting feature/bug where the error message "Unexpected readed size" is thrown.
			//If this is the case, we're going to try using a normal java file connection. Other IOExceptions
			//are just going be rethrown.
			if(ex.getMessage().startsWith("Unexpected readed size.")) {
				FileInputStream fis = new FileInputStream(file);
				try {
					byte[] bytes = StreamUtils.GetBytes(fis);
					return new ByteArrayInputStream(bytes);
				} finally {
					//JVM bug with files
					fis.close();
					fis = null;
					GCUtil.BlockUntilGC();
				}
			} else {
				throw ex;
			}
		}
//		try {
//			synchronized (getLock(file)) {
//				RandomAccessFile raf = new RandomAccessFile(file, "rw");
//				FileLock lock = null;
//				try {
//					lock = raf.getChannel().lock();
//					ByteBuffer buffer = ByteBuffer.allocate((int) raf.length());
//					raf.getChannel().read(buffer);
//					return new ByteArrayInputStream(buffer.array());
//				} finally {
//					if(lock != null) {
//						lock.release();
//					}
//					raf.close();
//				}
//			}
//		} finally {
//			freeLock(file);
//		}
//		FileInputStream fis = new FileInputStream(f);
//		try {
//		return StreamUtils.GetString(fis, charset);
//		} finally {
//			fis.close();
//			fis = null;
//			System.gc();
//		}
	}

	/**
	 * Works the same as write(String, File, int, false).
	 *
	 * @param data
	 * @param file
	 * @param mode
	 * @throws IOException
	 */
	public static void write(String data, File file, int mode) throws IOException {
		write(data, file, mode, false);
	}

	public static void write(String data, File file, int mode, boolean create) throws IOException {
		write(data.getBytes("UTF-8"), file, mode, create);
	}

	/**
	 * Writes out string data as UTF-8 to the given file, either appending or overwriting, depending on the selected
	 * mode. If create
	 * is true, will attempt to create the file and parent directories if need be.
	 *
	 * @param data The string to write to the file
	 * @param file The File to write to
	 * @param mode The mode in which to write the file
	 * @param create If true, will create the parent directories
	 * @throws IOException If the File cannot be written to
	 */
	public static void write(String data, File file, FileWriteMode mode, boolean create) throws IOException {
		write(data.getBytes("UTF-8"), file, mode, create);
	}

	/**
	 * Writes out byte data to the given file, either appending or overwriting, depending on the selected mode. If create
	 * is true, will attempt to create the file and parent directories if need be.
	 *
	 * @param data The data to write to the file
	 * @param file The File to write to
	 * @param mode The mode in which to write the file
	 * @param create If true, will create the parent directories
	 * @throws IOException If the File cannot be written to
	 */
	public static void write(byte[] data, File file, FileWriteMode mode, boolean create) throws IOException {
		if(mode == FileWriteMode.SAFE_WRITE) {
			if(file.exists()) {
				throw new IOException("Cannot create file, SAFE_WRITE set, and file already exists [" + file + "]");
			}
			mode = FileWriteMode.OVERWRITE;
		}
		if(mode == FileWriteMode.OVERWRITE) {
			write(data, file, OVERWRITE, create);
		} else if(mode == FileWriteMode.APPEND) {
			write(data, file, APPEND, create);
		} else {
			throw new Error("Unaccounted for FileWriteMode");
		}
	}

	/**
	 * Writes out byte data to the given file, either appending or overwriting, depending on the selected mode. If create
	 * is true, will attempt to create the file and parent directories if need be.
	 *
	 * @param data The string to write to the file
	 * @param file The File to write to
	 * @param mode Either OVERWRITE or APPEND
	 * @param create If true, will create the parent directories
	 * @throws IOException If the File f cannot be written to
	 */
	public static void write(byte[] data, File file, int mode, boolean create) throws IOException {
		boolean append;
		append = mode != OVERWRITE;
		if(create && !file.exists()) {
			if(file.getAbsoluteFile().getParentFile() != null) {
				file.getAbsoluteFile().getParentFile().mkdirs();
			}
			file.getAbsoluteFile().createNewFile();
		}
		FileUtils.writeByteArrayToFile(file, data, append);
//		try {
//			synchronized (getLock(file)) {
//				int sleepTime = 0;
//				int sleepTimes = 0;
//				loop: while(true){
//					try {
//						Thread.sleep(sleepTime);
//						sleepTime += 10;
//						sleepTimes++;
//					} catch (InterruptedException ex) {
//						//
//					}
//					RandomAccessFile raf = new RandomAccessFile(file, "rw");
//					FileLock lock = null;
//					try {
//						lock = raf.getChannel().lock();
//						//Clear out the file
//						if(!append) {
//							raf.getChannel().truncate(0);
//						} else {
//							raf.seek(raf.length());
//						}
//						//Write out the data
//						MappedByteBuffer buf = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, data.length);
//						buf.put(data);
//						buf.force();
//						//We assume it worked at this point, so let's break;
//						break loop;
//						//raf.getChannel().write(ByteBuffer.wrap(data));
//					} catch (IOException e){
//						//If we get this dumb message, we're on windows. We'll try again here shortly,
//						//but we don't want to bother the user with this exception if we can help it.
//						//http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6354433
//						if(!"The requested operation cannot be performed on a file with a user-mapped section open"
//								.equals(e.getMessage())){
//							throw e;
//						}
//						if(sleepTimes > 10){
//							//Eh. Gotta give up some time.
//							throw e;
//						}
//					} finally {
//						if(lock != null) {
//							lock.release();
//						}
//						raf.close();
//						raf = null;
//						System.gc();
//					}
//				}
//			}
//		} finally {
//			freeLock(file);
//		}
//		FileWriter fw = new FileWriter(f, append);
//		fw.write(s);
//		fw.close();
	}

	/**
	 * This function writes out a String to a file, overwriting it if it already exists
	 *
	 * @param s The string to write to the file
	 * @param f The File to write to
	 * @throws IOException If the File f cannot be written to
	 */
	public static void write(String s, File f) throws IOException {
		write(s, f, OVERWRITE);
	}

	/**
	 * Shorthand for write(s, f, OVERWRITE, create)
	 */
	public static void write(String s, File f, boolean create) throws IOException {
		write(s, f, OVERWRITE, create);
	}

	/**
	 * Copies a file from one location to another. If overwrite is null, prompts the user on the console if the file
	 * already exists. If overwrite is false, the operation throws an exception if the file already exists. If overwrite
	 * is true, the file is overwritten without prompting if it already exists.
	 *
	 * @param fromFile
	 * @param toFile
	 * @param overwrite
	 * @throws IOException
	 */
	public static void copy(File fromFile, File toFile, Boolean overwrite)
			throws IOException {

		if(!fromFile.exists()) {
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFile.getName());
		}
		if(!fromFile.isFile()) {
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFile.getName());
		}
		if(!fromFile.canRead()) {
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFile.getName());
		}

		if(toFile.isDirectory()) {
			toFile = new File(toFile, fromFile.getName());
		}

		if(toFile.exists()) {
			if(!toFile.canWrite()) {
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFile.getName());
			}

			String response = null;
			if(overwrite == null) {
				StreamUtils.GetSystemOut().print("Overwrite existing file " + toFile.getName()
						+ "? (Y/N): ");
				StreamUtils.GetSystemOut().flush();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						System.in));
				response = in.readLine();
			}
			if((overwrite != null && overwrite == false)
					|| (response != null && !response.equals("Y") && !response.equals("y"))) {
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
			}
			//overwrite being true falls through
		} else {
			String parent = toFile.getParent();
			if(parent == null) {
				parent = System.getProperty("user.dir");
			}
			File dir = new File(parent);
			if(!dir.exists()) {
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			}
			if(dir.isFile()) {
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			}
			if(!dir.canWrite()) {
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
			}
		}

		FileUtils.copyFile(fromFile, toFile);
//		FileInputStream from = null;
//		FileOutputStream to = null;
//		try {
//			from = new FileInputStream(fromFile);
//			to = new FileOutputStream(toFile);
//			byte[] buffer = new byte[4096];
//			int bytesRead;
//
//			while((bytesRead = from.read(buffer)) != -1) {
//				to.write(buffer, 0, bytesRead); // write
//			}
//		} finally {
//			if(from != null) {
//				try {
//					from.close();
//				} catch (IOException e) {
//					;
//				}
//			}
//			if(to != null) {
//				try {
//					to.close();
//				} catch (IOException e) {
//					;
//				}
//			}
//		}
	}

	/**
	 * Moves a file from one location to another. Assuming no exception is thrown, always returns true.
	 *
	 * @param from
	 * @param to
	 */
	public static boolean move(File from, File to) throws IOException {
		FileUtils.moveFile(from, to);
		return true;
//		try {
//			synchronized(getLock(to)){
//				return from.renameTo(to);
//			}
//		} finally{
//			freeLock(to);
//		}
	}

	/**
	 * Recursively deletes a file/folder structure. True is returned if ALL files were deleted. If it returns false,
	 * some or none of the files may have been deleted.
	 *
	 * @param file
	 * @return
	 */
	public static boolean recursiveDelete(File file) {
		//Hopefully this works around JVM bugs.
		//It seems that on windows machines, until garbage
		//collection happens, the system will still
		//have file locks on the file, even if the Streams
		//were properly closed.
		GCUtil.BlockUntilGC();
		if(file.isDirectory()) {
			boolean ret = true;
			for(File f : file.listFiles()) {
				if(!recursiveDelete(f)) {
					ret = false;
				}
			}
			if(!file.delete()) {
				ret = false;
			}
			return ret;
		} else {
			return file.delete();
		}
	}

	public static interface FileHandler {
		/**
		 * Handles a file.
		 * @param f
		 * @throws java.io.IOException If the some operation on the file errored.
		 */
		void handle(File f) throws IOException;
	}
	/**
	 * Recursively iterates through all files in this directory and all subdirectories. It takes a callback, which is
	 * sent each file, in turn. The parent directory (the one passed in) is also sent to the handler. The callback
	 * may throw an IOException, which stops further processing, and is rethrown.
	 * @param file The file to start with
	 * @param handler The handler to use to process the file
	 * @throws java.io.IOException If the underlying handler throws an IOException. If the handler does not throw
	 * an IOException, then this function will never otherwise do so. (SecurityExceptions may be thrown, however.)
	 */
	public static void recursiveFind(File file, FileHandler handler) throws IOException {
		handler.handle(file);
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				recursiveFind(f, handler);
			}
		}
	}

	/**
	 * Recursively deletes a file/folder structure on exit. This will not delete the file immediately, but it is not
	 * an error to delete the file first.
	 *
	 * @param file
	 */
	public static void recursiveDeleteOnExit(File file) {
		System.gc();
		if(file.isDirectory()) {
			for(File f : file.listFiles()) {
				recursiveDelete(f);
			}
			file.deleteOnExit();
		} else {
			file.deleteOnExit();
		}
	}

	/**
	 * Returns the most likely character encoding for this file. The default is "ASCII" and is probably the most common.
	 * Note that ASCII and UTF-8 are the same format when the character set is just the 8 byte characters.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String getFileCharset(File file) throws IOException {
		int lang = nsPSMDetector.ALL;
		nsDetector det = new nsDetector(lang);
		final MutableObject result = new MutableObject("ASCII");
		det.Init((String charset) -> {
			result.setObject(charset);
		});

		BufferedInputStream imp = null;
		try {
			imp = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[1024];
			int len;
			boolean done = false;
			boolean isAscii = true;

			while((len = imp.read(buf, 0, buf.length)) != -1) {
				if(isAscii) {
					isAscii = det.isAscii(buf, len);
				}
				if(!isAscii && !done) {
					done = det.DoIt(buf, len, false);
				}
			}
			det.DataEnd();
			return (String) result.getObject();
		} finally {
			if(imp != null) {
				imp.close();
			}
		}
	}
}
