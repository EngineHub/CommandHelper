package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBeacon;

public interface MCBeaconInventory extends MCInventory {
	MCItemStack getItem();
	void setItem(MCItemStack stack);
	@Override
	MCBeacon getHolder();
}
