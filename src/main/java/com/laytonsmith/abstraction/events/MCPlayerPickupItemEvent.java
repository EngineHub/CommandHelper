package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;

/**
 *
 * @author import
 */
public interface MCPlayerPickupItemEvent extends MCPlayerEvent {
	public int getRemaining();
	public MCItem getItem();
	public void setItemStack(MCItemStack stack);
	public boolean isCancelled();
	public void setCancelled(boolean cancelled);
}
