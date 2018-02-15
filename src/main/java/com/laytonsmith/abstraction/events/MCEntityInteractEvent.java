package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityInteractEvent extends BindableEvent {
	MCEntity getEntity();
	MCBlock getBlock();
	boolean isCancelled();
	void setCancelled(boolean cancelled);
}