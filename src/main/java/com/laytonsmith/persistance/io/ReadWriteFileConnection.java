package com.laytonsmith.persistance.io;

import com.laytonsmith.PureUtilities.StreamUtils;
import com.laytonsmith.PureUtilities.ZipReader;
import com.laytonsmith.persistance.ReadOnlyException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileLock;

/**
 *
 * @author lsmith
 */
public class ReadWriteFileConnection implements ConnectionMixin{
	//Do not change the name of this. It is read reflectively during testing
	protected final File file;
	protected final ZipReader reader;
	protected final String blankDataModel;
	public ReadWriteFileConnection(URI uri, File workingDirectory, String blankDataModel) throws IOException{
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
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileLock lock = null;
		try {
			lock = raf.getChannel().lock();			
			ByteBuffer buffer = ByteBuffer.allocate((int)raf.length());
			raf.getChannel().read(buffer);
			String s = StreamUtils.GetString(new ByteArrayInputStream(buffer.array()), "UTF-8");
			return s;
		} catch (FileNotFoundException e) {
			return blankDataModel;
		} finally {
			if(lock != null) {
				lock.release();
			}
			raf.close();
		}
	}

	public void writeData(String data) throws  ReadOnlyException, IOException, UnsupportedOperationException {		
		File outputFile = reader.getFile();
		if(reader.isZipped()){
			throw new ReadOnlyException("Cannot write to a zipped file.");
		}
		if (outputFile.getParentFile() != null) {
			outputFile.getParentFile().mkdirs();
		}
		
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		FileLock lock = null;
		try{
			lock = raf.getChannel().lock();
			//Clear out the file
			raf.getChannel().truncate(0);
			//Write out the data
			raf.getChannel().write(ByteBuffer.wrap(data.getBytes("UTF-8")));
		} finally {
			if(lock != null){
				lock.release();
			}
			raf.close();
		}
	}
}
