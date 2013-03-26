package com.laytonsmith.persistance.io;

import com.laytonsmith.PureUtilities.FileUtility;
import com.laytonsmith.PureUtilities.MemoryMapFileUtil;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import org.apache.log4j.lf5.util.StreamUtils;

/**
 *
 * @author lsmith
 */
public class ReadWriteFileConnection implements ConnectionMixin{
	//Do not change the name of this. It is read reflectively during testing
	protected final File file;
	/**
	 * The encoding that was determined to be the encoding for this file,
	 * if set, or UTF-8 by default, if the file doesn't exist.
	 */
	protected String encoding = "ASCII";
	protected final ZipReader reader;
	protected final String blankDataModel;
	protected byte[] data = new byte[0];
	protected MemoryMapFileUtil writer;
	protected MemoryMapFileUtil.DataGrabber grabber = new MemoryMapFileUtil.DataGrabber() {

		public byte[] getData() {
			return data;
		}
	};
	/**
	 * The executor service allows for reads and writes to be synchronized. Writes
	 * needn't be synchronous, just merely synchronized with reads and other writes.
	 * Reads of course need to be synchronous, at least as far as the thread
	 * that runs the getData function is concerned, but we still need to actually
	 * run the task on the executor service thread, so it will be synced with the
	 * writes. All file based persistance systems should use this executor to do
	 * the reads and writes.
	 */
	public ReadWriteFileConnection(URI uri, File workingDirectory, String blankDataModel) throws IOException{
		file = new File(workingDirectory, (uri.getHost() == null ? "" : uri.getHost()) + uri.getPath());
		if(file.exists()){
			encoding = FileUtility.getFileCharset(file);
		}
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
		writer = MemoryMapFileUtil.getInstance(file, grabber);
	}

	public String getData() throws IOException {
		if(reader.isZipped()){
			//We have an entirely different method here: it is assumed that
			//a zip file is an archive; that is, there will be no write operations.
			//Making this assumption, it is then OK to simply read from it
			//without worrying about corruption from a write operation.
			return reader.getFileContents();
		}
		this.data = StreamUtils.getBytes(FileUtility.readAsStream(file));
		return new String(this.data, encoding);
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
		this.data = data.getBytes(encoding);
		writer.mark();
	}

	public String getPath() throws IOException {
		return file.getCanonicalPath();
	}
}
