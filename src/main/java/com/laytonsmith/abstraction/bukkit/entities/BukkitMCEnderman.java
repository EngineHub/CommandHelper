package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Enderman;
import org.bukkit.material.MaterialData;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.bukkit.BukkitMCLivingEntity;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import com.laytonsmith.abstraction.entities.MCEnderman;

public class BukkitMCEnderman extends BukkitMCLivingEntity implements
		MCEnderman {
	Enderman e;
	public BukkitMCEnderman(Enderman ent) {
		super(ent);
		e = ent;
	}

	public MCMaterialData getCarriedMaterial() {
		return new BukkitMCMaterialData(e.getCarriedMaterial());
	}
	
	public int getCarriedType() {
		return e.getCarriedMaterial().getItemTypeId();
	}
	
	public byte getCarriedData() {
		return e.getCarriedMaterial().getData();
	}

	public void setCarriedMaterial(MCMaterialData held) {
		e.setCarriedMaterial((MaterialData) held.getHandle());
	}
	
	public void setCarriedMaterial(int type, byte data) {
		e.setCarriedMaterial(new MaterialData(type, data));
	}
}
