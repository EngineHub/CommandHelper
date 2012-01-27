/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.tools;

import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.PureUtilities.SerializedPersistance;
import com.laytonsmith.PureUtilities.fileutility.FileUtility;
import com.laytonsmith.core.Env;
import com.laytonsmith.core.MScriptCompiler;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.laytonsmith.PureUtilities.TermColors.*;
/**
 *
 * @author layton
 */
public class Manager {

    private static Scanner scanner;

    public static void start() {
        cls();
        pl("\n" + Static.Logo() + "\n\n" + Static.DataManagerLogo());
        scanner = new Scanner(System.in);
        
        pl("Starting the Data Manager...");
        try {
            MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex("player()", null)), new Env(), null, null);
        } catch (ConfigCompileException ex) {}
        pl(GREEN + "Welcome to the CommandHelper " + CYAN + "Data Manager!");
        pl(BLINKON + RED + "Warning!" + BLINKOFF + YELLOW + " Be sure your server is not running before using this tool to make changes to your database!");
        pl("------------------------");
        boolean finished = false;
        do {
            pl(YELLOW + "What function would you like to run? Type \"help\" for a full list of options.");
            String input = prompt();
            pl();
            if (input.toLowerCase().startsWith("help")) {
                help(input.replaceFirst("help ?", "").toLowerCase().split(" "));
            } else if (input.equalsIgnoreCase("refactor")) {
                pl("refactor - That feature isn't implemented yet :(");
            } else if (input.toLowerCase().startsWith("print")) {                
                print(input.replaceFirst("print ?", "").toLowerCase().split(" "));
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
            } else if(input.equalsIgnoreCase("interpreter")){
                Interpreter.start();
            } else if (input.equalsIgnoreCase("exit")) {
                pl("Thanks for using the " + CYAN + "Data Manager!");
                finished = true;
            } else {
                pl("I'm sorry, that's not a valid command. Here's the help:");
                help(new String[]{});
            }
        } while (finished == false);
    }
    
    public static void export(){
        cls();
        pl(GREEN + "Export creates a text based file that you can use to get your persisted data\n"
         + "out of the database. You have various options for the export type.");
        String type;
        while(true){
            pl(YELLOW + "What export type would you like? The options are: XML, plain text, INI, and YML.");
            pl("[XML/TEXT/INI/YML]");
            type = prompt().toLowerCase();
            if(type.equals("xml") || type.equals("text") || type.equals("ini") || type.equals("yml")){
                break;
            } else {
                pl(RED + "That's not a valid type.");
            }
        }
        
        String filename;
        while(true){
            pl("Give me a filename to store it in:");
            filename = prompt();
            File file = new File(filename);
            if(file.exists()){
                pl(RED + "That file already exists, do you want to overwrite it? " + WHITE + "[Y/N]");
                if(prompt().equalsIgnoreCase("y")){
                    break;
                }
            } else if(file.getAbsoluteFile().getParentFile() != null && !file.getAbsoluteFile().getParentFile().exists()){
                pl(RED + "That file's parent directory doesn't exist yet.");
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
        pl(RED + "Actually, this feature isn't implemented yet :(");
    }
    
    public static void cleardb(){
        pl(RED + "Are you absolutely sure you want to clear out your database? " + BLINKON + "No backup is going to be made." + BLINKOFF);
        pl(WHITE + "This will completely wipe your persistance information out. (No other data will be changed)");
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
                    pl(RED + ex.getMessage());
                }
                pl("Done!");
            }
        } else if(choice.equalsIgnoreCase("yes")){
            pl("No, you have to type YES exactly.");
        }
    }
    
    public static void help(String [] args){
        if(args[0].equals("")){
            pl("Currently, your options are:\n"
                            + "\t" + BLUE + "refactor" + WHITE + " - Options for refactoring your persisted data from one backend to another\n"
                            + "\t" + GREEN + "upgrade" + WHITE + " - Runs upgrade scripts on your persisted data\n"
                            + "\t" + GREEN + "print" + WHITE + " - Prints out the information from your persisted data\n"
                            + "\t" + GREEN + "cleardb" + WHITE + " - Clears out your database of persisted data\n"
                            + "\t" + BLUE + "import" + WHITE + " - Imports a text based file into the persistance database\n"
                            + "\t" + BLUE + "export" + WHITE + " - Exports your persisted data to a text based file\n"
                            + "\t" + GREEN + "edit" + WHITE + " - Allows you to edit individual fields\n"
                            + "\t" + GREEN + "interpreter" + WHITE + " - Command Line Interpreter mode. Most minecraft related functions don't work.\n"
                            + "\n\t" + RED + "exit" + WHITE + " - Quits the Data Manager\n");
            
            pl("Type " + MAGENTA + "help <command>" + WHITE + " for more details about a specific command");
            if(SYSTEM.equals(SYS.UNIX)){
                pl(BLUE + "Blue" + WHITE + " entries are not yet working.");
            }
        } else {
            if(args[0].equals("refactor")){
                pl("Not implemented yet");
            } else if(args[0].equals("upgrade")){
                pl("Converts any old formatted data into the new format. Any data that doesn't explicitely"
                        + " match the old format is not touched.");
            } else if(args[0].equals("print")){
                pl("Prints out the information in your persistance file. Entries may be narrowed down by"
                        + " specifying the namespace (for instance " + MAGENTA + "print user.username" + WHITE
                        + " will only show that particular users's aliases.) This is namespace based, so you"
                        + " must provide the entire namespace that your are trying to narrow down."
                        + "(" + MAGENTA + "print storage" + WHITE + " is valid, but " + MAGENTA + "print stor"
                        + WHITE + " is not)");
            } else if(args[0].equals("cleardb")){
                pl("Wipes your database clean of CommandHelper's persistance entries, but not other data. This"
                        + " includes any data that CommandHelper would have inserted into the database, or data"
                        + " that CommandHelper otherwise knows how to use. If using SerializedPersistance, this"
                        + " means the entire file. For other data backends, this may vary slightly, for instance,"
                        + " an SQL backend would only have the CH specific tables truncated, but the rest of the"
                        + " database would remain untouched.");
            } else if(args[0].equals("import")){
                pl("Not implemented yet");
            } else if(args[0].equals("export")){
                pl("Not implemented yet");
            } else if(args[0].equals("edit")){
                pl("Allows you to manually edit the values in the database. You have the option to add or edit an existing"
                        + " value, delete a single value, or view the value of an individual key.");
            } else if(args[0].equals("exit")){
                pl("Exits the data manager");
            } else if(args[0].equals("interpreter")){
                pl("Generally speaking, works the same as the in game interpreter mode, but none"
                        + " of the minecraft related functions will work. You should not"
                        + " run this while the server is operational.");
            } else {
                pl("That's not a recognized command: '" + args[0] + "'");
            }
        }
    }

    public static void edit(){
        cls();
        while(true){
            pl("Would you like to " + GREEN + "(a)dd/edit" + WHITE 
                    + " a value, " + RED + "(r)emove" + WHITE + " a value, " + CYAN 
                    + "(v)iew" + WHITE + " a single value, or " 
                    + MAGENTA + "(s)top" + WHITE + " editting? [" + GREEN + "A" + WHITE + "/" 
                    + RED + "R" + WHITE + "/" + CYAN + "V" + WHITE + "/" + MAGENTA + "S" + WHITE + "]");
            String choice = prompt();
            if(choice.equalsIgnoreCase("s") || choice.equalsIgnoreCase("exit")){
                break;
            } else if(choice.equalsIgnoreCase("a")){
                pl("Type the name of the key " + YELLOW + "EXACTLY" + WHITE + " as shown in the"
                        + " persistance format,\nnot the format you use when using store_value().");
                String key = prompt();
                pl("Provide a value for " + CYAN + key + WHITE + ". This value you provide will"
                        + " be interpreted as pure mscript. (So things like array() will work)");
                String value = prompt();
                if(doAddEdit(key, value)){
                    pl("Value changed!");
                }
            } else if(choice.equalsIgnoreCase("r")){
                pl("Type the name of the key " + YELLOW + "EXACTLY" + WHITE + " as shown in the"
                        + " persistance format,\nnot the format you use when using store_value().");
                String key = prompt();
                if(doRemove(key)){
                    pl("Value removed!");
                } else {
                    pl("That value wasn't in the database to start with");
                }
            } else if(choice.equalsIgnoreCase("v")){
                pl("Type the name of the key " + YELLOW + "EXACTLY" + WHITE + " as shown in the"
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
            pl(RED + ex.getMessage());
        }
        if(!db.isKeySet(new String[]{key})){
            pl(RED + "That value is not set!");
            return true;
        }
        pl(CYAN + key + ":" + WHITE + db.getValue(new String[]{key}));
        return true;
    }
    
    public static boolean doAddEdit(String key, String valueScript){
        try {
            Construct c = MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(valueScript, null)), new Env(), null, null);
            String value = Construct.json_encode(c, 0, null);
            pl(CYAN + "Adding: " + WHITE + value);
            Persistance db = GetDB();
            db.setValue(new String[]{key}, value);
            db.save();
            return true;
        } catch (Exception ex) {
            pl(RED + ex.getMessage());
            return false;
        }        
    }
    
    public static boolean doRemove(String key){
        Persistance db = GetDB();
        try {
            db.load();
        } catch (Exception ex) {
            pl(RED + ex.getMessage());
            return false;
        }
        if(db.isKeySet(new String[]{key})){
            db.setValue(new String[]{key}, null);
            return true;
        } else {
            return false;
        }
    }
    
    public static void print(String [] args) {
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
                pl(RED + ex.getMessage());
            }
            data = sp.rawData();
        }
        pl();
        if (data != null) {
            int count = 0;
            for (Object key : data.keySet()) {
                if(!args[0].equals("")){
                    //We are splitting by namespace
                    if(!key.toString().toLowerCase().startsWith(args[0] + ".")){
                        continue;
                    }
                }
                pl(CYAN + key.toString() + ": " + WHITE + data.get(key).toString());
                count++;
            }
            pl(BLUE + count + " items found");
        }
    }

    public static void upgrade() {
        pl("\nThis will automatically detect and upgrade your persisted data. Though this will"
                + " create a backup for you, you should manually back up your data before running"
                + " this utility.");
        pl("Would you like to continue? [" + GREEN + "Y" + WHITE + "/"
                + RED + "N" + WHITE + "]");
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
                        pl(RED + ex.getMessage());
                    }
                    Map<String, Serializable> data = sp.rawData();
                    if (data.isEmpty()) {
                        pl("Looks like you haven't used your persistance file yet.");
                        return;
                    }
                    sp.clearAllData(); //Bye bye!
                    //Ok, now we need to determine the type of data we're currently working with
                    p(WHITE + "Working");
                    int counter = 0;
                    int changes = 0;
                    Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA};
                    for (String key : data.keySet()) {
                        counter++;
                        int c = counter / 20;
                        if(c == ((double)counter / 20.0)){
                            p(color(colors[c % 6]) + ".");
                        }
                        if (key.matches("^plugin\\.com\\.sk89q\\.commandhelper\\.CommandHelperPlugin\\.commandhelper\\.function\\.storage\\..*")) {
                            //We're in version 1, and we need to upgrade to version 2
                            String newKey = "storage." + key.replaceFirst("plugin\\.com\\.sk89q\\.commandhelper\\.CommandHelperPlugin\\.commandhelper\\.function\\.storage\\.", "");
                            sp.rawData().put(newKey, data.get(key));
                            changes++;
                        } else if(key.matches("^plugin\\.com\\.sk89q\\.commandhelper\\.CommandHelperPlugin\\..*?\\.aliases\\.\\d+$")){
                            //Pull out the parts we need
                            Pattern p = Pattern.compile("^plugin\\.com\\.sk89q\\.commandhelper\\.CommandHelperPlugin\\.(.*?)\\.aliases\\.(\\d+)$");
                            Matcher m = p.matcher(key);
                            String newKey = null;
                            if(m.find()){
                                String username = m.group(1);
                                String id = m.group(2);
                                newKey = "user." + username + ".aliases." + id;
                            }
                            //If something went wrong, just put the old one back in
                            if(newKey == null){
                                sp.rawData().put(key, data.get(key));
                            } else {
                                sp.rawData().put(newKey, data.get(key));
                                changes++;
                            }
                        } else {
                            sp.rawData().put(key, data.get(key));
                        }
                    }
                    try {
                        sp.save();
                    } catch (Exception ex) {
                        pl(RED + ex.getMessage());
                    }
                    pl();
                    pl(GREEN + "Assuming there are no error messages above, it should be upgraded now! (Use print to verify)");
                    pl(CYAN.toString() + changes + " change" + (changes==1?" was":"s were") + " made");

                } catch (IOException ex) {
                    pl(RED + ex.getMessage());
                }
            }
        } else {
            pl(RED + "Upgrade Cancelled");
        }
    }
    
    public static Persistance GetDB(){
        //Figure out what engine they're using
        //For now, it's obviously SerializedPersistance
        return new SerializedPersistance(new File("CommandHelper/persistance.ser"), null);
    }

    public static void p(CharSequence c) {
        System.out.print(c);
        System.out.flush();
    }

    public static void pl() {
        pl("");
    }
    
    public static String prompt(){
        p(">" + MAGENTA);
        System.out.flush();
        String ret = scanner.nextLine();
        p(WHITE);
        return ret;
    }

    public static void pl(CharSequence c) {
        System.out.println(c + WHITE);
    }

    
}
