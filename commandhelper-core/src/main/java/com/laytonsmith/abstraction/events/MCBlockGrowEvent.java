package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;

/**
 *
 * @author Hekta
 */
public interface MCBlockGrowEvent extends MCBlockEvent {

	@Override
	public MCBlock getBlock();

	public MCBlockState getNewState();
}
