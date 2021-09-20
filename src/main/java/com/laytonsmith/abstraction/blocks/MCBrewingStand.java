package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCBrewerInventory;

public interface MCBrewingStand extends MCContainer {
	int getBrewingTime();
	int getFuelLevel();
	@Override
	MCBrewerInventory getInventory();
	void setBrewingTime(int brewTime);
	void setFuelLevel(int level);
}
