/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine;

import com.laytonsmith.aliasengine.functions.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.functions.exceptions.ConfigRuntimeException;
import com.laytonsmith.PureUtilities.Preferences;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.commandhelper.CommandHelperPlugin;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * This class contains all the handling code. It only deals with built-in Java Objects,
 * so that if the Minecraft API Hook changes, porting the code will only require changing
 * the API specific portions, not this core file.
 * @author Layton
 */
public class AliasCore {

    private File aliasConfig;
    private File prefFile;
    //AliasConfig config;
    List<Script> scripts;
    static final Logger logger = Logger.getLogger("Minecraft");
    private ArrayList<String> echoCommand = new ArrayList<String>();
    private PermissionsResolverManager perms;
    public static CommandHelperPlugin parent;

    /**
     * This constructor accepts the configuration settings for the plugin, and ensures
     * that the manager uses these settings.
     * @param allowCustomAliases Whether or not to allow users to add their own personal aliases
     * @param maxCustomAliases How many aliases a player is allowed to have. -1 is unlimited.
     * @param maxCommands How many commands an alias may contain. Since aliases can be used like a
     * macro, this can help prevent command spamming.
     */
    public AliasCore(File aliasConfig, File prefFile, PermissionsResolverManager perms, CommandHelperPlugin parent) throws ConfigCompileException {
        this.aliasConfig = aliasConfig;
        this.prefFile = prefFile;
        this.perms = perms;
        this.parent = parent;
        reload();
    }

    /**
     * This is the workhorse function. It takes a given command, then converts it
     * into the actual command(s). If the command maps to a defined alias, it will
     * run the specified alias. It will search through the
     * global list of aliases, as well as the aliases defined for that specific player.
     * This function doesn't handle the /alias command however.
     * @param command
     * @return
     */
    public boolean alias(String command, final Player player, ArrayList<Script> playerCommands) {

        if (scripts == null) {
            throw new ConfigRuntimeException("Cannot run alias commands, no config file is loaded", 0);
        }

        boolean match = false;
        try { //catch RuntimeException
            //If player is null, we are running the test harness, so don't
            //actually add the player to the array.
            if (player != null && echoCommand.contains(player.getName())) {
                //we are running one of the expanded commands, so exit with false
                return false;
            }

            //Global aliases override personal ones, so check the list first
            //a = config.getRunnableAliases(command, player);
            for (Script s : scripts) {
                if (s.match(command)) {
                    echoCommand.add(player.getName());
                    if ((Boolean) Static.getPreferences().getPreference("console-log-commands")) {
                        Static.getLogger().log(Level.INFO, "CH: Running original command ----> " + command);
                        Static.getLogger().log(Level.INFO, "on player " + player.getName());
                    }
                    try {
                        s.run(s.getVariables(command), player, new MScriptComplete() {

                            public void done(String output) {
                                try {
                                    if (output != null) {
                                        if (!output.trim().equals("") && output.trim().startsWith("/")) {
                                            if ((Boolean) Static.getPreferences().getPreference("debug-mode")) {
                                                Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + player.getName() + ": " + output.trim());
                                            }
                                            //Sometimes bukkit works with one version of this, sometimes with the other. performCommand would be prefered, but
                                            //chat works more often, because chat actually triggers a CommandPreprocessEvent, unlike performCommand.
                                            player.chat(output.trim());
                                            //player.performCommand(output.trim().substring(1));
                                        }
                                    }
                                } catch (Throwable e) {
                                    System.err.println(e.getMessage());
                                    player.sendMessage(ChatColor.RED + e.getMessage());
                                } finally {
                                    echoCommand.remove(player.getName());
                                }
                            }
                        });
                    } catch (/*ConfigRuntimeException*/Throwable e) {
                        System.err.println("An unexpected exception occured: " + e.getClass().getSimpleName());
                        player.sendMessage("An unexpected exception occured: " + ChatColor.RED + e.getClass().getSimpleName());
                        e.printStackTrace();
                    } finally {
                        echoCommand.remove(player.getName());
                    }
                    match = true;
                    break;
                }
            }

            if (match == false && playerCommands != null) {
                //if we are still looking, look in the aliases for this player
                for (Script ac : playerCommands) {
                    //RunnableAlias b = ac.getRunnableAliases(command, player);
                    try {
                        ac.compile();
                        if (ac.match(command)) {
                            echoCommand.add(player.getName());
                            try {
                                ac.run(ac.getVariables(command), player, new MScriptComplete() {

                                    public void done(String output) {
                                        if (output != null) {
                                            if (!output.trim().equals("") && output.trim().startsWith("/")) {
                                                if ((Boolean) Static.getPreferences().getPreference("debug-mode")) {
                                                    Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + player.getName() + ": " + output.trim());
                                                }
                                                player.chat(output.trim());
                                                //player.performCommand(output.trim().substring(1));
                                            }
                                        }
                                        echoCommand.remove(player.getName());
                                    }
                                });
                            } catch (/*ConfigRuntimeException*/Throwable e) {
                                System.err.println(e.getMessage());
                                player.sendMessage(ChatColor.RED + e.getMessage());
                                echoCommand.remove(player.getName());
                            }
                            match = true;
                        }
                    } catch (Exception e) {
                        player.chat("An exception occured while trying to compile/run your alias: " + e.getMessage());
                    }
                }

            }
        } catch (Throwable e) {
            throw new InternalException("An error occured in the CommandHelper plugin: " + e.getMessage() + Arrays.asList(e.getStackTrace()));
        }
        return match;
    }

    /**
     * Loads the global alias file in from
     */
    public final boolean reload() throws ConfigCompileException {
        boolean is_loaded = false;
        try {
            if (!aliasConfig.exists()) {
                aliasConfig.getParentFile().mkdirs();
                aliasConfig.createNewFile();
                try {
                    file_put_contents(aliasConfig,
                            getStringResource(AliasCore.class.getResourceAsStream("/com/laytonsmith/aliasengine/samp_config.txt")),
                            "o");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "CommandHelper: Could not write sample config file");
                }
            }

            Preferences prefs = Static.getPreferences();
            prefs.init(prefFile);

            String alias_config = file_get_contents(aliasConfig.getAbsolutePath()); //get the file again
            //config = new AliasConfig(alias_config, null, perms);
            scripts = MScriptCompiler.preprocess(MScriptCompiler.lex(alias_config));
            for (Script s : scripts) {
                try {
                    s.compile();
                    s.checkAmbiguous((ArrayList<Script>) scripts);
                } catch (ConfigCompileException e) {
                    logger.log(Level.SEVERE, "[CommandHelper]: " + e.toString() + "\nCompilation will continue.");
                }
            }
            int errors = 0;
            for (Script s : scripts) {
                if (s.compilerError) {
                    errors++;
                }
            }
            if (errors > 0) {
                System.out.println("[CommandHelper]: " + (scripts.size() - errors) + " alias(es) defined, with " + errors + " aliases with compile errors.");
            } else {
                System.out.println("[CommandHelper]: " + scripts.size() + " alias(es) defined.");
            }
            is_loaded = true;
        } catch (ConfigCompileException ex) {
            logger.log(Level.SEVERE, "CommandHelper: " + ex.toString());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "CommandHelper: Path to config file is not correct/accessable. Please"
                    + " check the location and try loading the plugin again.");
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return is_loaded;
    }

//    public ArrayList<AliasConfig> parse_user_config(ArrayList<String> config, User u) throws ConfigCompileException {
//        if (config == null) {
//            return null;
//        }
//        ArrayList<AliasConfig> alac = new ArrayList<AliasConfig>();
//        for (int i = 0; i < config.size(); i++) {
//            alac.add(new AliasConfig(config.get(i), u, perms));
//        }
//        return alac;
//    }
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

    /**
     * This function writes the contents of a string to a file.
     * @param file_location the location of the file on the disk
     * @param contents the string to be written to the file
     * @param mode the mode in which to write the file: <br />
     * <ul>
     * <li>"o" - overwrite the file if it exists, without asking</li>
     * <li>"a" - append to the file if it exists, without asking</li>
     * <li>"c" - cancel the operation if the file exists, without asking</li>
     * </ul>
     * @return true if the file was written, false if it wasn't. Throws an exception
     * if the file could not be created, or if the mode is not valid.
     * @throws Exception if the file could not be created
     */
    public static boolean file_put_contents(File file_location, String contents, String mode)
            throws Exception {
        BufferedWriter out = null;
        File f = file_location;
        if (f.exists()) {
            //do different things depending on our mode
            if (mode.equalsIgnoreCase("o")) {
                out = new BufferedWriter(new FileWriter(file_location));
            } else if (mode.equalsIgnoreCase("a")) {
                out = new BufferedWriter(new FileWriter(file_location, true));
            } else if (mode.equalsIgnoreCase("c")) {
                return false;
            } else {
                throw new RuntimeException("Undefined mode in file_put_contents: " + mode);
            }
        } else {
            out = new BufferedWriter(new FileWriter(file_location));
        }
        //At this point, we are assured that the file is open, and ready to be written in
        //from this point in the file.
        if (out != null) {
            out.write(contents);
            out.close();
            return true;
        } else {
            return false;
        }
    }

    public static String getStringResource(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return writer.toString();
    }
}
