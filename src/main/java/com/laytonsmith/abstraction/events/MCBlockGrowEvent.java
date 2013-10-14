package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;

/**
 *
 * @author Hekta
 */
public interface MCBlockGrowEvent extends MCBlockEvent {

	public MCBlock getBlock();

	public MCBlockState getNewState();
}
