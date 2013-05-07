package com.laytonsmith.abstraction.pluginmessages;

import com.laytonsmith.abstraction.AbstractionObject;
import java.util.Set;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCMessenger extends AbstractionObject {
	public MCPluginMessageListenerRegistration registerIncomingPluginChannel(String channel);

	public boolean isIncomingChannelRegistered(String channel);

	public void unregisterIncomingPluginChannel(String channel);

	public Set<String> getIncomingChannels();
}
