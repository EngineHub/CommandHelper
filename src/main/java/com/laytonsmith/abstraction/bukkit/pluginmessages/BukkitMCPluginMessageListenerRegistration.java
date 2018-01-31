package com.laytonsmith.abstraction.bukkit.pluginmessages;

import com.laytonsmith.abstraction.pluginmessages.MCPluginMessageListenerRegistration;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public class BukkitMCPluginMessageListenerRegistration implements MCPluginMessageListenerRegistration {
	PluginMessageListenerRegistration registration;

	public BukkitMCPluginMessageListenerRegistration(PluginMessageListenerRegistration registration) {
		this.registration = registration;
	}
}
