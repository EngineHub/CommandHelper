package com.laytonsmith.abstraction.bukkit.pluginmessages;

import com.laytonsmith.abstraction.pluginmessages.MCPluginMessageListenerRegistration;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class BukkitMCPluginMessageListenerRegistration implements MCPluginMessageListenerRegistration {
	@WrappedItem PluginMessageListenerRegistration registration;

	public PluginMessageListenerRegistration getHandle() {
		return registration;
	}
}
