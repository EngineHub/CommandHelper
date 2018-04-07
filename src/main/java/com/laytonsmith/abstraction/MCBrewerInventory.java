package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBrewingStand;

public interface MCBrewerInventory extends MCInventory {
	MCItemStack getFuel();
	MCItemStack getIngredient();
	MCItemStack getLeftBottle();
	MCItemStack getMiddleBottle();
	MCItemStack getRightBottle();
	void setFuel(MCItemStack stack);
	void setIngredient(MCItemStack stack);
	void setLeftBottle(MCItemStack stack);
	void setMiddleBottle(MCItemStack stack);
	void setRightBottle(MCItemStack stack);
	@Override
	MCBrewingStand getHolder();
}
