package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommand;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.events.MCCommandTabCompleteEvent;
import com.laytonsmith.abstraction.events.MCPluginIncomingMessageEvent;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import java.util.List;

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

	public static class BukkitMCCommandTabCompleteEvent implements MCCommandTabCompleteEvent {

		List<String> comp;
		MCCommandSender sender;
		Command cmd;
		String alias;
		String[] args;

		public BukkitMCCommandTabCompleteEvent(MCCommandSender sender, Command cmd, String alias, String[] args) {
			this.comp = null;
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

		@Override
		public void setCompletions(List<String> completions) {
			this.comp = completions;
		}
	}
}
