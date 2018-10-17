package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockPhysicsEvent extends BindableEvent {

	MCMaterial getChangedType();

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancel);

}
