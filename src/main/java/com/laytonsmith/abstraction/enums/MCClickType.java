package com.laytonsmith.abstraction.enums;

import com.laytonsmith.annotations.MEnum;

@MEnum("ClickType")
public enum MCClickType {
	/**
	 * The left (or primary) mouse button.
	 */
	LEFT,
	/**
	 * Holding shift while pressing the left mouse button.
	 */
	SHIFT_LEFT,
	/**
	 * The right mouse button.
	 */
	RIGHT,
	/**
	 * Holding shift while pressing the right mouse button.
	 */
	SHIFT_RIGHT,
	/**
	 * Clicking the left mouse button on the grey area around the
	 * inventory.
	 */
	WINDOW_BORDER_LEFT,
	/**
	 * Clicking the right mouse button on the grey area around the
	 * inventory.
	 */
	WINDOW_BORDER_RIGHT,
	/**
	 * The middle mouse button, or a "scrollwheel click".
	 */
	MIDDLE,
	/**
	 * One of the number keys 1-9, correspond to slots on the hotbar.
	 */
	NUMBER_KEY,
	/**
	 * Pressing the left mouse button twice in quick succession.
	 */
	DOUBLE_CLICK,
	/**
	 * The "Drop" key (defaults to Q).
	 */
	DROP,
	/**
	 * Holding Ctrl while pressing the "Drop" key (defaults to Q).
	 */
	CONTROL_DROP,
	/**
	 * Any action done with the Creative inventory open.
	 */
	CREATIVE,
	/**
	 * A type of inventory manipulation not yet recognized by Bukkit.
	 * This is only for transitional purposes on a new Minecraft update,
	 * and should never be relied upon.
	 * <p>
	 * Any ClickType.UNKNOWN is called on a best-effort basis.
	 */
	UNKNOWN,
	;
}
