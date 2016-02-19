package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;

/**
 * 
 * @author jb_aero
 */
public interface MCPlayerItemConsumeEvent extends MCPlayerEvent {
	public MCItemStack getItem();
	public void setItem(MCItemStack item);
}
