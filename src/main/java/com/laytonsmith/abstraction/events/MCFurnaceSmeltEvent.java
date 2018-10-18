package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCItemStack;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCFurnaceSmeltEvent extends BindableEvent {

	MCItemStack getResult();

	MCItemStack getSource();

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancel);

	void setResult(MCItemStack result);

}
