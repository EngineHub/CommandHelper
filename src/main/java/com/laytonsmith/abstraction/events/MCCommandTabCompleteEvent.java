package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

public interface MCCommandTabCompleteEvent extends BindableEvent {

	public MCCommandSender getCommandSender();
	
	public MCCommand getCommand();
	
	public String getAlias();
	
	public String[] getArguments();
	
	public List<String> getCompletions();

	public void setCompletions(List<String> completions);
}
