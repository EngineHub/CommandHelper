package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;
import java.util.Collection;

public interface MCChatTabCompleteEvent extends BindableEvent {
	MCPlayer getPlayer();
	String getChatMessage();
	String getLastToken();
	Collection<String> getTabCompletions();
}
