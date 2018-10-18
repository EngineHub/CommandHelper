package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

public interface MCPlayerChannelEvent extends BindableEvent {

	String getChannel();

	MCPlayer getPlayer();

}
