package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItem;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCPlayerDropItemEvent extends MCPlayerEvent {
	MCItem getItemDrop();
	void setItemStack(MCItemStack stack);
	boolean isCancelled();
	void setCancelled(boolean cancelled);
}
