package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityInteractEvent extends BindableEvent {
	public MCEntity getEntity();
	public MCBlock getBlock();
	public boolean isCancelled();
	public void setCancelled(boolean cancelled);
}