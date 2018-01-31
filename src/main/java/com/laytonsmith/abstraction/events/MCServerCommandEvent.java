package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.events.BindableEvent;

public interface MCServerCommandEvent extends BindableEvent {
	String getCommand();
	void setCommand(String command);
	MCCommandSender getCommandSender();
}
