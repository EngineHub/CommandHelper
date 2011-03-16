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
import org.bukkit.entity.Player;

/**
 * This class contains all the handling code. It only deals with built-in Java Objects,
 * so that if the Minecraft API Hook changes, porting the code will only require changing
 * the API specific portions, not this core file.
 * @author Layton
 */
public class AliasCore {
    private boolean allowCustomAliases = true;
    private int maxCustomAliases = 50;
    private int maxCommands = 5;
    private File aliasConfig;
    AliasConfig config;
    static final Logger logger = Logger.getLogger("Minecraft");
    private ArrayList<String> echoCommand = new ArrayList<String>();
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
    public boolean alias(String command, Player player, ArrayList<AliasConfig> playerCommands){
        //If player is null, we are running the test harness, so don't
        //actually add the player to the array.
        if(player != null && echoCommand.contains(player.getName())){
            //we are running one of the expanded commands, so exit with false
            return false;
        }

        //Global aliases override personal ones, so check the list first
        RunnableAlias a = config.getRunnableAliases(command);

        if(a == null){
            //if we are still looking, look in the aliases for this player
            for(AliasConfig ac : playerCommands){
                RunnableAlias b = ac.getRunnableAliases(command);
                if(b != null){
                    a = b;
                }
            }

        }

        if(a == null){
            //apparently we couldn't find the command, so return false
            return false;
        } else{
            //Run all the aliases
            a.player = player;
            if(player != null) echoCommand.add(player.getName());
            a.run();
            if(player != null) echoCommand.remove(player.getName());
            return true;
        }
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
        } catch(Throwable t){
            t.printStackTrace();
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

    public ArrayList<AliasConfig> parse_user_config(String[] config) throws ConfigCompileException{
        ArrayList<AliasConfig> alac = new ArrayList<AliasConfig>();
        for(int i = 0; i < config.length; i++){
            alac.add( new AliasConfig(config[i]) );
        }
        return alac;
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
}
