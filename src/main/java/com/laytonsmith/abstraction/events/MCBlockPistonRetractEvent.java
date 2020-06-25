package com.laytonsmith.abstraction.events;

import com.laytonsmith.abstraction.MCLocation;

public interface MCBlockPistonRetractEvent extends MCBlockPistonEvent {

	/**
	 * Get the location of the block that is being pulled by the retracting piston.
	 * @return The location.
	 * @deprecated Use {@link #getAffectedBlocks()} to obtain all pulled blocks instead.
	 */
	@Deprecated
	MCLocation getRetractedLocation();
}
