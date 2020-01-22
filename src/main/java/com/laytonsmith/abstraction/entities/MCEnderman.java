package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.blocks.MCBlockData;

public interface MCEnderman extends MCLivingEntity {

	/**
	 * Gets the data of the block that the Enderman is carrying.
	 * @return {@link MCBlockData} containing the carried block, or {@code null} if none.
	 */
	MCBlockData getCarriedMaterial();

	void setCarriedMaterial(MCBlockData held);
}
