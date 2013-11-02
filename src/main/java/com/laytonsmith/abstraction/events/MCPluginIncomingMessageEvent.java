package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.entities.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author Jason Unger <entityreborn@gmail.com>
 */
public interface MCPluginIncomingMessageEvent extends BindableEvent {
	public String getChannel();
	public byte[] getBytes();
	public MCPlayer getPlayer();
}
