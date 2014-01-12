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

		@Override
		public Object _GetObject() {
			return sce;
		}

		@Override
		public String getCommand() {
			return sce.getCommand();
		}

		@Override
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
		
		
		@Override
		public String getChannel() {
			return channel;
		}

		@Override
		public byte[] getBytes() {
			return bytes;
		}

		@Override
		public MCPlayer getPlayer() {
			return new BukkitMCPlayer(player);
		}

		@Override
		public Object _GetObject() {
			return null;
		}
	}

	public static class BukkitMCServerPingEvent implements MCServerPingEvent {
	
		ServerListPingEvent slp;
		public BukkitMCServerPingEvent(ServerListPingEvent event) {
			slp = event;
		}
		
		@Override
		public Object _GetObject() {
			return slp;
		}
	
		@Override
		public InetAddress getAddress() {
			return slp.getAddress();
		}
	
		@Override
		public int getMaxPlayers() {
			return slp.getMaxPlayers();
		}
	
		@Override
		public String getMOTD() {
			return slp.getMotd();
		}
	
		@Override
		public int getNumPlayers() {
			return slp.getNumPlayers();
		}
	
		@Override
		public void setMaxPlayers(int max) {
			slp.setMaxPlayers(max);
		}
	
		@Override
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
		
		@Override
		public Object _GetObject() {
			return comp;
		}

		@Override
		public MCCommandSender getCommandSender() {
			return sender;
		}

		@Override
		public MCCommand getCommand() {
			return new BukkitMCCommand(cmd);
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public String[] getArguments() {
			return args;
		}

		@Override
		public List<String> getCompletions() {
			return comp;
		}
	}
}
