package com.laytonsmith.PureUtilities;

import java.io.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Allows read operations to happen transparently on a zip file, as if it were a
 * folder. Nested zips are also supported. All operations are read only.
 * Operations on a ZipReader with a path in an actual zip are expensive, so it's
 * good to keep in mind this when using the reader, you'll have to balance
 * between memory usage (caching) or CPU use (re-reading as needed).
 *
 * @author Layton Smith
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
     * Whether or not we have to dig down into the zip, or if
     * we can use trivial file operations.
     */
    private final boolean isZipped;

    /**
     * Creates a new ZipReader object, which can be used to read from a zip
     * file, as if the zip files were simple directories. All files are checked
     * to see if they are a zip.
     * 
     * <p>{@code new ZipReader(new File("path/to/container.zip/with/nested.zip/file.txt"));}</p>
     * 
     *
     * @param file The path to the internal file. This needn't exist, according
     * to File, as the zip file won't appear as a directory to other classes.
     * This constructor will however throw a FileNotFoundException if it
     * determines that the file doesn't exist.
     */
    public ZipReader(File file){
        chainedPath = new LinkedList<File>();

        this.file = file;

        //make sure file is absolute
        file = file.getAbsoluteFile();

        //We need to walk up the parents, putting those files onto the stack which are valid Zips
        File f = file;
        chainedPath.addFirst(f); //Gotta add the file itself to the path for everything to work
        File tempTopZip = null;
        while ((f = f.getParentFile()) != null) {
            chainedPath.addFirst(f);
            try {
                //If this works, we'll know we have our top zip file. Everything else will have
                //to be in memory, so we'll start with this if we have to dig deeper.
                if (tempTopZip == null) {
                    ZipFile zf = new ZipFile(f);
                    tempTopZip = f;
                }
            } catch (ZipException ex) {
                //This is fine, it's just not a zip file
            } catch (IOException ex) {
                //This is fine too, it may mean we don't have permission to access this directory,
                //but that's ok, we don't need access yet.
            }
        }

        //If it's not a zipped file, this will make operations easier to deal with,
        //so let's save that information
        isZipped = tempTopZip != null;
        if(isZipped){
            topZip = tempTopZip;
        } else {
            topZip = file;
        }

    }
    
    /**
     * Returns if this file exists or not. Note this is a non-trivial operation.
     * 
     * @return 
     */
    public boolean exists(){
        if(!topZip.exists()){
            return false; //Don't bother trying
        }
        try{
            getInputStream().close();
            return true;
        } catch(IOException e){
            return false;
        }
    }
    
    /**
     * Returns true if this file is read accessible. Note that if the file is a zip,
     * the permissions are checked on the topmost zip file.
     * @return 
     */
    public boolean canRead(){
        return topZip.canRead();
    }
    
    /**
     * Returns true if this file has write permissions. Note that if the file is nested
     * in a zip, then this will always return false
     * @return 
     */
    public boolean canWrite(){
        if(isZipped){
            return false;
        } else {
            return topZip.canWrite();
        }
    }

    /*
     * This function recurses down into a zip file, ultimately returning the InputStream for the file,
     * or throwing exceptions if it can't be found.
     */
    private InputStream getFile(Deque<File> fullChain, String zipName, final ZipInputStream zis) throws FileNotFoundException, IOException {
        ZipEntry entry;
        InputStream zipReader = new InputStream() {

            @Override
            public int read() throws IOException {
                if (zis.available() > 0) {
                    return zis.read();
                } else {
                    return -1;
                }
            }

            @Override
            public void close() throws IOException {
                zis.close();
            }
        };
        boolean isZip = false;
        while ((entry = zis.getNextEntry()) != null) {
            //This is at least a zip file
            isZip = true;
            Deque<File> chain = new LinkedList<File>(fullChain);
            File chainFile = null;
            while ((chainFile = chain.pollFirst()) != null) {
                if (chainFile.equals(new File(zipName + File.separator + entry.getName()))) {
                    //We found it. Now, chainFile is one that is in our tree
                    //We have to do some further analyzation on it
                    break;
                }
            }
            if (chainFile == null) {
                //It's not in the chain at all, which means we don't care about it at all.
                continue;
            }
            if (chain.isEmpty()) {
                //It was the last file in the chain, so no point in looking at it at all.
                //If it was a zip or not, it doesn't matter, because this is the file they
                //specified, precisely. Read it out, and return it.
                return zipReader;
            }

            //It's a single file, it's in the chain, and the chain isn't finished, so that
            //must mean it's a container (or it's being used as one, anyways). Let's attempt to recurse.

            ZipInputStream inner = new ZipInputStream(zipReader);
            return getFile(fullChain, zipName + File.separator + entry.getName(), inner);

        }
        //If we get down here, it means either we recursed into not-a-zip file, or 
        //the file was otherwise not found
        if (isZip) {
            //if this is the terminal node in the chain, it's due to a file not found.
            throw new FileNotFoundException(zipName + " could not be found!");
        } else {
            //if not, it's due to this not being a zip file
            throw new IOException(zipName + " is not a zip file!");
        }
    }

    /**
     * Returns a raw input stream for this file. If you just need the string contents,
     * it would probably be easer to use getFileContents instead, however, this method
     * is necessary for accessing binary files.
     * @return An InputStream that will read the specified file
     * @throws FileNotFoundException If the file is not found
     * @throws IOException If you specify a file that isn't a zip file as if it were a folder
     */
    public InputStream getInputStream() throws FileNotFoundException, IOException {
        if (!isZipped) {           
            return new FileInputStream(file);
        } else {            
            return getFile(chainedPath, topZip.getAbsolutePath(), new ZipInputStream(new FileInputStream(topZip)));
        }
    }

    /**
     * If the file is a simple text file, this function is your best option. It returns
     * the contents of the file as a string.
     * @return
     * @throws FileNotFoundException If the file is not found
     * @throws IOException If you specify a file that isn't a zip file as if it were a folder
     */
    public String getFileContents() throws FileNotFoundException, IOException {
        if (!isZipped) {
            return FileUtility.read(file);
        } else {            
            return getStringFromInputStream(getInputStream());
        }
    }
    
    /*
     * Converts an input stream into a string
     */
    private String getStringFromInputStream(InputStream is) throws IOException {
        BufferedReader din = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = din.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
            }
        }
        return sb.toString();
    }

    /**
     * Delegates the equals check to the underlying File object.
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZipReader other = (ZipReader) obj;        
        return other.file.equals(this.file);
    }

    /**
     * Delegates the hashCode to the underlying File object.
     * @return 
     */
    @Override
    public int hashCode() {
        return file.hashCode();
    }
    
    public File getFile(){
        return file;
    }
    
    
}
