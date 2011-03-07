/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.laytonsmith.aliasengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains all the handling code. It only deals with built-in Java Objects,
 * so that if the Minecraft API Hook changes, porting the code will only require changing
 * the API specific portions, not this core file.
 * @author Layton
 */
public class AliasCore {
    private boolean allowCustomAliases = true;
    private int maxCustomAliases = 10;
    private int maxCommands = 5;
    private File aliasConfig;
    AliasConfig config;
    static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * This constructor accepts the configuration settings for the plugin, and ensures
     * that the manager uses these settings.
     * @param allowCustomAliases Whether or not to allow users to add their own personal aliases
     * @param maxCustomAliases How many aliases a player is allowed to have. -1 is unlimited.
     * @param maxCommands How many commands an alias may contain. Since aliases can be used like a
     * macro, this can help prevent command spamming.
     */
    public AliasCore(boolean allowCustomAliases, int maxCustomAliases, int maxCommands,
            File aliasConfig) throws ConfigCompileException{
        this.allowCustomAliases = allowCustomAliases;
        this.maxCustomAliases = maxCustomAliases;
        this.maxCommands = maxCommands;
        this.aliasConfig = aliasConfig;
        reload();
    }
    /**
     * This is the workhorse function. It takes a given command, then converts it
     * into the actual command(s). If the command maps to a defined alias, it will
     * return an ArrayList of actual commands to run. It will search through the
     * global list of aliases, as well as the aliases defined for that specific player.
     * This function doesn't handle the /alias command however.
     * @param command
     * @return
     */
    public ArrayList<String> alias(String command, String playerName){
        String[] cmds = command.split(" ");
        ArrayList<String> args = new ArrayList(Arrays.asList(cmds));
        args.remove(0);
        command = cmds[0].substring(1);
        //Global aliases override personal ones, so check the list first
        config.getRunnableAliases(command, playerName);

        //if we are still looking, look in the aliases for this player


        //apparently we couldn't find the command, so return null
        return null;
    }


    /**
     * Loads the global alias file in from
     */
    public boolean reload() throws ConfigCompileException{
        boolean is_loaded = false;
        try {
            String alias_config = file_get_contents(aliasConfig.getAbsolutePath()); //get the file again
            config = new AliasConfig(alias_config);
            is_loaded = true;
        } catch (ConfigCompileException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch(IOException ex){
            logger.log(Level.SEVERE, null, "Path to config file is not correct/accessable. Please"
                    + " check the location and try loading the plugin again.");
        } finally {
            if(!is_loaded){
                //Try and pull the old config file, if it exists
                boolean old_file_exists = false;


                if(!old_file_exists){
                    throw new ConfigCompileException("Unable to load working config file, aborting plugin operation", 0);
                }
            }
        }
        return is_loaded;
    }

    /**
     * Returns the contents of a file as a string. Accepts the file location
     * as a string.
     * @param file_location
     * @return the contents of the file as a string
     * @throws Exception if the file cannot be found
     */
    public static String file_get_contents(String file_location) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file_location));
        String ret = "";
        String str;
        while ((str = in.readLine()) != null) {
            ret += str + "\n";
        }
        in.close();
        return ret;
    }

    public class builtin{
        public String player(){
            return "";
        }

        public String data_values(String name){
            return name;
        }

        public String die(String message) throws CancelCommandException{
            throw new CancelCommandException();
        }
    }

}
