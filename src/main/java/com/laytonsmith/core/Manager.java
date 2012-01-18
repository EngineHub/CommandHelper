/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author layton
 */
public class Manager {

    private static Scanner scanner;

    public static void start() {
        scanner = new Scanner(System.in);
        pl(color(Color.GREEN) + "Welcome to the CommandHelper " + color(Color.CYAN) + "Data Manager!");
        boolean finished = false;
        do {
            pl(color(Color.YELLOW) + "What function would you like to run? Type \"help\" for a full list of options.");
            p(">" + color(Color.MAGENTA));
            String input = scanner.nextLine();
            pl();
            if (input.equalsIgnoreCase("help")) {
                pl("Currently, your options are:\n"
                        + "\t" + color(Color.GREEN) + "refactor" + color(Color.WHITE) + " - Options for refactoring your persisted data from one backend to another\n"
                        + "\t" + color(Color.GREEN) + "upgrade" + color(Color.WHITE) + " - Runs upgrade scripts on your persisted data\n"
                        + "\t" + color(Color.GREEN) + "print" + color(Color.WHITE) + " - Prints out the information from your persisted data\n"
                        + "\t" + color(Color.GREEN) + "cleardb" + color(Color.WHITE) + " - Clears out your database of persisted data\n"
                        + "\t" + color(Color.GREEN) + "export" + color(Color.WHITE) + " - Exports your persisted data to a text based file\n"
                        + "\n\t" + color(Color.RED) + "exit" + color(Color.WHITE) + " - Quits the Data Manager\n");
            } else if (input.equalsIgnoreCase("refactor")) {
                pl("refactor - That feature isn't implemented yet :(");
            } else if (input.equalsIgnoreCase("print")) {
                print();
            } else if (input.equalsIgnoreCase("cleardb")) {
                pl("cleardb - That feature isn't implemented yet :(");
            } else if (input.equalsIgnoreCase("export")) {
                pl("export - That feature isn't implemented yet :(");
            } else if (input.equalsIgnoreCase("upgrade")) {
                upgrade();
            } else if (input.equalsIgnoreCase("exit")) {
                pl("Thanks for using the " + color(Color.CYAN) + "Data Manager!");
                finished = true;
            } else {
                pl("I'm sorry, that's not a valid command");
            }
        } while (finished == false);
    }

    public static void print() {
        String backingType = "serialization";
        Map data = null;
        if (backingType.equals("serialization")) {
            File db = new File("CommandHelper/persistance.ser");
            if (!db.exists()) {
                pl("Looks like you haven't used your persistance file yet.");
                return;
            }
            SerializedPersistance sp = new SerializedPersistance(db, null);
            try {
                sp.load();
            } catch (Exception ex) {
                pl(color(Color.red) + ex.getMessage());
            }
            data = sp.rawData();
        }
        pl();
        if (data != null) {
            for (Object key : data.keySet()) {
                pl(color(Color.RED) + key.toString() + ": " + color(Color.WHITE) + data.get(key).toString());
            }
        }
    }

    public static void upgrade() {
        pl("\nThis will automatically detect and upgrade your persisted data. Though this will"
                + " create a backup for you, you should manually back up your data before running"
                + " this utility.");
        pl("Would you like to continue? [" + color(Color.GREEN) + "Y" + color(Color.WHITE) + "/"
                + color(Color.RED) + "N" + color(Color.WHITE) + "]");
        p(">" + color(Color.MAGENTA));
        String choice = scanner.nextLine();
        pl();
        if (choice.equalsIgnoreCase("y")) {
            //First we have to read in the preferences file, and see what persistance type they are using
            //Only serialization is supported right now
            String backingType = "serialization";
            if (backingType.equals("serialization")) {
                try {
                    //Back up the persistance.ser file
                    File db = new File("CommandHelper/persistance.ser");
                    if (!db.exists()) {
                        pl("Looks like you haven't used your persistance file yet.");
                        return;
                    }
                    FileUtility.copy(db, new File("CommandHelper/persistance.ser.bak"));
                    //Now, load in all the data
                    SerializedPersistance sp = new SerializedPersistance(db, null);
                    try {
                        sp.load();
                    } catch (Exception ex) {
                        pl(color(Color.red) + ex.getMessage());
                    }
                    Map<String, Serializable> data = sp.rawData();
                    if (data.isEmpty()) {
                        pl("Looks like you haven't used your persistance file yet.");
                        return;
                    }
                    sp.clearAllData(); //Bye bye!
                    //Ok, now we need to determine the type of data we're currently working with
                    for (String key : data.keySet()) {
                        if (key.contains("plugin.com.sk89q.commandhelper.CommandHelperPlugin.commandhelper.function.storage.")) {
                            //We're in version 1, and we need to upgrade to version 2
                            String newKey = key.replaceFirst("plugin\\.com\\.sk89q\\.commandhelper\\.CommandHelperPlugin\\.commandhelper\\.function\\.storage\\.", "");
                            pl("Changing " + color(Color.RED) + key + " to " + color(Color.YELLOW) + newKey);
                            sp.setValue(new String[]{newKey}, data.get(key));
                        }
                    }
                    try {
                        sp.save();
                    } catch (Exception ex) {
                        pl(color(Color.red) + ex.getMessage());
                    }
                    pl(color(Color.GREEN) + "Assuming there are no error messages above, it should be upgraded now! (Use print to verify)");

                } catch (IOException ex) {
                    pl(color(Color.RED) + ex.getMessage());
                }
            }
        } else {
            pl(color(Color.RED) + "Upgrade Cancelled");
        }
    }

    public static void p(CharSequence c) {
        System.out.print(c);
    }

    public static void pl() {
        pl("");
    }

    public static void pl(CharSequence c) {
        System.out.println(c + color(Color.WHITE).toString());
    }

    private enum sys {

        WINDOWS,
        UNIX
    }
    private static sys system;

    static {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            system = sys.WINDOWS;
        } else {
            system = sys.UNIX;
        }
    }

    public static CharSequence color(Color c) {
        if (system.equals(sys.WINDOWS)) {
            return "";
        }

        String color = "37";
        if (c.equals(Color.RED)) {
            color = "31";
        } else if (c.equals(Color.GREEN)) {
            color = "32";
        } else if (c.equals(Color.BLUE)) {
            color = "34";
        } else if (c.equals(Color.YELLOW)) {
            color = "33";
        } else if (c.equals(Color.CYAN)) {
            color = "36";
        } else if (c.equals(Color.MAGENTA)) {
            color = "35";
        } else if (c.equals(Color.BLACK)) {
            color = "30";
        } else if (c.equals(Color.WHITE)) {
            color = "37";
        }
        return "\033[" + color + "m";
    }
}
