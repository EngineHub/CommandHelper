package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCPlugin;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlugin;
import com.laytonsmith.abstraction.events.MCConsoleCommandEvent;
import com.laytonsmith.abstraction.events.MCPluginDisableEvent;
import com.laytonsmith.abstraction.events.MCPluginEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;
import com.laytonsmith.annotations.abstraction;

import java.net.InetAddress;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 *
 * @author Layton
 */
public class BukkitServerEvents {

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCConsoleCommandEvent implements MCConsoleCommandEvent {

		ServerCommandEvent sce;
		boolean isCancelled = false;

		public BukkitMCConsoleCommandEvent(ServerCommandEvent sce){
			this.sce = sce;
		}

		public Object _GetObject() {
			return sce;
		}

		public String getCommand() {
			return sce.getCommand();
		}

		public void setCommand(String command) {
			sce.setCommand(command);
		}
	
		public boolean isRemote() {
			return (sce instanceof RemoteServerCommandEvent);
		}
	
		public void cancel() {
			sce.setCommand("commandhelper null");
			isCancelled = true;
		}
	
		public boolean isCancelled() {
			return isCancelled;
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCServerPingEvent implements MCServerPingEvent {
	
		ServerListPingEvent slp;

		public BukkitMCServerPingEvent(ServerListPingEvent event) {
			slp = event;
		}

		public Object _GetObject() {
			return slp;
		}

		public InetAddress getAddress() {
			return slp.getAddress();
		}
	
		public int getMaxPlayers() {
			return slp.getMaxPlayers();
		}
	
		public String getMOTD() {
			return slp.getMotd();
		}
	
		public int getNumPlayers() {
			return slp.getNumPlayers();
		}
	
		public void setMaxPlayers(int max) {
			slp.setMaxPlayers(max);
		}
	
		public void setMOTD(String motd) {
			slp.setMotd(motd);
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static abstract class BukkitMCPluginEvent implements MCPluginEvent {

		PluginEvent pe;

		public BukkitMCPluginEvent(PluginEvent event) {
			this.pe = event;
		}

		public Object _GetObject() {
			return pe;
		}

		public MCPlugin getPlugin() {
			return new BukkitMCPlugin(pe.getPlugin());
		}
	}

	@abstraction(type=Implementation.Type.BUKKIT)
	public static class BukkitMCPluginDisableEvent extends BukkitMCPluginEvent implements MCPluginDisableEvent {

		PluginDisableEvent pde;

		public BukkitMCPluginDisableEvent(PluginDisableEvent event) {
			super(event);
			this.pde = event;
		}

		@Override
		public Object _GetObject() {
			return pde;
		}
	}
}