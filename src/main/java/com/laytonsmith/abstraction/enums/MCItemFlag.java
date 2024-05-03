package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

/**
 * A MCItemFlag can hide some Attributes from MCItemStacks, through MCItemMeta.
 */
@MEnum("com.commandhelper.ItemFlag")
public enum MCItemFlag {
	/**
	 * Hides enchantments
	 */
	HIDE_ENCHANTS,
	/**
	 * Hides Attributes like Damage
	 */
	HIDE_ATTRIBUTES,
	/**
	 * Hides unbreakable state
	 */
	HIDE_UNBREAKABLE,
	/**
	 * Hides what the ItemStack can break/destroy
	 */
	HIDE_DESTROYS,
	/**
	 * Hides where this ItemStack can be built/placed on
	 */
	HIDE_PLACED_ON,
	/**
	 * Hides potion effects (and additional tooltips)
	 * Deprecated for HIDE_ADDITIONAL_TOOLTIP
	 */
	HIDE_POTION_EFFECTS,
	/**
	 * Hides any additional tooltips (like HIDE_POTION_EFFECTS)
	 */
	HIDE_ADDITIONAL_TOOLTIP,
	/**
	 * Hide dyes from coloured leather armour
	 */
	HIDE_DYE,
	/**
	 * Hides armor trim
	 */
	HIDE_ARMOR_TRIM,
	/**
	 * Hides enchantments on enchanted books
	 */
	HIDE_STORED_ENCHANTS
}
