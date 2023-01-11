package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("com.commandhelper.InventoryType")
public enum MCInventoryType {
	BREWING,
	CHEST,
	COMPOSTER(false), // no inventory view
	CRAFTING(false),
	CREATIVE(false),
	DISPENSER,
	DROPPER,
	ENCHANTING(false), // non-functional when virtualized
	ENDER_CHEST,
	FURNACE,
	HOPPER,
	MERCHANT(false),
	PLAYER,
	WORKBENCH,
	ANVIL,
	SMITHING,
	BEACON(false),
	SHULKER_BOX,
	BARREL,
	BLAST_FURNACE,
	LECTERN(false),
	SMOKER,
	LOOM(false),
	CARTOGRAPHY(false),
	GRINDSTONE(false),
	STONECUTTER(false),
	CHISELED_BOOKSHELF(false);

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
