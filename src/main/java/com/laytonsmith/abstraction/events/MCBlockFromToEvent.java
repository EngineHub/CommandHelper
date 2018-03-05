package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.core.events.BindableEvent;

public interface MCBlockFromToEvent extends BindableEvent {

	MCBlock getBlock();

	MCBlock getToBlock();

	MCBlockFace getBlockFace();

	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
