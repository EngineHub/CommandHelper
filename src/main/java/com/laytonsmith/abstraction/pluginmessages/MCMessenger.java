/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
}
