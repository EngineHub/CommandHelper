package com.laytonsmith.PureUtilities;

import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import com.laytonsmith.core.Installer;
import java.io.*;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author layton
 */
public class ZipReader {

    private final File topZip;
    private final Deque<File> chainedPath;
    private final File file;
    private final boolean isZipped;

    /**
     * Creates a new ZipReader object, which can be used to read from a zip
     * file, as if the zip files were simple directories. All files are checked
     * to see if they are a zip.
     *
     * @param file The path to the internal file. This needn't exist, according
     * to File, as the zip file won't appear as a directory to other classes.
     * This constructor will however throw a FileNotFoundException if it
     * determines that the file doesn't exist.
     * @param tempDirectory
     * @param zipExtentions
     */
    public ZipReader(File file, File tempDirectory) throws FileNotFoundException, IOException {
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
                Logger.getLogger(ZipReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        topZip = tempTopZip;

        //If it's not a zipped file, this will make operations easier to deal with,
        //so let's save that information
        isZipped = topZip != null;
        if (!isZipped) {
            //Go ahead and assert that the file exists, so we can safely ignore this exception later
            if (!file.exists()) {
                throw new FileNotFoundException(file.getAbsolutePath() + " does not exist");
            }
            if (!file.canRead()) {
                throw new IOException("Cannot read target file!");
            }
            //We're done here, since it isn't zipped. The rest of the operations are trivial.
        } else {
            //We need to walk through the entries, and see if they match any of our chained paths (below us)
            //If so, we need to see if they are zips also. Everything from this point is in memory,
            //because we have to inflate the zips dynamically
            ZipInputStream zis = new ZipInputStream(new FileInputStream(topZip));
            //We're just getting it for test purposes, so we want to immediately close it
            getFile(chainedPath, topZip.getAbsolutePath(), zis).close();
        }


    }

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

    public InputStream getInputStream() {
        if (!isZipped) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                //This really shouldn't ever happen, unless the file was deleted
                //in between construction and this call, in which case it's more of
                //an error than an expected condition
                throw new IOError(e);
            }
        } else {
            try {
                return getFile(chainedPath, topZip.getAbsolutePath(), new ZipInputStream(new FileInputStream(topZip)));
            } catch (IOException ex) {
                throw new IOError(ex); //Uh, it's unspecified why this would happen, so whatever.
            }
        }
    }

    public String getFileContents() {
        if (!isZipped) {
            try {
                return FileUtility.read(file);
            } catch (FileNotFoundException ex) {
                throw new IOError(ex);
            }
        } else {
            try{
                return getStringFromInputStream(getInputStream());
            } catch(IOException e){
                throw new IOError(e);
            }
        }
    }
    
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
}
