package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("InventoryType")
public enum MCInventoryType {
	BREWING,
	CHEST,
	CRAFTING(false), // bukkit doesn't allow opening
	CREATIVE(false), // bukkit doesn't allow opening
	DISPENSER,
	DROPPER,
	ENCHANTING(false), // non-functional
	ENDER_CHEST,
	FURNACE,
	HOPPER,
	MERCHANT(false), // doesn't open
	PLAYER,
	WORKBENCH,
	ANVIL,
	BEACON,
	SHULKER_BOX;

	// Whether or not this inventory type can be created and used virtually
	private final boolean canVirtualize;

	MCInventoryType() {
		this.canVirtualize = true;
	}

	MCInventoryType(boolean virtual) {
		this.canVirtualize = virtual;
	}

	public boolean canVirtualize() {
		return this.canVirtualize;
	}
}
