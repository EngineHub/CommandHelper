package com.laytonsmith.abstraction.events;

import java.util.List;

import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockFace;

public interface MCBlockPistonEvent extends MCBlockEvent {

	/**
	 * Get the direction in which the pushed or pulled blocks are moved.
	 * @return The direction.
	 */
	MCBlockFace getDirection();

	/**
	 * Get all blocks that will be moved by the extending or retracting piston.
	 * @return A {@link List} containing all pushed or pulled blocks.
	 */
	List<MCBlock> getAffectedBlocks();

	boolean isSticky();

	boolean isCancelled();

	void setCancelled(boolean cancelled);
}
