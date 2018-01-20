package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCMaterialData;

public interface MCEnderman extends MCLivingEntity {
	MCMaterialData getCarriedMaterial();
	void setCarriedMaterial(MCMaterialData held);
}
