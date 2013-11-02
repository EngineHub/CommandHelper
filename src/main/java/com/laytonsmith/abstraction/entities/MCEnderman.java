package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCMaterialData;

public interface MCEnderman extends MCLivingEntity {

	public MCMaterialData getCarriedMaterial();
	public void setCarriedMaterial(MCMaterialData held);
}