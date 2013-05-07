package com.laytonsmith.abstraction.bukkit.pluginmessages;

import com.laytonsmith.abstraction.AbstractionUtils;
import com.laytonsmith.abstraction.pluginmessages.MCMessenger;
import com.laytonsmith.abstraction.pluginmessages.MCPluginMessageListenerRegistration;
import com.laytonsmith.annotations.WrappedItem;
import com.laytonsmith.commandhelper.CommandHelperMessageListener;
import com.laytonsmith.commandhelper.CommandHelperPlugin;
import java.util.Set;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class BukkitMCMessenger implements MCMessenger {
	
	@WrappedItem Messenger messenger;
	
	public MCPluginMessageListenerRegistration registerIncomingPluginChannel(
			String channel) {
		PluginMessageListenerRegistration reg;
		
		reg = messenger.registerIncomingPluginChannel(
			CommandHelperPlugin.self, channel, 
			CommandHelperMessageListener.getInstance());

		return AbstractionUtils.wrap(reg);
	}
	
	public boolean isIncomingChannelRegistered(String channel) {
		return messenger.isIncomingChannelRegistered(CommandHelperPlugin.self, channel);
	}
	
	public void unregisterIncomingPluginChannel(String channel) {
		messenger.unregisterIncomingPluginChannel(
			CommandHelperPlugin.self, channel, 
			CommandHelperMessageListener.getInstance());
	}
	
	public Set<String> getIncomingChannels() {
		return messenger.getIncomingChannels(CommandHelperPlugin.self);
	}

	public Messenger getHandle() {
		return messenger;
	}
}
