/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import java.net.InetAddress;

import com.laytonsmith.abstraction.events.MCConsoleCommandEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;

import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

/**
 *
 * @author Layton
 */
public class BukkitMiscEvents {
	public static class BukkitMCConsoleCommandEvent implements MCConsoleCommandEvent {
		ServerCommandEvent sce;
		
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
		
		
		
	}

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
}
