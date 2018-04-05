package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBrewingStand;

public interface MCBrewerInventory extends MCInventory {
	MCItemStack getFuel();
	MCItemStack getIngredient();
	void setFuel(MCItemStack stack);
	void setIngredient(MCItemStack stack);
	@Override
	MCBrewingStand getHolder();
}
