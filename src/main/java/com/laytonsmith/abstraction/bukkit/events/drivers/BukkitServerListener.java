

package com.laytonsmith.abstraction.bukkit.events.drivers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

import com.laytonsmith.abstraction.bukkit.events.BukkitMiscEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;

/**
 *
 * @author Layton
 */
public class BukkitServerListener implements Listener{
    
	//@EventHandler(priority= EventPriority.LOWEST)
	public void onServerCommandEvent(ServerCommandEvent e){
		
	}
	
	@EventHandler(priority= EventPriority.LOWEST)
	public void onPing(ServerListPingEvent event) {
		BukkitMiscEvents.BukkitMCServerPingEvent pe = new BukkitMiscEvents.BukkitMCServerPingEvent(event);
		EventUtils.TriggerExternal(pe);
		EventUtils.TriggerListener(Driver.SERVER_PING, "server_ping", pe);
	}
}
