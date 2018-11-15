package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.InventoryType")
public enum MCInventoryType {
	BREWING,
	CHEST,
	CRAFTING(false),
	CREATIVE(false),
	DISPENSER,
	DROPPER,
	ENCHANTING(false), // non-functional
	ENDER_CHEST,
	FURNACE,
	HOPPER,
	MERCHANT(false),
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
