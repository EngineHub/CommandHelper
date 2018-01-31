package com.laytonsmith.abstraction.pluginmessages;

import java.util.Set;

public interface MCMessenger {
	MCPluginMessageListenerRegistration registerIncomingPluginChannel(String channel);
	boolean isIncomingChannelRegistered(String channel);
	void unregisterIncomingPluginChannel(String channel);
	Set<String> getIncomingChannels();
	void closeAllChannels();
}
