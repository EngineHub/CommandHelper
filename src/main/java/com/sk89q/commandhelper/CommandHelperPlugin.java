// $Id$
/*
 * CommandHelper
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.commandhelper;

import com.laytonsmith.PureUtilities.Persistance;
import com.laytonsmith.aliasengine.AliasCore;
import com.laytonsmith.aliasengine.ConfigCompileException;
import com.laytonsmith.PureUtilities.Preferences;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.User;
import com.laytonsmith.aliasengine.Version;
import com.sk89q.bukkit.migration.PermissionsResolverManager;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the plugin.
 *
 * @author sk89q
 */
public class CommandHelperPlugin extends JavaPlugin {
    public static final Logger logger = Logger.getLogger("Minecraft.CommandHelper");
    private static AliasCore ac;
    public static Server myServer;
    public static Persistance persist;
    public static PermissionsResolverManager perms;
    public static Version version;
    public static Preferences prefs;
    public static CommandHelperPlugin self;
    public static WorldEditPlugin wep;
    /**
     * Listener for the plugin system.
     */
    final CommandHelperListener playerListener =
            new CommandHelperListener(this);

    final ArrayList<Player> commandRunning = new ArrayList<Player>();
    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        self = this;
        myServer = getServer();
        persist = new Persistance(new File("plugins/CommandHelper/persistance.ser"), this);
        logger.info("CommandHelper " + getDescription().getVersion() + " enabled");
        version = new Version(getDescription().getVersion());
        perms = new PermissionsResolverManager(getConfiguration(), getServer(),
                getDescription().getName(), logger);
        Plugin pwep = getServer().getPluginManager().getPlugin("WorldEdit");
        if(pwep != null && pwep.isEnabled() && pwep instanceof WorldEditPlugin){
            wep = (WorldEditPlugin)pwep;
        }
        try {
            File prefsFile = new File("plugins/CommandHelper/preferences.txt");
            Static.getPreferences().init(prefsFile);
            String script_name = (String) Static.getPreferences().getPreference("script-name");
            ac = new AliasCore(new File("plugins/CommandHelper/" + script_name), prefsFile, perms);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ConfigCompileException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Highest);
        registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal);
        
        playerListener.loadGlobalAliases();
    }

    public static AliasCore getCore(){
        return ac;
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void onDisable() {
        //free up some memory
        ac = null;
        wep = null;
    }
    
    /**
     * Register an event.
     * 
     * @param type
     * @param listener
     * @param priority
     */
    private void registerEvent(Event.Type type, Listener listener, Priority priority) {
        getServer().getPluginManager().registerEvent(type, listener, priority, this);
    }

    /**
     * Called when a command registered by this plugin is received.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (sender instanceof Player) {
                return runCommand((Player)sender, cmd.getName(), args);
        } else if(sender.isOp() && (cmd.getName().equals("reloadaliases") || cmd.getName().equals("reloadalias"))){
            try {
                if(ac.reload()){
                    System.out.println("Command Helper scripts sucessfully recompiled.");
                } else{
                    System.out.println("An error occured when trying to compile the script. Check the console for more information.");
                }
                return true;
            } catch (ConfigCompileException ex) {
                logger.log(Level.SEVERE, null, ex);
                System.out.println("An error occured when trying to compile the script. Check the console for more information.");
                return false;
            }
            
        } else {
            return false;
        }
    }
    
    /**
     * Runs commands.
     *
     * @param player
     * @param split
     * @return
     */
    private boolean runCommand(Player player, String cmd, String[] args) {
        CommandHelperSession session = playerListener.getSession(player);
        if(commandRunning.contains(player)){
            return true;
        }

        commandRunning.add(player);
        
        // Repeat command
        if (cmd.equals("repeat")) {
            if(perms.hasPermission(player.getName(), "commandhelper.repeat") ||
                    perms.hasPermission(player.getName(), "ch.repeat")){
                //Go ahead and remove them, so that they can repeat aliases. They can't get stuck in
                //an infinite loop though, because the preprocessor won't try to fire off a repeat command
                commandRunning.remove(player);
                if (session.getLastCommand() != null) {
                    player.sendMessage(ChatColor.GRAY + session.getLastCommand());
                    execCommand(player, session.getLastCommand());
                } else {
                    player.sendMessage(ChatColor.RED + "No previous command.");
                }
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You do not have permission to access the repeat command");
                commandRunning.remove(player);
                return true;
            }
    
        // Save alias
        } else if (cmd.equalsIgnoreCase("alias") || cmd.equalsIgnoreCase("commandhelper")
                /*&& player.canUseCommand("/alias")*/) {
            if(!perms.hasPermission(player.getName(), "commandhelper.useralias") && !perms.hasPermission(player.getName(), "ch.useralias")){
                player.sendMessage(ChatColor.RED + "You do not have permission to access the alias command");
                commandRunning.remove(player);
                return true;
            }
            if(args.length > 0){

                String alias = CommandHelperPlugin.joinString(args, " ");
                try {
                    User u = new User(player, persist);
                    //AliasConfig uac = new AliasConfig(alias, u, perms);
                    //MScriptCompiler.compile(MScriptCompiler.preprocess(MScriptCompiler.lex(alias)));
                    //TODO: Finish this
                    int id = u.addAlias(alias);
                    if(id > -1){
                        player.sendMessage(ChatColor.YELLOW + "Alias added with id '" + id + "'");
                    }
                } catch (/*ConfigCompile*/Exception ex) {
                    player.sendMessage(ChatColor.RED + ex.getMessage());
                }
            } else{
                //Display a help message
                player.sendMessage(ChatColor.GREEN + "Command usage: \n"
                        + ChatColor.GREEN + "/alias <alias> - adds an alias to your user defined list\n"
                        + ChatColor.GREEN + "/delalias <id> - deletes alias with id <id> from your user defined list\n"
                        + ChatColor.GREEN + "/viewalias - shows you all of your aliases");
            }

            commandRunning.remove(player);
            return true;
        //View all aliases for this user
        } else if(cmd.equalsIgnoreCase("viewalias")){
            if(!perms.hasPermission(player.getName(), "commandhelper.useralias") && !perms.hasPermission(player.getName(), "ch.useralias")){
                player.sendMessage(ChatColor.RED + "You do not have permission to access the viewalias command");
                commandRunning.remove(player);
                return true;
            }
            User u = new User(player, persist);
            player.sendMessage(u.getAllAliases());
            commandRunning.remove(player);
            return true;
        // Delete alias
        } else if (cmd.equalsIgnoreCase("delalias")) {
            if(!perms.hasPermission(player.getName(), "commandhelper.useralias") && !perms.hasPermission(player.getName(), "ch.useralias")){
                player.sendMessage(ChatColor.RED + "You do not have permission to access the delalias command");
                commandRunning.remove(player);
                return true;
            }
            User u = new User(player, persist);
            try{
                ArrayList<String> deleted = new ArrayList<String>();
                for(int i = 0; i < args.length; i++){
                    u.removeAlias(Integer.parseInt(args[i]));
                    deleted.add("#" + args[i]);
                }
                if(args.length > 1){
                    String s = ChatColor.YELLOW + "Aliases " + deleted.toString() + " were deleted";
                    player.sendMessage(s);

                } else{
                    player.sendMessage(ChatColor.YELLOW + "Alias #" + args[0] + "was deleted");
                }
            } catch(NumberFormatException e){
                player.sendMessage(ChatColor.RED + "The id must be a number");
            } catch(ArrayIndexOutOfBoundsException e){
                player.sendMessage(ChatColor.RED + "Usage: /delalias <id> <id> ...");
            }
            commandRunning.remove(player);
            return true;
    
        // Reload global aliases
        } else if (cmd.equalsIgnoreCase("reloadaliases")) {
            if(!perms.hasPermission(player.getName(), "commandhelper.reloadaliases") && !perms.hasPermission(player.getName(), "ch.reloadaliases")){
                player.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that command");
                commandRunning.remove(player);
                return true;
            }
            try {
                if(ac.reload()){
                    player.sendMessage("Command Helper scripts sucessfully recompiled.");
                } else{
                    player.sendMessage("An error occured when trying to compile the script. Check the console for more information.");
                }
                commandRunning.remove(player);
                return true;
            } catch (ConfigCompileException ex) {
                logger.log(Level.SEVERE, null, ex);
                player.sendMessage("An error occured when trying to compile the script. Check the console for more information.");
            }
        }
        commandRunning.remove(player);
        return false;
    }

    /**
     * Joins a string from an array of strings.
     *
     * @param str
     * @param delimiter
     * @return
     */
    public static String joinString(String[] str, String delimiter) {
        if (str.length == 0) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(str[0]);
        for (int i = 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
        }
        return buffer.toString();
    }
    
    /**
     * Execute a command.
     * @param player 
     *
     * @param cmd
     */
    public static void execCommand(Player player, String cmd) {
        player.chat(cmd);
    }
}
