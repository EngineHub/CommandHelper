
package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.MCPlayer;
import com.laytonsmith.core.events.BindableEvent;

/**
 *
 * @author import
 */
public interface MCPlayerPickupItemEvent extends BindableEvent {
	public int getRemaining();
	public MCItemStack getItem();
	public void setItem(MCItemStack stack);
	public boolean isCancelled();
	public void setCancelled(boolean cancelled);
	public MCPlayer getPlayer();
}
