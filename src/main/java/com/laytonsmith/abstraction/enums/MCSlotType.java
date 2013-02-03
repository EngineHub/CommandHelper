package com.laytonsmith.abstraction.enums;

/**
 *
 * @author jb_aero
 */
public enum MCSlotType {
	/**
	 * An armour slot in the player's inventory.
	 */
	ARMOR,
	/**
	 * A regular slot in the container or the player's inventory; anything not covered by the other enum values.
	 */
	CONTAINER,
	/**
	 * A slot in the crafting matrix, or the input slot in a furnace inventory, the potion slot in the brewing stand, or the enchanting slot.
	 */
	CRAFTING,
	/**
	 * The fuel slot in a furnace inventory, or the ingredient slot in a brewing stand inventory.
	 */
	FUEL,
	/**
	 * A pseudo-slot representing the area outside the inventory window.
	 */
	OUTSIDE,
	/**
	 * A slot in the bottom row or quickbar.
	 */
	QUICKBAR,
	/**
	 * A result slot in a furnace or crafting inventory.
	 */
	RESULT
}
