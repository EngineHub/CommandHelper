package com.laytonsmith.abstraction.pluginmessages;

import java.util.Set;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCMessenger {
	public MCPluginMessageListenerRegistration registerIncomingPluginChannel(String channel);

	public boolean isIncomingChannelRegistered(String channel);

	public void unregisterIncomingPluginChannel(String channel);

	public Set<String> getIncomingChannels();
	
	public void closeAllChannels();
}
