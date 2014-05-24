package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import java.util.Collection;

public interface MCChatTabCompleteEvent extends BindableEvent {
	public MCPlayer getPlayer();
	public String getChatMessage();
	public String getLastToken();
	public Collection<String> getTabCompletions();
}
