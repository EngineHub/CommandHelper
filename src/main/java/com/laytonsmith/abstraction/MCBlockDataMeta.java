package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;

public interface MCBlockDataMeta extends MCItemMeta {
	MCBlockData getBlockData(MCMaterial material);
	boolean hasBlockData();
	void setBlockData(MCBlockData blockData);
}
