package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCPlayerPickupItemEvent extends MCPlayerEvent {

	int getRemaining();

	MCItem getItem();

	void setItemStack(MCItemStack stack);

	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
