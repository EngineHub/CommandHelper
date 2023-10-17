package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.blocks.MCBlockData;

public interface MCBlockDisplay extends MCDisplay {

	MCBlockData getBlockData();

	void setBlockData(MCBlockData data);

}
