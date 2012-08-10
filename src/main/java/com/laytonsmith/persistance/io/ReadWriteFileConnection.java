package com.laytonsmith.persistance.io;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lsmith
 */
public class ReadWriteFileConnection implements ConnectionMixin{
	//Do not change the name of this. It is read reflectively during testing
	protected final File file;
	protected final ZipReader reader;
	protected final String blankDataModel;
	/**
	 * The executor service allows for reads and writes to be synchronized. Writes
	 * needn't be synchronous, just merely synchronized with reads and other writes.
	 * Reads of course need to be synchronous, at least as far as the thread
	 * that runs the getData function is concerned, but we still need to actually
	 * run the task on the executor service thread, so it will be synced with the
	 * writes. All file based persistance systems should use this executor to do
	 * the reads and writes.
	 */
	protected static ExecutorService Executor;	
	public ReadWriteFileConnection(URI uri, File workingDirectory, String blankDataModel) throws IOException{
		synchronized(ReadWriteFileConnection.class){
			if(Executor == null){
				//We needn't set this up until we are used at least once
				Executor = Executors.newSingleThreadExecutor(new ThreadFactory() {

					public Thread newThread(Runnable r) {
						Thread t = new Thread(r, "MethodScriptFileQueue");
						t.setDaemon(false);
						return t;
					}
				});
			}
		}
		file = new File(workingDirectory, (uri.getHost() == null ? "" : uri.getHost()) + uri.getPath());
		reader = new ZipReader(file);
		if(!reader.isZipped()){
			if(reader.getTopLevelFile().getParentFile() != null){
				reader.getTopLevelFile().getParentFile().mkdirs();
			}
		}
		if(!reader.exists()){
			reader.getTopLevelFile().createNewFile();			
		}
		this.blankDataModel = blankDataModel;		
	}

	public String getData() throws IOException {
		if(reader.isZipped()){
			//We have an entirely different method here: it is assumed that
			//a zip file is an archive; that is, there will be no write operations.
			//Making this assumption, it is then OK to simply read from it
			//without worrying about corruption from a write operation.
			return reader.getFileContents();
		}
		Future<String> future = Executor.submit(new Callable<String>(){

			public String call() throws Exception {				
				try {
					return FileUtility.read(file);
				} catch (FileNotFoundException e) {
					return blankDataModel;
				}				
			}
		});
		try {
			return future.get();
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	public void writeData(final String data) throws  ReadOnlyException, IOException, UnsupportedOperationException {		
		File outputFile = reader.getFile();
		if(reader.isZipped()){
			throw new ReadOnlyException("Cannot write to a zipped file.");
		}
		if (outputFile.getParentFile() != null) {
			outputFile.getParentFile().mkdirs();
		}
		if(!file.exists()){
			throw new FileNotFoundException(file.getAbsolutePath() + " does not exist!");
		}
		Executor.submit(new Callable() {

			public Object call() {
				try{
					FileUtility.write(data, file, FileUtility.OVERWRITE);		
				} catch (Exception ex) {
					Logger.getLogger(ReadWriteFileConnection.class.getName()).log(Level.SEVERE, null, ex);
				}
				return null;
			}
		});
	}
}
