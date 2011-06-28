/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.PureUtilities.fileutility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author layton
 */
public class FileUtility {

    public static final int OVERWRITE = 0;
    public static final int APPEND = 1;

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

    /**
     * Returns the contents of this file as a string
     * @param f The file to read
     * @return a string with the contents of the file
     * @throws FileNotFoundException 
     */
    public static String read(File f) throws FileNotFoundException{
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
     * Writes out a String to the given file, either appending or overwriting,
     * depending on the selected mode
     * @param s The string to write to the file
     * @param f The File to write to
     * @param mode Either OVERWRITE or APPEND
     * @throws IOException If the File f cannot be written to
     */
    public static void write(String s, File f, int mode) throws IOException{
        boolean append;
        if(mode == OVERWRITE){
            append = false;
        } else {
            append = true;
        }
        FileWriter fw = new FileWriter(f, append);
        fw.write(s);
    }

    /**
     * This function writes out a String to a file, overwriting it if it
     * already exists
     * @param s The string to write to the file
     * @param f The File to write to
     * @throws IOException If the File f cannot be written to
     */
    public static void write(String s, File f) throws IOException{
        write(s, f, OVERWRITE);
    }
}
