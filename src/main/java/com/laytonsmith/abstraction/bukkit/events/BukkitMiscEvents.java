package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommand;
import com.laytonsmith.abstraction.bukkit.BukkitMCConsoleCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.events.MCConsoleCommandEvent;
import com.laytonsmith.abstraction.events.MCPluginIncomingMessageEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;
import com.laytonsmith.abstraction.events.MCCommandTabCompleteEvent;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
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
	
	/*
	 * Not an actual event, but making it one.
	 */
	public static class BukkitMCPluginIncomingMessageEvent implements MCPluginIncomingMessageEvent {
		
		Player player;
		String channel;
		byte[] bytes;

		public BukkitMCPluginIncomingMessageEvent(Player player, String channel, byte[] bytes) {
			this.player = player;
			this.channel = channel;
			this.bytes = bytes;
		}
		
		
		public String getChannel() {
			return channel;
		}

		public byte[] getBytes() {
			return bytes;
		}

		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(player);
		}

		public Object _GetObject() {
			return null;
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
	
	public static class BukkitMCCommandTabCompleteEvent implements MCCommandTabCompleteEvent {

		List<String> comp;
		MCCommandSender sender;
		Command cmd;
		String alias;
		String[] args;
		public BukkitMCCommandTabCompleteEvent(MCCommandSender sender, Command cmd, String alias, String[] args) {
			this.comp = new ArrayList<String>();
			this.sender = sender;
			this.cmd = cmd;
			this.alias = alias;
			this.args = args;
		}
		
		public Object _GetObject() {
			return comp;
		}

		public MCCommandSender getCommandSender() {
			return sender;
		}

		public MCCommand getCommand() {
			return new BukkitMCCommand(cmd);
		}

		public String getAlias() {
			return alias;
		}

		public String[] getArguments() {
			return args;
		}

		public List<String> getCompletions() {
			return comp;
		}
	}
}
