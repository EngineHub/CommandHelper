package com.laytonsmith.PureUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author layton
 */
public class ZipMaker {

    public static void MakeZip(File startingDir, String filename) {
        if(startingDir.isDirectory()){
            List<File> files = new ArrayList<File>();
            GetFiles(files, startingDir, startingDir);
            MakeZip(files, new File(startingDir.getParentFile(), filename), startingDir);
        } else {
            MakeZip(Arrays.asList(startingDir), new File(startingDir.getParentFile(), filename), startingDir);
        }
    }
    
    private static void GetFiles(List<File> ongoing, File directory, File base){
        if(directory.isDirectory()){
            for(File f : directory.listFiles()){
                GetFiles(ongoing, f, base);
            }
        } else {
            File file = new File(directory.getAbsolutePath().replaceFirst(Pattern.quote(base.getAbsolutePath() + "/"), ""));
            ongoing.add(file);
        }
    }

    private static void MakeZip(List<File> files, File output, File base) {
        // These are the files to include in the ZIP file

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(output));

            // Compress the files
            for (File f : files) {
                FileInputStream in = new FileInputStream(new File(base, f.getPath()));

                // Add ZIP entry to output stream.                
                out.putNextEntry(new ZipEntry(f.getPath()));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            Logger.getLogger(ZipMaker.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
