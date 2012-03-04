/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.PureUtilities.TermColors;
import com.laytonsmith.abstraction.MCChatColor;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigCompileException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.IncludeCache;
import com.sk89q.util.StringUtil;
import com.sk89q.wepif.PermissionsResolverManager;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains all the handling code. It only deals with built-in Java Objects,
 * so that if the Minecraft API Hook changes, porting the code will only require changing
 * the API specific portions, not this core file.
 * @author Layton
 */
public class AliasCore {

    private File aliasConfig;
    private File prefFile;
    private File mainFile;
    //AliasConfig config;
    List<Script> scripts;
    static final Logger logger = Logger.getLogger("Minecraft");
    private Set<String> echoCommand = new HashSet<String>();
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
    public AliasCore(File aliasConfig, File prefFile, File mainFile, PermissionsResolverManager perms, CommandHelperPlugin parent) throws ConfigCompileException {
        this.aliasConfig = aliasConfig;
        this.prefFile = prefFile;
        this.perms = perms;
        this.parent = parent;
        this.mainFile = mainFile;
        reload(null);
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
    public boolean alias(String command, final MCCommandSender player, List<Script> playerCommands) {
        
        Env env = new Env();
        env.SetCommandSender(player);

        if (scripts == null) {
            throw new ConfigRuntimeException("Cannot run alias commands, no config file is loaded", 0, null);
        }

        boolean match = false;
        try { //catch RuntimeException
            //If player is null, we are running the test harness, so don't
            //actually add the player to the array.
            if (player != null && player instanceof MCPlayer && echoCommand.contains(((MCPlayer) player).getName())) {
                //we are running one of the expanded commands, so exit with false
                return false;
            }

            //Global aliases override personal ones, so check the list first
            //a = config.getRunnableAliases(command, player);
            for (Script s : scripts) {
                try {
                    if (s.match(command)) {
                        this.addPlayerReference(player);
                        if (Prefs.ConsoleLogCommands()) {
                            StringBuilder b = new StringBuilder("CH: Running original command ");
                            if (player instanceof MCPlayer) {
                                b.append("on player ").append(((MCPlayer) player).getName());
                            } else {
                                b.append("from a MCCommandSender");
                            }
                            b.append(" ----> ").append(command);
                            Static.getLogger().log(Level.INFO, b.toString());
                        }
                        try {
                            env.SetCommand(command);
                            s.run(s.getVariables(command), env, new MScriptComplete() {

                                public void done(String output) {
                                    try {
                                        if (output != null) {
                                            if (!output.trim().equals("") && output.trim().startsWith("/")) {
                                                if (Prefs.DebugMode()) {
                                                    if (player instanceof MCPlayer) {
                                                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((MCPlayer) player).getName() + ": " + output.trim());
                                                    } else {
                                                        Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command from console equivalent: " + output.trim());
                                                    }
                                                }
                                                
                                                if (player instanceof MCPlayer) {
                                                    ((MCPlayer) player).chat(output.trim());
                                                } else {
                                                    Static.getServer().dispatchCommand(player, output.trim().substring(1));
                                                }
                                            }
                                        }
                                    } catch (Throwable e) {
                                        System.err.println(e.getMessage());
                                        player.sendMessage(MCChatColor.RED + e.getMessage());
                                    } finally {
                                        Static.getAliasCore().removePlayerReference(player);
                                    }
                                }
                            });
                        } catch(ConfigRuntimeException ex){
                            ex.setEnv(env);
                            switch(ConfigRuntimeException.HandleUncaughtException(ex)){
                                case REPORT:
                                    ConfigRuntimeException.DoReport(ex);
                                    break;
                                case IGNORE:
                                    break;
                                case FATAL:
                                    throw ex;
                                default:
                                    break;
                            }
                        } catch (Throwable e) {
                            //This is not a simple user script error, this is a deeper problem, so we always handle this.
                            System.err.println("An unexpected exception occured: " + e.getClass().getSimpleName());
                            player.sendMessage("An unexpected exception occured: " + MCChatColor.RED + e.getClass().getSimpleName());
                            e.printStackTrace();
                        } finally {
                            Static.getAliasCore().removePlayerReference(player);
                        }
                        match = true;
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("An unexpected exception occured inside the command " + s.toString());
                    e.printStackTrace();
                }
            }

            if (player instanceof MCPlayer) {
                if (match == false && playerCommands != null) {
                    //if we are still looking, look in the aliases for this player
                    for (Script ac : playerCommands) {
                        //RunnableAlias b = ac.getRunnableAliases(command, player);
                        try {
                            
                            ac.compile();
                            
                            if (ac.match(command)) {
                                Static.getAliasCore().addPlayerReference(player);
                                ac.run(ac.getVariables(command), env, new MScriptComplete() {

                                    public void done(String output) {
                                        if (output != null) {
                                            if (!output.trim().equals("") && output.trim().startsWith("/")) {
                                                if (Prefs.DebugMode()) {
                                                    Static.getLogger().log(Level.INFO, "[CommandHelper]: Executing command on " + ((MCPlayer)player).getName() + ": " + output.trim());
                                                }
                                                ((MCPlayer)player).chat(output.trim());
                                            }
                                        }
                                        Static.getAliasCore().removePlayerReference(player);
                                    }
                                });
                                match = true;
                                break;
                            }
                        } catch (ConfigRuntimeException e) {
                            //Unlike system scripts, this should just report the problem to the player
                            if(e.getEnv() == null){
                                e.setEnv(new Env());
                            }
                            e.getEnv().SetCommandSender(player);
                            Static.getAliasCore().removePlayerReference(player);
                            ConfigRuntimeException.DoReport(e);
                        } catch(ConfigCompileException e){
                            //Something strange happened, and a bad alias was added
                            //to the database. Our best course of action is to just
                            //skip it.
                        }
                    }

                }
            }
        } catch (Throwable e) {
            //Not only did an error happen, an error happened in our error handler
            throw new InternalException(TermColors.RED + "An unexpected error occured in the CommandHelper plugin. "
                    + "Further, this is likely an error with the error handler, so it may be caused by your script, "
                    + "however, there is no more information at this point. Check your script, but also report this "
                    + "as a bug in CommandHelper. Also, it's possible that some commands will no longer work. As a temporary "
                    + "workaround, restart the server, and avoid doing whatever it is you did to make this happen.\nThe error is as follows: " 
                    + e.toString() + "\n" + TermColors.reset() + "Stack Trace:\n" + StringUtil.joinString(Arrays.asList(e.getStackTrace()), "\n", 0));
        }
        return match;
    }

    /**
     * Loads the global alias file in from the file system. If a player is
     * running the command, send a reference to them, and they will see
     * compile errors, otherwise, null.
     */
    public final boolean reload(MCPlayer player) {
        boolean is_loaded = true;
        try {
            Globals.clear();
            EventUtils.UnregisterAll();            
            IncludeCache.clearCache(); //Clear the include cache, so it re-pulls files
            if (!aliasConfig.exists()) {
                aliasConfig.getParentFile().mkdirs();
                aliasConfig.createNewFile();
                try {
                    String samp_config = getStringResource(AliasCore.class.getResourceAsStream("/samp_config.txt"));
                    //Because the sample config may have been written an a machine that isn't this type, replace all
                    //line endings
                    samp_config = samp_config.replaceAll("\n|\r\n", System.getProperty("line.separator"));
                    file_put_contents(aliasConfig, samp_config, "o");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "CommandHelper: Could not write sample config file");
                }
            }
            
            if(!mainFile.exists()){
                mainFile.getParentFile().mkdirs();
                mainFile.createNewFile();
                try{
                    String samp_main = getStringResource(AliasCore.class.getResourceAsStream("/samp_main.txt"));
                    samp_main = samp_main.replaceAll("\n|\r\n", System.getProperty("line.separator"));
                    file_put_contents(mainFile, samp_main, "o");
                } catch(Exception e){
                    logger.log(Level.WARNING, "CommandHelper: Could not write sample main file");
                }
            }

            Preferences prefs = Static.getPreferences();
            prefs.init(prefFile);
            
            //Run the main file once
            try{
                Env main_env = new Env();
                main_env.SetCommandSender(null);
                String main = file_get_contents(mainFile.getAbsolutePath());
                MScriptCompiler.execute(MScriptCompiler.compile(MScriptCompiler.lex(main, mainFile)), main_env, new MScriptComplete() {

                    public void done(String output) {
                        logger.log(Level.INFO, TermColors.YELLOW + "[CommandHelper]: Main file processed" + TermColors.reset());
                    }
                }, null);
            } catch(ConfigCompileException e){
                ConfigRuntimeException.DoReport(e, "Main file could not be compiled, due to a compile error.", null);
                is_loaded = false;
            }
            
            String alias_config = file_get_contents(aliasConfig.getAbsolutePath()); //get the file again
            //config = new AliasConfig(alias_config, null, perms);
            scripts = MScriptCompiler.preprocess(MScriptCompiler.lex(alias_config, aliasConfig), new Env());
            for (Script s : scripts) {
                try {
                    s.compile();
                    s.checkAmbiguous((ArrayList<Script>) scripts);
                } catch (ConfigCompileException e) {
                    ConfigRuntimeException.DoReport(e, "Compile error in script. Compilation will attempt to continue, however.", player);
                    is_loaded = false;
                }
            }
            int errors = 0;
            for (Script s : scripts) {
                if (s.compilerError) {
                    errors++;
                }
            }
            if (errors > 0) {
                System.out.println("[CommandHelper]: " + (scripts.size() - errors) + " alias(es) defined, " + TermColors.RED + "with " + errors + " aliases with compile errors." + TermColors.reset());
                is_loaded = false;
            } else {
                System.out.println("[CommandHelper]: " + scripts.size() + " alias(es) defined.");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "[CommandHelper]: Path to config file is not correct/accessable. Please"
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

    public void removePlayerReference(MCCommandSender p) {
        //If they're not a player, oh well.
        if (p instanceof MCPlayer) {
            echoCommand.remove(((MCPlayer) p).getName());
        }
    }

    public void addPlayerReference(MCCommandSender p) {
        if (p instanceof MCPlayer) {
            echoCommand.add(((MCPlayer) p).getName());
        }
    }
    
    public boolean hasPlayerReference(MCCommandSender p){
        if(p instanceof MCPlayer){
            return echoCommand.contains(((MCPlayer)p).getName());
        } else {
            return false;
        }
    }
}
