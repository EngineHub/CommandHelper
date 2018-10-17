package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCLeavesDecayEvent extends BindableEvent {

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancel);

}
