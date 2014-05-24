package com.laytonsmith.persistence.io;

import com.laytonsmith.PureUtilities.Common.FileUtil;
import com.laytonsmith.PureUtilities.DaemonManager;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistence.ReadOnlyException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.lf5.util.StreamUtils;

/**
 *
 */
public class ReadWriteFileConnection implements ConnectionMixin {

	//Do not change the name of this. It is read reflectively during testing
	protected final File file;
	/**
	 * The encoding that was determined to be the encoding for this file, if
	 * set, or UTF-8 by default, if the file doesn't exist.
	 */
	protected String encoding = "UTF-8";
	protected final ZipReader reader;
	protected final String blankDataModel;
	protected final ExecutorService service;

	/**
	 * The executor service allows for reads and writes to be synchronized.
	 * Writes needn't be synchronous, just merely synchronized with reads and
	 * other writes. Reads of course need to be synchronous, at least as far as
	 * the thread that runs the getData function is concerned, but we still need
	 * to actually run the task on the executor service thread, so it will be
	 * synced with the writes. All file based persistence systems should use
	 * this executor to do the reads and writes.
	 */
	public ReadWriteFileConnection(URI uri, File workingDirectory, String blankDataModel) throws IOException {
		{
			//This bit is a little tricky. Since this is a file path, not a URL, we can't use most of the parts
			//of the URI class. We need to get the scheme specific part directly, and parse it ourselves. Given
			//"sqlite://../path/to/db.db" getSchemeSpecificPart() will return "//../path/to/db.db" so we need to
			//check to see if it starts with "//" (either 2 or 3 slashes are acceptable) and remove those manually.
			//then the rest of the path is the actual file path. If it is absolute, the File constructor will handle
			//that for us.
			String path = uri.getSchemeSpecificPart();
			if (!path.startsWith("//")) {
				throw new IOException("Could not read the URI: " + uri.toString() + ". Did you forget the \"//\"?");
			}
			path = path.substring(2);
			File temp = new File(path);
			if (temp.isAbsolute()) {
				file = temp;
			} else {
				file = new File(workingDirectory, path);
			}
		}
		if (file.exists()) {
			encoding = FileUtil.getFileCharset(file);
		}
		reader = new ZipReader(file);
		if (!reader.isZipped()) {
			if (reader.getTopLevelFile().getParentFile() != null) {
				reader.getTopLevelFile().getParentFile().mkdirs();
			}
		}
		if (!reader.exists()) {
			reader.getTopLevelFile().createNewFile();
		}
		this.blankDataModel = blankDataModel;
		this.service = new ThreadPoolExecutor(1, 1,
				60L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(),
				new ThreadFactory() {
					@Override
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, "ReadWriteFileConnection-" + file);
						t.setPriority(Thread.MIN_PRIORITY);
						t.setDaemon(true);
						return t;
					}
				});

	}

	@Override
	@SuppressWarnings("ThrowableResultIgnored")
	public String getData() throws IOException {
		if (reader.isZipped()) {
			//We have an entirely different method here: it is assumed that
			//a zip file is an archive; that is, there will be no write operations.
			//Making this assumption, it is then OK to simply read from it
			//without worrying about corruption from a write operation.
			return reader.getFileContents();
		}
		final Future<byte[]> future;
		synchronized (service) {
			future = service.submit(new Callable<byte[]>() {

				@Override
				public byte[] call() throws Exception {
					return StreamUtils.getBytes(FileUtil.readAsStream(file));
				}
			});
		}
		try {
			return new String(future.get(), encoding);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (ExecutionException ex) {
			if (ex.getCause() instanceof IOException) {
				throw (IOException) ex.getCause();
			} else {
				throw new RuntimeException(ex.getCause());
			}
		}
	}

	@Override
	public void writeData(final DaemonManager dm, final String data) throws ReadOnlyException, IOException, UnsupportedOperationException {
		if (reader.isZipped()) {
			throw new ReadOnlyException("Cannot write to a zipped file.");
		}
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath() + " does not exist!");
		}
		synchronized (service) {
			dm.activateThread(null);
			service.submit(new Runnable() {

				@Override
				public void run() {
					try {
						FileUtil.write(data, file);
					} catch (IOException ex) {
						Logger.getLogger(ReadWriteFileConnection.class.getName()).log(Level.SEVERE, null, ex);
					}
					dm.deactivateThread(null);
				}
			});
		}
	}

	@Override
	public String getPath() throws UnsupportedOperationException, IOException {
		return file.getCanonicalPath();
	}

}
