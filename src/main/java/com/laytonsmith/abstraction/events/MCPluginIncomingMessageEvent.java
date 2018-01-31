package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPluginIncomingMessageEvent extends BindableEvent {
	String getChannel();
	byte[] getBytes();
	MCPlayer getPlayer();
}
