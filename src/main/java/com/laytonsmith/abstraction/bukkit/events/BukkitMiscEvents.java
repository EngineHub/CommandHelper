package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommand;
import com.laytonsmith.abstraction.bukkit.BukkitMCConsoleCommandSender;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.events.MCPluginIncomingMessageEvent;
import com.laytonsmith.abstraction.events.MCCommandTabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Layton
 */
public class BukkitMiscEvents {

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
	
	public static class BukkitMCCommandTabCompleteEvent implements MCCommandTabCompleteEvent {

		List<String> comp;
		CommandSender sender;
		Command cmd;
		String alias;
		String[] args;
		public BukkitMCCommandTabCompleteEvent(CommandSender sender, Command cmd, String alias, String[] args) {
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
			if (sender instanceof Player) {
				return new BukkitMCPlayer((Player) sender);
			} else if (sender instanceof ConsoleCommandSender) {
				// There is an open PR that will make this possible
				return new BukkitMCConsoleCommandSender((ConsoleCommandSender) sender);
			} else {
				return null;
			}
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