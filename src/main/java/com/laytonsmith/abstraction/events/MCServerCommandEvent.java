/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * 
 */
public interface MCServerCommandEvent extends BindableEvent {
	
	String getCommand();
	void setCommand(String command);
	MCCommandSender getCommandSender();
	
}
