/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.BukkitConvertor;
import com.laytonsmith.aliasengine.events.EventHandler;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Layton
 */
public class BukkitPlayerListener extends PlayerListener{
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent e){
        EventHandler.TriggerListener(BukkitConvertor.GetGenericType(Type.PLAYER_JOIN), "player_join", e);
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent e){
        EventHandler.TriggerListener(BukkitConvertor.GetGenericType(Type.PLAYER_INTERACT), "player_interact", e);
    }
}
