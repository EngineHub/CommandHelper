package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * 
 */
public interface MCConsoleCommandEvent extends BindableEvent {
	
	public String getCommand();
	public void setCommand(String command);
	public boolean isRemote();
}