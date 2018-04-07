package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCFurnace;

public interface MCFurnaceInventory extends MCInventory {
	MCItemStack getResult();
	MCItemStack getFuel();
	MCItemStack getSmelting();
	void setFuel(MCItemStack stack);
	void setResult(MCItemStack stack);
	void setSmelting(MCItemStack stack);
	@Override
	MCFurnace getHolder();
}
