package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCMaterialData;

public interface MCEnderman extends MCLivingEntity {
	public MCMaterialData getCarriedMaterial();
	public int getCarriedType();
	public byte getCarriedData();
	public void setCarriedMaterial(MCMaterialData held);
	public void setCarriedMaterial(int type, byte data);
}
