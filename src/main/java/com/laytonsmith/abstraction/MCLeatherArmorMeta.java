package com.laytonsmith.abstraction;

public interface MCLeatherArmorMeta extends MCItemMeta {

	/**
	 * Gets the color of the armor.
	 *
	 * @return the color of the armor, never null
	 */
	MCColor getColor();

	/**
	 * Sets the color of the armor.
	 *
	 * @param color the color to set or default if null.
	 */
	void setColor(MCColor color);
}
