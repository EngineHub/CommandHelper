package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCOfflinePlayer;

public interface MCSkull extends MCBlockState {

	/**
	 * Gets the player which owns the skull.
	 * @return The player or {@code null} if the skull does not have an owner.
	 */
	MCOfflinePlayer getOwningPlayer();

	/**
	 * Gets whether the skull has an owner.
	 * @return {@code true} if the skull has an owner, {@code false} otherwise.
	 */
	boolean hasOwner();

	/**
	 * Sets the player which owns the skull.
	 * @param player - The new skull owner or {@code null} to clear the current owner.
	 */
	void setOwningPlayer(MCOfflinePlayer player);
}
