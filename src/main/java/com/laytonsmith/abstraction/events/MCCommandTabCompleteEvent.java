package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCommand;
import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.events.BindableEvent;
import java.util.List;

public interface MCCommandTabCompleteEvent extends BindableEvent {
	MCCommandSender getCommandSender();
	MCCommand getCommand();
	String getAlias();
	String[] getArguments();
	List<String> getCompletions();
	void setCompletions(List<String> completions);
}
