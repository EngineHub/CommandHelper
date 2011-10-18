/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.aliasengine.events;

import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 *
 * @author Layton
 */
public class CPlayerListener extends PlayerListener{
    
    @Override
    public void onPlayerLogin(PlayerLoginEvent e){
        EventHandler.TriggerListener(Type.PLAYER_LOGIN, "player_login", e);
    }
}
