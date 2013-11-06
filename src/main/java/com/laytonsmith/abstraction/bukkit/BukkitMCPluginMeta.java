package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.bukkit.entities.BukkitMCPlayer;
import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.abstraction.MCPluginMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 *
 * @author lsmith
 */
public class BukkitMCPluginMeta extends MCPluginMeta implements PluginMessageListener {

	Plugin plugin;
	public BukkitMCPluginMeta(Plugin plugin){
		super();
		this.plugin = plugin;
	}
	
	@Override
	public void closeOutgoingChannel0(String channel) {
		Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin, channel);
	}

	@Override
	public void openOutgoingChannel0(String channel) {
		Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, channel);
	}

	@Override
	protected void sendIncomingMessage0(MCPlayer player, String channel, byte[] message) {
		Bukkit.getMessenger().dispatchIncomingMessage((Player) player.getHandle(), channel, message);
	}

	@Override
	public void closeIncomingChannel0(String channel) {
		Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin, channel);
	}

	@Override
	public void openIncomingChannel0(String channel) {
		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, channel, this);
	}

	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		triggerOnMessage(new BukkitMCPlayer(player), channel, message);
	}
	
}
