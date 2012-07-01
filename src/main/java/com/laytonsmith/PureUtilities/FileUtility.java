/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities;

import java.io.*;
import java.util.Scanner;

/**
 *
 * @author layton
 */
public class FileUtility {

    public static final int APPEND = 1;
    public static final int OVERWRITE = 0;

    /**
     * Reads in a file and return the results line by line to the LineCallback object
     * @param f The file to read in
     * @param r The LineCallback callback object
     */
    public static void CallbackReader(File f, LineCallback lc) throws FileNotFoundException {
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(new FileInputStream(f));
        try {
            while (scanner.hasNextLine()) {
                lc.run(scanner.nextLine() + NL);
            }
        } finally {
            scanner.close();
        }
    }

    public static void copy(File fromFile, File toFile)
            throws IOException {

        if (!fromFile.exists()) {
            throw new IOException("FileCopy: " + "no such source file: "
                    + fromFile.getName());
        }
        if (!fromFile.isFile()) {
            throw new IOException("FileCopy: " + "can't copy directory: "
                    + fromFile.getName());
        }
        if (!fromFile.canRead()) {
            throw new IOException("FileCopy: " + "source file is unreadable: "
                    + fromFile.getName());
        }

        if (toFile.isDirectory()) {
            toFile = new File(toFile, fromFile.getName());
        }

        if (toFile.exists()) {
            if (!toFile.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination file is unwriteable: " + toFile.getName());
            }
            System.out.print("Overwrite existing file " + toFile.getName()
                    + "? (Y/N): ");
            System.out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    System.in));
            String response = in.readLine();
            if (!response.equals("Y") && !response.equals("y")) {
                throw new IOException("FileCopy: "
                        + "existing file was not overwritten.");
            }
        } else {
            String parent = toFile.getParent();
            if (parent == null) {
                parent = System.getProperty("user.dir");
            }
            File dir = new File(parent);
            if (!dir.exists()) {
                throw new IOException("FileCopy: "
                        + "destination directory doesn't exist: " + parent);
            }
            if (dir.isFile()) {
                throw new IOException("FileCopy: "
                        + "destination is not a directory: " + parent);
            }
            if (!dir.canWrite()) {
                throw new IOException("FileCopy: "
                        + "destination directory is unwriteable: " + parent);
            }
        }

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    ;
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    /**
     * Returns the contents of this file as a string
     * @param f The file to read
     * @return a string with the contents of the file
     * @throws FileNotFoundException 
     */
    public static String read(File f) throws FileNotFoundException {
        StringBuilder t = new StringBuilder();
        String NL = System.getProperty("line.separator");
        Scanner scanner = new Scanner(new FileInputStream(f));
        try {
            while (scanner.hasNextLine()) {
                t.append(scanner.nextLine()).append(NL);
            }
        } finally {
            scanner.close();
        }
        return t.toString();
    }

    /**
     * This function writes out a String to a file, overwriting it if it
     * already exists
     * @param s The string to write to the file
     * @param f The File to write to
     * @throws IOException If the File f cannot be written to
     */
    public static void write(String s, File f) throws IOException {
        write(s, f, OVERWRITE);
    }

    /**
     * Writes out a String to the given file, either appending or overwriting,
     * depending on the selected mode
     * @param s The string to write to the file
     * @param f The File to write to
     * @param mode Either OVERWRITE or APPEND
     * @throws IOException If the File f cannot be written to
     */
    public static void write(String s, File f, int mode) throws IOException {
        boolean append;
        if (mode == OVERWRITE) {
            append = false;
        } else {
            append = true;
        }
        FileWriter fw = new FileWriter(f, append);
        fw.write(s);
        fw.close();
    }
}
