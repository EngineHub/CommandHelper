package com.laytonsmith.abstraction.events;

import java.util.List;

import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.events.BindableEvent;

public interface MCCommandTabCompleteEvent extends BindableEvent {

	public MCCommandSender getCommandSender();
	
	public MCCommand getCommand();
	
	public String getAlias();
	
	public String[] getArguments();
	
	public List<String> getCompletions();
}
