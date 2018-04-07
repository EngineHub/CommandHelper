package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCFurnaceInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;

public interface MCFurnace extends MCBlockState, MCInventoryHolder {
	short getBurnTime();
	void setBurnTime(short burnTime);
	short getCookTime();
	void setCookTime(short cookTime);
	@Override
	MCFurnaceInventory getInventory();
}
