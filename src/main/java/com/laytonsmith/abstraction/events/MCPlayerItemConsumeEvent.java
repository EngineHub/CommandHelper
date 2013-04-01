package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 * 
 * @author jb_aero
 */
public interface MCPlayerItemConsumeEvent extends BindableEvent {
	public MCItemStack getItem();
	public void setItem(MCItemStack item);
}
