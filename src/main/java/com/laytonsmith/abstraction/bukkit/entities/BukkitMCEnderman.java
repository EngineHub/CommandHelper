package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import com.laytonsmith.abstraction.entities.MCEnderman;
import org.bukkit.entity.Enderman;
import org.bukkit.material.MaterialData;

public class BukkitMCEnderman extends BukkitMCCreature implements MCEnderman {

	public BukkitMCEnderman(Enderman enderman) {
		super(enderman);
	}

	public BukkitMCEnderman(AbstractionObject ao) {
		this((Enderman) ao.getHandle());
	}

	@Override
	public Enderman getHandle() {
		return (Enderman) metadatable;
	}

	public MCMaterialData getCarriedMaterial() {
		return new BukkitMCMaterialData(getHandle().getCarriedMaterial());
	}

	public void setCarriedMaterial(MCMaterialData held) {
		getHandle().setCarriedMaterial((MaterialData) held.getHandle());
	}
}