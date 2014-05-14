/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.laytonsmith.abstraction.events;

import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * 
 */
public interface MCConsoleCommandEvent extends BindableEvent {
	
	String getCommand();
	void setCommand(String command);
	
}
