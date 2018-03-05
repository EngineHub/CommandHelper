package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;

public interface MCBlockGrowEvent extends MCBlockEvent {

	@Override
	MCBlock getBlock();

	MCBlockState getNewState();
}
