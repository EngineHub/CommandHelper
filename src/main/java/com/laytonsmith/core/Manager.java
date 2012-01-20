/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.ConfigCompileException;
//import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.*;
import static org.fusesource.jansi.Ansi.Color.*;

/**
 *
 * @author layton
 */
public class Manager {

    private static Scanner scanner;

    public static void start() {
        cls();
        AnsiConsole.systemInstall();
        pl("\n" + Static.Logo() + "\n\n" + Static.DataManagerLogo());
        scanner = new Scanner(System.in);
        
        pl("Starting the Data Manager...");
        try {
            MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex("player()", null)), new Env(), null, null);
        } catch (ConfigCompileException ex) {}
        pl(green + "Welcome to the CommandHelper " + cyan + "Data Manager!");
        pl(blinkon + red + "Warning!" + blinkoff + yellow + " Be sure your server is not running before using this tool to make changes to your database!");
        pl("------------------------");
        boolean finished = false;
        do {
            pl(yellow + "What function would you like to run? Type \"help\" for a full list of options.");
            String input = prompt();
            pl();
            if (input.equalsIgnoreCase("help")) {
                help();
            } else if (input.equalsIgnoreCase("refactor")) {
                pl("refactor - That feature isn't implemented yet :(");
            } else if (input.equalsIgnoreCase("print")) {
                print();
            } else if (input.equalsIgnoreCase("cleardb")) {
                cleardb();
            } else if(input.equalsIgnoreCase("import")){                
                pl("import - That feature isn't implemented yet :(");
            } else if (input.equalsIgnoreCase("export")) {
                export();
            } else if(input.equalsIgnoreCase("edit")){
                edit();
            } else if (input.equalsIgnoreCase("upgrade")) {
                upgrade();
            } else if (input.equalsIgnoreCase("exit")) {
                pl("Thanks for using the " + cyan + "Data Manager!");
                finished = true;
            } else {
                pl("I'm sorry, that's not a valid command. Here's the help:");
                help();
            }
        } while (finished == false);
    }
    
    public static void export(){
        cls();
        pl(green + "Export creates a text based file that you can use to get your persisted data\n"
         + "out of the database. You have various options for the export type.");
        String type;
        while(true){
            pl(yellow + "What export type would you like? The options are: XML, plain text, INI, and YML.");
            pl("[XML/TEXT/INI/YML]");
            type = prompt().toLowerCase();
            if(type.equals("xml") || type.equals("text") || type.equals("ini") || type.equals("yml")){
                break;
            } else {
                pl(red + "That's not a valid type.");
            }
        }
        
        String filename;
        while(true){
            pl("Give me a filename to store it in:");
            filename = prompt();
            File file = new File(filename);
            if(file.exists()){
                pl(red + "That file already exists, do you want to overwrite it? " + white + "[Y/N]");
                if(prompt().equalsIgnoreCase("y")){
                    break;
                }
            } else if(file.getAbsoluteFile().getParentFile() != null && !file.getAbsoluteFile().getParentFile().exists()){
                pl(red + "That file's parent directory doesn't exist yet.");
                pl("I'm not going to create any directories, so go create them first\n"
                        + "if that's really where you want the file.");
            } else {
                break;
            }            
        }
        
        pl("Alright, I'm going to create a " + type + " type file at " + new File(filename).getAbsolutePath());
        pl("Is this alright? [Y/N]");
        if(prompt().equalsIgnoreCase("y")){
            doExport(type, new File(filename));
        }
    }
    
    public static void doExport(String type, File file){
        pl(red + "Actually, this feature isn't implemented yet :(");
    }
    
    public static void cleardb(){
        pl(red + "Are you absolutely sure you want to clear out your database? " + blinkon + "No backup is going to be made." + blinkoff);
        pl(white + "This will completely wipe your persistance information out. (No other data will be changed)");
        pl("[YES/No]");
        String choice = prompt();
        if(choice.equals("YES")){
            pl("Positive? [YES/No]");
            if(prompt().equals("YES")){
                p("Ok, here we go... ");
                Persistance db = GetDB();
                try {
                    db.load();
                    db.clearAllData();
                    db.save();
                } catch (Exception ex) {
                    pl(red + ex.getMessage());
                }
                pl("Done!");
            }
        } else if(choice.equalsIgnoreCase("yes")){
            pl("No, you have to type YES exactly.");
        }
    }
    
    public static void help(){
        pl("Currently, your options are:\n"
                        + "\t" + blue + "refactor" + white + " - Options for refactoring your persisted data from one backend to another\n"
                        + "\t" + green + "upgrade" + white + " - Runs upgrade scripts on your persisted data\n"
                        + "\t" + green + "print" + white + " - Prints out the information from your persisted data\n"
                        + "\t" + green + "cleardb" + white + " - Clears out your database of persisted data\n"
                        + "\t" + blue + "import" + white + " - Imports a text based file into the persistance database\n"
                        + "\t" + blue + "export" + white + " - Exports your persisted data to a text based file\n"
                        + "\t" + green + "edit" + white + " - Allows you to edit individual fields\n"
                        + "\n\t" + red + "exit" + white + " - Quits the Data Manager\n");
        if(system.equals(sys.UNIX)){
            pl(blue + "Blue" + white + " entries are not yet working.");
        }
    }

    public static void edit(){
        cls();
        while(true){
            pl("Would you like to " + green + "(a)dd/edit" + white 
                    + " a value, " + red + "(r)emove" + white + " a value, " + cyan 
                    + "(v)iew" + white + " a single value, or " 
                    + magenta + "(s)top" + white + " editting? [" + green + "A" + white + "/" 
                    + red + "R" + white + "/" + cyan + "V" + white + "/" + magenta + "S" + white + "]");
            String choice = prompt();
            if(choice.equalsIgnoreCase("s") || choice.equalsIgnoreCase("exit")){
                break;
            } else if(choice.equalsIgnoreCase("a")){
                pl("Type the name of the key " + yellow + "EXACTLY" + white + " as shown in the"
                        + " persistance format,\nnot the format you use when using store_value().");
                String key = prompt();
                pl("Provide a value for " + cyan + key + white + ". This value you provide will"
                        + " be interpreted as pure mscript. (So things like array() will work)");
                String value = prompt();
                if(doAddEdit(key, value)){
                    pl("Value changed!");
                }
            } else if(choice.equalsIgnoreCase("r")){
                pl("Type the name of the key " + yellow + "EXACTLY" + white + " as shown in the"
                        + " persistance format,\nnot the format you use when using store_value().");
                String key = prompt();
                if(doRemove(key)){
                    pl("Value removed!");
                } else {
                    pl("That value wasn't in the database to start with");
                }
            } else if(choice.equalsIgnoreCase("v")){
                pl("Type the name of the key " + yellow + "EXACTLY" + white + " as shown in the"
                        + " persistance format,\nnot the format you use when using store_value().");
                String key = prompt();
                doView(key);
            } else {
                pl("I'm sorry, that's not a valid choice.");
            }
        }
        
    }
    
    public static boolean doView(String key){
        Persistance db = GetDB();
        try {
            db.load();
        } catch (Exception ex) {
            pl(red + ex.getMessage());
        }
        if(!db.isKeySet(new String[]{key})){
            pl(red + "That value is not set!");
            return true;
        }
        pl(cyan + key + ":" + white + db.getValue(new String[]{key}));
        return true;
    }
    
    public static boolean doAddEdit(String key, String valueScript){
        try {
            Construct c = MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(valueScript, null)), new Env(), null, null);
            String value = Construct.json_encode(c, 0, null);
            pl(cyan + "Adding: " + white + value);
            Persistance db = GetDB();
            db.setValue(new String[]{key}, value);
            db.save();
            return true;
        } catch (Exception ex) {
            pl(red + ex.getMessage());
            return false;
        }        
    }
    
    public static boolean doRemove(String key){
        Persistance db = GetDB();
        try {
            db.load();
        } catch (Exception ex) {
            pl(red + ex.getMessage());
            return false;
        }
        if(db.isKeySet(new String[]{key})){
            db.setValue(new String[]{key}, null);
            return true;
        } else {
            return false;
        }
    }
    
    public static void print() {
        Map data = null;
        if (GetDB() instanceof SerializedPersistance) {
            File db = new File("CommandHelper/persistance.ser");
            if (!db.exists()) {
                pl("Looks like you haven't used your persistance file yet.");
                return;
            }
            SerializedPersistance sp = new SerializedPersistance(db, null);
            try {
                sp.load();
            } catch (Exception ex) {
                pl(red + ex.getMessage());
            }
            data = sp.rawData();
        }
        pl();
        if (data != null) {
            for (Object key : data.keySet()) {
                pl(cyan + key.toString() + ": " + white + data.get(key).toString());
            }
        }
    }

    public static void upgrade() {
        pl("\nThis will automatically detect and upgrade your persisted data. Though this will"
                + " create a backup for you, you should manually back up your data before running"
                + " this utility.");
        pl("Would you like to continue? [" + green + "Y" + white + "/"
                + red + "N" + white + "]");
        String choice = prompt();
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
                        pl(red + ex.getMessage());
                    }
                    Map<String, Serializable> data = sp.rawData();
                    if (data.isEmpty()) {
                        pl("Looks like you haven't used your persistance file yet.");
                        return;
                    }
                    sp.clearAllData(); //Bye bye!
                    //Ok, now we need to determine the type of data we're currently working with
                    p(white + "Working");
                    int counter = 0;
                    int changes = 0;
                    Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
                    for (String key : data.keySet()) {
                        counter++;
                        int c = counter / 20;
                        if(c == ((double)counter / 20.0)){
                            p(color(colors[c % 6]) + ".");
                        }
                        if (key.contains("plugin.com.sk89q.commandhelper.CommandHelperPlugin.commandhelper.function.storage.")) {
                            //We're in version 1, and we need to upgrade to version 2
                            String newKey = "storage." + key.replaceFirst("plugin\\.com\\.sk89q\\.commandhelper\\.CommandHelperPlugin\\.commandhelper\\.function\\.storage\\.", "");
                            sp.rawData().put(newKey, data.get(key));
                            changes++;
                        }
                    }
                    try {
                        sp.save();
                    } catch (Exception ex) {
                        pl(red + ex.getMessage());
                    }
                    pl();
                    pl(green + "Assuming there are no error messages above, it should be upgraded now! (Use print to verify)");
                    pl(cyan.toString() + changes + " change" + (changes==1?" was":"s were") + " made");

                } catch (IOException ex) {
                    pl(red + ex.getMessage());
                }
            }
        } else {
            pl(red + "Upgrade Cancelled");
        }
    }
    
    public static Persistance GetDB(){
        //Figure out what engine they're using
        //For now, it's obviously SerializedPersistance
        return new SerializedPersistance(new File("CommandHelper/persistance.ser"), null);
    }

    public static void p(CharSequence c) {
        System.out.print(c);
    }

    public static void pl() {
        pl("");
    }
    
    public static String prompt(){
        p(">" + magenta);
        System.out.flush();
        String ret = scanner.nextLine();
        p(white);
        return ret;
    }

    public static void pl(CharSequence c) {
        System.out.println(c + white);
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
    
    public static void cls(){
        if(system.equals(sys.WINDOWS)){
            //Fuck you windows.
            for(int i = 0; i < 50; i++){
                pl();
            }
        } else {           
            System.out.print("\u001b[2J");
            System.out.flush();
        }
    }
    
    public static final String red = color(Color.RED).toString();
    public static final String green = color(Color.GREEN).toString();
    public static final String blue = color(Color.BLUE).toString();
    public static final String yellow = color(Color.YELLOW).toString();
    public static final String cyan = color(Color.CYAN).toString();
    public static final String magenta = color(Color.MAGENTA).toString();
    public static final String black = color(Color.BLACK).toString();
    public static final String white = color(Color.WHITE).toString();
    
    public static final String blinkon = special("blinkon");
    public static final String blinkoff = special("blinkoff");
    
    private static String special(String type){
        if(system.equals(sys.UNIX)){
            if(type.equals("blinkon")){
                return "\033[5m";
            }
            if(type.equals("blinkoff")){
                return "\033[25m";
            }
        }
        return "";
    }

    private static CharSequence color(Color c) {
        return ansi().fg(c).toString();
        /*if (system.equals(sys.WINDOWS)) {
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
        return "\033[" + color + "m";*/
    }
}
