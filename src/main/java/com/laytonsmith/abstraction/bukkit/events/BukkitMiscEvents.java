/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.events.MCConsoleCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;

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
}
