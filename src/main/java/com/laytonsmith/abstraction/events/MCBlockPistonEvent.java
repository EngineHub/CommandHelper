package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlockFace;

public interface MCBlockPistonEvent extends MCBlockEvent {
	MCBlockFace getDirection();
	boolean isSticky();
	boolean isCancelled();
	void setCancelled(boolean cancelled);
}
