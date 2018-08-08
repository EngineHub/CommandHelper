package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.blocks.MCBlockData;

public interface MCEnderman extends MCLivingEntity {

	MCBlockData getCarriedMaterial();

	void setCarriedMaterial(MCBlockData held);
}
