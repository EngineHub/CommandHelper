/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.abstraction.bukkit.BukkitMCPlayer;
import com.laytonsmith.abstraction.bukkit.events.BukkitMiscEvents.BukkitMCPluginIncomingMessageEvent;
import com.laytonsmith.abstraction.events.MCPluginIncomingMessageEvent;
import com.laytonsmith.core.events.BindableEvent;
import com.laytonsmith.core.events.BoundEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import java.util.SortedSet;
import java.util.TreeSet;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class CommandHelperMessageListener implements PluginMessageListener {
	private static CommandHelperMessageListener instance = new CommandHelperMessageListener();

	public static CommandHelperMessageListener getInstance() {
		return instance;
	}

	private CommandHelperMessageListener() {
	}

	public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
		BukkitMCPluginIncomingMessageEvent event = new BukkitMCPluginIncomingMessageEvent(player, channel, bytes);
		EventUtils.TriggerExternal(event);
		EventUtils.TriggerListener(Driver.PLUGIN_MESSAGE_RECEIVED, "plugin_message_received", event);
	}
}
