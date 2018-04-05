package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCInventoryHolder;

public interface MCDropper extends MCBlockState, MCInventoryHolder {
	void drop();
}
