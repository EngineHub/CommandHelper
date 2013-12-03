package com.laytonsmith.commandhelper;

import com.laytonsmith.abstraction.bukkit.events.BukkitMiscEvents.BukkitMCPluginIncomingMessageEvent;
import com.laytonsmith.core.events.Driver;
import com.laytonsmith.core.events.EventUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class CommandHelperMessageListener implements PluginMessageListener {
	private static final CommandHelperMessageListener instance = new CommandHelperMessageListener();

	public static CommandHelperMessageListener getInstance() {
		return instance;
	}

	private CommandHelperMessageListener() {
	}

	@Override
	public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
		BukkitMCPluginIncomingMessageEvent event = new BukkitMCPluginIncomingMessageEvent(player, channel, bytes);
		EventUtils.TriggerExternal(event);
		EventUtils.TriggerListener(Driver.PLUGIN_MESSAGE_RECEIVED, "plugin_message_received", event);
	}
}
