package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockState;

public interface MCBlockStateMeta extends MCItemMeta {

	boolean hasBlockState();

	MCBlockState getBlockState();

	MCBlockState getBlockState(boolean copy);

	void setBlockState(MCBlockState state);

}
