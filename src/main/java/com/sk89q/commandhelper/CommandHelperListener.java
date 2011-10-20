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
import com.laytonsmith.aliasengine.DirtyRegisteredListener;
import com.laytonsmith.aliasengine.exceptions.ConfigCompileException;
import com.laytonsmith.aliasengine.exceptions.ConfigRuntimeException;
import com.laytonsmith.aliasengine.InternalException;
import com.laytonsmith.aliasengine.MScriptCompiler;
import com.laytonsmith.aliasengine.Script;
import com.laytonsmith.aliasengine.Static;
import com.laytonsmith.aliasengine.User;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CommandHelperListener extends PlayerListener {

    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * Sessions.
     */
    private Map<String, CommandHelperSession> sessions =
            new HashMap<String, CommandHelperSession>();
    /**
     * List of global aliases.
     */
    private AliasCore ac;
    private CommandHelperPlugin plugin;

    public CommandHelperListener(CommandHelperPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load global aliases.
     */
    public void loadGlobalAliases() {
        ac = CommandHelperPlugin.getCore();
    }

    /**
     * Find and run aliases for a player for a given command.
     *
     * @param command
     * @return
     */
    public boolean runAlias(String command, Player player) {
        try {
            User u = new User(player, plugin.persist);
            ArrayList<String> aliases = u.getAliasesAsArray();
            ArrayList<Script> scripts = new ArrayList<Script>();
            for (String script : aliases) {
                scripts.addAll(MScriptCompiler.preprocess(MScriptCompiler.lex(script, new File("Player"))));
            }
            return CommandHelperPlugin.getCore().alias(command, player, scripts);
            //return globalAliases.get(command.toLowerCase());

        } catch (ConfigCompileException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /**
     * Get session.
     * 
     * @param player
     * @return
     */
    public CommandHelperSession getSession(Player player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        } else {
            CommandHelperSession session = new CommandHelperSession(player.getName());
            sessions.put(player.getName(), session);
            return session;
        }
    }
        

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {        
        String cmd = event.getMessage();        
        Player player = event.getPlayer();
        playDirty();
        if (cmd.equals("/.") || cmd.equals("/repeat")) {
            return;
        }
        this.getSession(player).setLastCommand(cmd);

        if (!(Boolean) Static.getPreferences().getPreference("play-dirty")) {
            if (event.isCancelled()) {
                return;
            }
        } //If we are playing dirty, ignore the cancelled flag

        try {
            if (runAlias(event.getMessage(), player)) {
                event.setCancelled(true);
                if((Boolean) Static.getPreferences().getPreference("play-dirty")){
                    //Super cancel the event
                    DirtyRegisteredListener.setCancelled(event);
                }
                //System.out.println("Command Cancelled: " + cmd);
                return;
            }
        } catch (InternalException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (ConfigRuntimeException e) {
            logger.log(Level.WARNING, e.getMessage());
        } catch (Throwable e) {
            player.sendMessage(ChatColor.RED + "Command failed with following reason: " + e.getMessage());
            //Obviously the command is registered, but it somehow failed. Cancel the event.
            event.setCancelled(true);
            e.printStackTrace();
            return;
        }
    }

    /**
     * Runs commands.
     *
     * @param player
     * @param split
     * @return
     */
//    private boolean runCommand(Player player, String split) throws InsufficientArgumentsException {
//        CommandHelperSession session = getSession(player);
//
//        if (split[0].equals("/repeat") || split[0].equals("/.")) {
//            return false;
//        }
//
//        // Catch aliases
//        session.setLastCommand(CommandHelperPlugin.joinString(split, " "));
//
//
//
////        String[] commands = getSession(player).findAlias(split[0]);
////        String[] arguments = new String[split.length - 1];
////        System.arraycopy(split, 1, arguments, 0, split.length - 1);
//
//        if (commands != null) {
//            execCommands(player, commands, arguments, false);
//            return true;
//        } else if (true /*player.canUseCommand(split[0])*/) {
//            return runAlias(CommandHelperPlugin.joinString(split, " "), player);
//        }
//
//        return false;
//    }
    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sessions.remove(player.getName());
    }

    /**
     * Execute a command.
     *
     * @param cmd
     */
    private void execScriptableCommand(Player player, String cmd) {
        if (cmd.charAt(0) != '@') {
            CommandHelperPlugin.execCommand(player, cmd);
            return;
        }

        String[] args = cmd.split(" ");

        if (args[0].equalsIgnoreCase("@read")) {
            if (args.length >= 2) {
                try {
                    String[] newArgs = new String[args.length - 1];
                    System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                    FileReader input = new FileReader(CommandHelperPlugin.joinString(newArgs, " "));
                    BufferedReader reader = new BufferedReader(input);
                    String line;
                    while ((line = reader.readLine()) != null) {
                        player.sendMessage(line);
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "@read: Could not read "
                            + args[1] + ": " + e.getMessage());
                }
            } else {
                logger.log(Level.WARNING, "@read requires 2 arguments");
            }
        } else {
            logger.log(Level.WARNING, "Unknown CommandHelper instruction: "
                    + args[0]);
        }
    }

    /**
     * Execute a command.
     *
     * @param cmd
     */
    private void execCommands(Player player, String[] commands,
            String[] args, boolean scriptable) {
        for (String cmd : commands) {
            String[] parts = cmd.split(" ");

            for (int i = 0; i < parts.length; i++) {
                if (parts[i].matches("%[0-9]+")) {
                    int n = Integer.parseInt(parts[i].substring(1)) - 1;
                    if (n < args.length && n >= 0) {
                        parts[i] = args[n];
                    } else {
                        parts[i] = "";
                    }
                }
            }

            cmd = CommandHelperPlugin.joinString(parts, " ");

            if (scriptable) {
                execScriptableCommand(player, cmd);
            } else {
                CommandHelperPlugin.execCommand(player, cmd);
            }
        }
    }

    

    /**
     * Sets up CommandHelper to play-dirty, if the user has specified as such
     */
    public void playDirty() {
        if ((Boolean) Static.getPreferences().getPreference("play-dirty")) {
            try {
                    //Set up our "proxy"
                    DirtyRegisteredListener.Repopulate();                
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(CommandHelperListener.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchFieldException ex) {
                logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            } catch (ClassCastException ex) {
                logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, "Uh oh, play dirty mode isn't working.", ex);
            }
        } //else play nice :(
    }

}