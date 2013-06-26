package com.laytonsmith.abstraction.events;

import java.util.Collection;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

public interface MCChatTabCompleteEvent extends BindableEvent {
	public MCPlayer getPlayer();
	public String getChatMessage();
	public String getLastToken();
	public Collection<String> getTabCompletions();
}
