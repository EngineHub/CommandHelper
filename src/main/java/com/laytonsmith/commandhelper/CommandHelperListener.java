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
package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.abstraction.enums.MCChatColor;
import com.laytonsmith.abstraction.events.MCPlayerCommandEvent;
import com.laytonsmith.core.AliasCore;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.Prefs;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.UserManager;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.persistence.DataSourceException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for Hey0's server mod.
 *
 * @author sk89q
 */
public class CommandHelperListener implements Listener {

    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft");

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
    public boolean runAlias(String command, MCPlayer player) throws DataSourceException {
        UserManager um = UserManager.GetUserManager(player.getName());
        List<Script> scripts = um.getAllScripts(plugin.persistenceNetwork);

        return CommandHelperPlugin.getCore().alias(command, player, scripts);
    }

    /**
     * Called when a player attempts to use a command
     *
     * @param event Relevant event details
     */
    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {     
        if(CommandHelperPlugin.self.interpreterListener
                .isInInterpreterMode(event.getPlayer().getName())){
            //They are in interpreter mode, so we want it to handle this, not everything else.
            return;
        }
        MCPlayerCommandEvent mpce = new BukkitPlayerEvents.BukkitMCPlayerCommandEvent(event);
		EventUtils.TriggerExternal(mpce);
        EventUtils.TriggerListener(Driver.PLAYER_COMMAND, "player_command", mpce);
        if(mpce.isCancelled()){
            return;
        }
        String cmd = event.getMessage();        
        MCPlayer player = new BukkitMCPlayer(event.getPlayer());
        BukkitDirtyRegisteredListener.PlayDirty();
        if (cmd.equals("/.") || cmd.equals("/repeat")) {
            return;
        }
        
        UserManager.GetUserManager(player.getName()).setLastCommand(cmd);

        if (!Prefs.PlayDirty()) {
            if (event.isCancelled()) {
                return;
            }
        } //If we are playing dirty, ignore the cancelled flag

        try {
            if (runAlias(event.getMessage(), player)) {
                event.setCancelled(true);
                if(Prefs.PlayDirty()){
                    //Super cancel the event
                    BukkitDirtyRegisteredListener.setCancelled(event);
                }
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
        }
    }

    /**
     * Called when a player leaves a server
     *
     * @param event Relevant event details
     */
    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UserManager.ClearUser(player.getName());
    }

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Static.HostnameCache(new BukkitMCPlayer(event.getPlayer()));
    }
    
    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event){
        Static.SetPlayerHost(new BukkitMCPlayer(event.getPlayer()), event.getHostname());
    }
    

}