package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.events.MCBroadcastMessageEvent;
import com.laytonsmith.abstraction.events.MCServerCommandEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;
import com.laytonsmith.annotations.abstraction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BukkitServerEvents {

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCServerCommandEvent implements MCServerCommandEvent {

		ServerCommandEvent sce;
		MCCommandSender sender;

		public BukkitMCServerCommandEvent(ServerCommandEvent sce, MCCommandSender sender) {
			this.sce = sce;
			this.sender = sender;
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

		@Override
		public MCCommandSender getCommandSender() {
			return sender;
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCServerPingEvent implements MCServerPingEvent {

		private final ServerListPingEvent slp;

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

		@Override
		public Set<MCPlayer> getPlayers() {
			Set<MCPlayer> players = new HashSet<>();
			try {
				Iterator<Player> iterator = slp.iterator();
				while(iterator.hasNext()) {
					players.add(new BukkitMCPlayer(iterator.next()));
				}
			} catch (UnsupportedOperationException ex) {
				// not implemented, ignore
			}
			return players;
		}

		@Override
		public void setPlayers(Collection<MCPlayer> players) {
			Set<Player> ps = new HashSet<>();
			for(MCPlayer player : players) {
				ps.add((Player) player.getHandle());
			}
			try {
				Iterator<Player> iterator = slp.iterator();
				while(iterator.hasNext()) {
					if(!ps.contains(iterator.next())) {
						iterator.remove();
					}
				}
			} catch (UnsupportedOperationException ex) {
				// not implemented, ignore
			}
		}
	}

	@abstraction(type = Implementation.Type.BUKKIT)
	public static class BukkitMCBroadcastMessageEvent implements MCBroadcastMessageEvent {

		private final BroadcastMessageEvent bme;

		public BukkitMCBroadcastMessageEvent(Event event) {
			this.bme = (BroadcastMessageEvent) event;
		}

		// This constructor is required by EventBuilder.instantiate(...).
		public BukkitMCBroadcastMessageEvent(BroadcastMessageEvent event) {
			this.bme = event;
		}

		public static BroadcastMessageEvent _instantiate(String message, Set<MCCommandSender> recipients) {
			Set<CommandSender> bukkitRecipients = new HashSet<>(recipients.size());
			for(MCCommandSender commandSender : recipients) {
				if(commandSender.getHandle() instanceof CommandSender) {
					bukkitRecipients.add((CommandSender) commandSender.getHandle());
				}
			}
			return new BroadcastMessageEvent(message, bukkitRecipients);
		}

		@Override
		public Object _GetObject() {
			return this.bme;
		}

		@Override
		public void cancel(boolean state) {
			this.bme.setCancelled(state);
		}

		@Override
		public String getMessage() {
			return this.bme.getMessage();
		}

		@Override
		public void setMessage(String message) {
			this.bme.setMessage(message);
		}

		/**
		 * Gets the recipients of this message.
		 * Modifications made to the returned set do not have influence on the event itself.
		 * This set can contain command senders like players, command blocks, command block functions and console.
		 * To only receive the player recipients, use the {@link #getPlayerRecipients()} method.
		 * @return The recipients of this message.
		 */
		@Override
		public Set<MCCommandSender> getRecipients() {
			Set<MCCommandSender> ret = new HashSet<MCCommandSender>();
			for(CommandSender sender : this.bme.getRecipients()) {
				ret.add(new BukkitMCCommandSender(sender));
			}
			return ret;
		}

		/**
		 * Gets the player recipients of this message.
		 * Modifications made to the returned set do not have influence on the event itself.
		 * This set can contain command senders like players, command blocks, command block functions and console.
		 * To receive player and non-player recipients, use the {@link #getRecipients()} method.
		 * @return The player recipients of this message.
		 */
		@Override
		public Set<MCPlayer> getPlayerRecipients() {
			Set<MCPlayer> ret = new HashSet<MCPlayer>();
			for(CommandSender sender : this.bme.getRecipients()) {
				if(sender instanceof Player) {
					ret.add(new BukkitMCPlayer((Player) sender));
				}
			}
			return ret;
		}

		@Override
		public boolean isCancelled() {
			return this.bme.isCancelled();
		}

	}
}
