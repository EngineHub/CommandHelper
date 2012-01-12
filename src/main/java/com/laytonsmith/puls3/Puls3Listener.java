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
package com.laytonsmith.puls3;

import com.laytonsmith.puls3.abstraction.MCChatColor;
import com.laytonsmith.puls3.abstraction.MCPlayer;
import com.laytonsmith.puls3.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.puls3.core.AliasCore;
import com.laytonsmith.puls3.core.BukkitDirtyRegisteredListener;
import com.laytonsmith.puls3.core.Env;
import com.laytonsmith.puls3.core.exceptions.ConfigCompileException;
import com.laytonsmith.puls3.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.puls3.core.InternalException;
import com.laytonsmith.puls3.core.MScriptCompiler;
import com.laytonsmith.puls3.core.Script;
import com.laytonsmith.puls3.core.Static;
import com.laytonsmith.puls3.core.User;
import com.sk89q.worldguard.bukkit.WorldGuardPlayerListener;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class Puls3Listener extends PlayerListener {

    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");
    /**
     * Sessions.
     */
    private Map<String, Puls3Session> sessions =
            new HashMap<String, Puls3Session>();
    /**
     * List of global aliases.
     */
    private AliasCore ac;
    private Puls3Plugin plugin;

    public Puls3Listener(Puls3Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load global aliases.
     */
    public void loadGlobalAliases() {
        ac = Puls3Plugin.getCore();
    }

    /**
     * Find and run aliases for a player for a given command.
     *
     * @param command
     * @return
     */
    public boolean runAlias(String command, MCPlayer player) {
        try {
            User u = new User(player, plugin.persist);
            ArrayList<String> aliases = u.getAliasesAsArray();
            ArrayList<Script> scripts = new ArrayList<Script>();
            for (String script : aliases) {
                Env env = new Env();
                env.SetPlayer(player);
                scripts.addAll(MScriptCompiler.preprocess(MScriptCompiler.lex(script, new File("Player")), env));
            }
            return Puls3Plugin.getCore().alias(command, player, scripts);
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
    public Puls3Session getSession(MCPlayer player) {
        if (sessions.containsKey(player.getName())) {
            return sessions.get(player.getName());
        } else {
            Puls3Session session = new Puls3Session(player.getName());
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
//        if((Boolean)Static.getPreferences().getPreference("debug-mode")){
//            System.out.println("CommandHelper: (>'.')> Recieved event-> " + event.getMessage() + " Is Cancelled? " + (event.isCancelled()?"Y":"N"));
//        }
        WorldGuardPlugin wgp = Static.getWorldGuardPlugin();
        //This will cancel the command if the player isn't supposed to run it in this region
        if(wgp != null){
            WorldGuardPlayerListener wgpl = new WorldGuardPlayerListener(wgp);
            wgpl.onPlayerCommandPreprocess(event);
        }
        String cmd = event.getMessage();        
        MCPlayer player = new BukkitMCPlayer(event.getPlayer());
        Static.PlayDirty();
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
                    BukkitDirtyRegisteredListener.setCancelled(event);
                }
                //System.out.println("Command Cancelled: " + cmd);
                return;
            }
        } catch (InternalException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (ConfigRuntimeException e) {
            logger.log(Level.WARNING, e.getMessage());
        } catch (Throwable e) {
            player.sendMessage(MCChatColor.RED + "Command failed with following reason: " + e.getMessage());
            //Obviously the command is registered, but it somehow failed. Cancel the event.
            event.setCancelled(true);
            e.printStackTrace();
            return;
        }
    }

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
    

}