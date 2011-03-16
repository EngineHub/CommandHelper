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

import com.laytonsmith.aliasengine.AliasCore;
import com.laytonsmith.aliasengine.ConfigCompileException;
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
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point for the plugin.
 *
 * @author sk89q
 */
public class CommandHelperPlugin extends JavaPlugin {
    private static final Logger logger = Logger.getLogger("Minecraft.CommandHelper");
    private static AliasCore ac;
    public static Server myServer;
    
    /**
     * Listener for the plugin system.
     */
    private final CommandHelperListener playerListener =
            new CommandHelperListener(this);
    /**
     * Called on plugin enable.
     */
    public void onEnable() {
        myServer = getServer();
        logger.info("CommandHelper " + getDescription().getVersion() + " enabled");
        try {
            ac = new AliasCore(true, 50, 5, new java.io.File("./config.txt"));
        } catch (ConfigCompileException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.High);
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
            try {
                return runCommand((Player)sender, cmd.getName(), args);
            } catch (InsufficientArgumentsException e) {
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
    private boolean runCommand(Player player, String cmd, String[] args) throws InsufficientArgumentsException {
        CommandHelperSession session = playerListener.getSession(player);
        
        // Repeat command
        if (cmd.equals("repeat")) {
            if (session.getLastCommand() != null) {
                player.sendMessage(ChatColor.GRAY + session.getLastCommand());
                execCommand(player, session.getLastCommand());
            } else {
                player.sendMessage(ChatColor.RED + "No previous command.");
            }
            return true;
        
        // Each group
        } else if (cmd.equalsIgnoreCase("each")
                && player.isOp()) {
            checkArgs(args, 2, -1, cmd);
    
            PlayerFilter filter = SimplePlayerFilter.parse(getServer(), args[0]);
    
            // Get arguments
            String[] newSplit = new String[args.length - 1];
            System.arraycopy(args, 1, newSplit, 0, args.length - 1);
    
            // Perform command
            int affected = 0;
            for (Player pl : filter) {
                affected++;
    
                // Substitute
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("^")) {
                        newSplit[i - 1] = pl.getName();
                    }
                }
    
                execCommand(player, joinString(newSplit, " "));
            }
    
            // Tell the user if no users were matched
            if (affected == 0) {
                player.sendMessage(ChatColor.RED + "No users were matched.");
            }
    
            return true;
    
        // Perform
        } else if (cmd.equalsIgnoreCase("perform")
                && player.isOp()) {
            checkArgs(args, 2, -1, cmd);
    
            PlayerFilter filter = SimplePlayerFilter.parse(getServer(), args[0]);
    
            // Get arguments
            String[] newSplit = new String[args.length - 1];
            System.arraycopy(args, 1, newSplit, 0, args.length - 1);
    
            // Perform command
            int affected = 0;
            for (Player pl : filter) {
                affected++;
    
                // Substitute
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("^")) {
                        newSplit[i - 1] = pl.getName();
                    }
                }
    
                execCommand(pl, joinString(newSplit, " "));
            }
    
            // Tell the user if no users were matched
            if (affected == 0) {
                player.sendMessage(ChatColor.RED + "No users were matched.");
            }
    
            return true;
    
        // Save alias
        } else if (cmd.equalsIgnoreCase("alias")
                /*&& player.canUseCommand("/alias")*/) {
            checkArgs(args, 2, -1, cmd);
    
            // Get alias name
            String aliasName = args[0];
            if (aliasName.charAt(0) != '/') {
                aliasName = "/" + aliasName;
            }
    
            // Get arguments
            String[] newSplit = new String[args.length - 1];
            System.arraycopy(args, 1, newSplit, 0, args.length - 1);
    
            // Set alias
            String[] commands = new String[]{ joinString(newSplit, " ") };
            playerListener.getSession(player).setAlias(player, commands);
    
            player.sendMessage(ChatColor.YELLOW + "Alias " + aliasName + " set.");
            session.saveAliases();
            
            return true;
    
        // Delete alias
        } else if (cmd.equalsIgnoreCase("delalias")
                /*&& player.canUseCommand("/alias")*/) {
            checkArgs(args, 1, 1, cmd);
    
            // Get alias name
            String aliasName = args[0];
            if (aliasName.charAt(0) != '/') {
                aliasName = "/" + aliasName;
            }
    
            playerListener.getSession(player).removeAlias(aliasName);
    
            player.sendMessage(ChatColor.YELLOW + "Alias " + aliasName + " removed.");
            session.saveAliases();
    
            return true;
    
        // Reload global aliases
        } else if (cmd.equalsIgnoreCase("reloadaliases")
                && player.isOp()) {
            checkArgs(args, 0, 0, cmd);
    
            playerListener.loadGlobalAliases();
    
            player.sendMessage(ChatColor.YELLOW + "Aliases reloaded.");
            session.saveAliases();
    
            return true;
        }
        
        return false;
    }

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    private void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length < min) {
            throw new InsufficientArgumentsException("Minimum " + min + " arguments");
        } else if (max != -1 && args.length > max) {
            throw new InsufficientArgumentsException("Maximum " + max + " arguments");
        }
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
