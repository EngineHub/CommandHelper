package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFurnaceBurnEvent extends BindableEvent {

	int getBurnTine();

	MCItemStack getFuel();

	MCBlock getBlock();

	boolean isBurning();

	boolean isCancelled();

	void setBurning(boolean burning);

	void setBurnTime(int burnTime);

	void setCancelled(boolean cancel);

}
