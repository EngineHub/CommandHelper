/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author Layton
 */
public class BukkitPlayerListener implements Listener{
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e){
        EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", e);
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e){
        EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "player_interact", e);
    }  

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EventUtils.TriggerListener(Driver.PLAYER_SPAWN, "player_spawn", event);
    }
    

}
