package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityChangeBlockEvent extends BindableEvent {

	MCEntity getEntity();

	MCBlock getBlock();

	MCMaterial getTo();

	byte getData();

	boolean isCancelled();

	void setCancelled(boolean cancel);
}
