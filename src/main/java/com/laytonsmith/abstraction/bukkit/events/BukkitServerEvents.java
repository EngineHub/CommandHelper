package com.laytonsmith.abstraction.bukkit.events;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.events.MCServerCommandEvent;
import com.laytonsmith.abstraction.events.MCServerPingEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BukkitServerEvents {

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
			} catch(UnsupportedOperationException ex) {
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
			} catch(UnsupportedOperationException ex) {
				// not implemented, ignore
			}
		}
	}

	
}
