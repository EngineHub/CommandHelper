/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author Layton
 */
public class BukkitPlayerListener extends PlayerListener{
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent e){
        EventUtils.TriggerListener(BukkitConvertor.GetGenericType(Type.PLAYER_JOIN), "player_join", e);
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent e){
        EventUtils.TriggerListener(BukkitConvertor.GetGenericType(Type.PLAYER_INTERACT), "player_interact", e);
    }  

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EventUtils.TriggerListener(BukkitConvertor.GetGenericType(Type.PLAYER_RESPAWN), "player_spawn", event);
    }
    

}
