/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Layton
 */
public class CPlayerListener extends PlayerListener{
    
    @Override
    public void onPlayerJoin(PlayerJoinEvent e){
        EventHandler.TriggerListener(Type.PLAYER_JOIN, "player_join", e);
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent e){
        EventHandler.TriggerListener(Type.PLAYER_INTERACT, "player_interact", e);
    }
}
