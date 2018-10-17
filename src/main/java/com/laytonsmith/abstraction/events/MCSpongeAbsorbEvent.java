package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.core.events.BindableEvent;

import java.util.List;

public interface MCSpongeAbsorbEvent extends BindableEvent {

	List<MCBlockState> getBlocks();

	MCBlock getBlock();

	boolean isCancelled();

	void setCancelled(boolean cancel);

}
