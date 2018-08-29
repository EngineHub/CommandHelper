package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

public interface MCEntityChangeBlockEvent extends BindableEvent {

	MCEntity getEntity();

	MCBlock getBlock();

	MCMaterial getTo();

	MCBlockData getBlockData();

	boolean isCancelled();

	void setCancelled(boolean cancel);
}
