
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author import
 */
public interface MCPlayerPickupItemEvent extends BindableEvent {
	public int getRemaining();
	public MCItem getItem();
	public void setItemStack(MCItemStack stack);
	public boolean isCancelled();
	public void setCancelled(boolean cancelled);
	public MCPlayer getPlayer();
}
