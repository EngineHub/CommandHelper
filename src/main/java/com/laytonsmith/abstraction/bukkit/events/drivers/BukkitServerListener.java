package com.laytonsmith.abstraction.bukkit.events.drivers;

import com.laytonsmith.abstraction.bukkit.events.BukkitServerEvents;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

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
		BukkitServerEvents.BukkitMCServerPingEvent pe = new BukkitServerEvents.BukkitMCServerPingEvent(event);
		EventUtils.TriggerExternal(pe);
		EventUtils.TriggerListener(Driver.SERVER_PING, "server_ping", pe);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPluginDisable(PluginDisableEvent event) {
		BukkitServerEvents.BukkitMCPluginDisableEvent pde = new BukkitServerEvents.BukkitMCPluginDisableEvent(event);
		EventUtils.TriggerExternal(pde);
		EventUtils.TriggerListener(Driver.PLUGIN_DISABLE, "plugin_disable", pde);
	}
}