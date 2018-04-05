package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCBrewerInventory;
import com.laytonsmith.abstraction.MCInventoryHolder;

public interface MCBrewingStand extends MCBlockState, MCInventoryHolder {
	int getBrewingTime();
	int getFuelLevel();
	@Override
	MCBrewerInventory getInventory();
	void setBrewingTime(int brewTime);
	void setFuelLevel(int level);
}
