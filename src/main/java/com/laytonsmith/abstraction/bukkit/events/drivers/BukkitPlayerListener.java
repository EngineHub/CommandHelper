/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitPlayerEvents;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

/**
 *
 * @author Layton
 */
public class BukkitPlayerListener implements Listener{
	@EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event){
        BukkitMCPlayer currentPlayer = (BukkitMCPlayer)Static.GetPlayer(event.getPlayer().getName(), Target.UNKNOWN);        
        //Apparently this happens sometimes, so prevent it
        if(!event.getFrom().equals(currentPlayer._Player().getWorld())){
            EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", new BukkitPlayerEvents.BukkitMCWorldChangedEvent(event));            
        }
    }
	
	@EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent event){
        EventUtils.TriggerListener(Driver.PLAYER_CHAT, "player_chat", new BukkitPlayerEvents.BukkitMCPlayerChatEvent(event));
    }
	
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent e){
        EventUtils.TriggerListener(Driver.PLAYER_INTERACT, "player_interact", new BukkitPlayerEvents.BukkitMCPlayerInteractEvent(e));
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent e){
        EventUtils.TriggerListener(Driver.PLAYER_JOIN, "player_join", new BukkitPlayerEvents.BukkitMCPlayerJoinEvent(e));
    }  

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e){
        EventUtils.TriggerListener(Driver.PLAYER_LOGIN, "player_login", new BukkitPlayerEvents.BukkitMCPlayerLoginEvent(e));
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerPreLogin(PlayerPreLoginEvent e){
        EventUtils.TriggerListener(Driver.PLAYER_PRELOGIN, "player_prelogin", new BukkitPlayerEvents.BukkitMCPlayerPreLoginEvent(e));
    }
    
    @EventHandler(priority= EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event){
        EventUtils.TriggerListener(Driver.PLAYER_QUIT, "player_quit", new BukkitPlayerEvents.BukkitMCPlayerQuitEvent(event));
    }
    
    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EventUtils.TriggerListener(Driver.PLAYER_SPAWN, "player_spawn", new BukkitPlayerEvents.BukkitMCPlayerRespawnEvent(event));
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event){
        if(!event.getFrom().getWorld().equals(event.getTo().getWorld())){
            EventUtils.TriggerListener(Driver.WORLD_CHANGED, "world_changed", new BukkitPlayerEvents.BukkitMCWorldChangedEvent(new PlayerChangedWorldEvent(event.getPlayer(), event.getFrom().getWorld())));
        }
    }
    

}
