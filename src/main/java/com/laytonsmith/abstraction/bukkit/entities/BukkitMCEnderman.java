package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import com.laytonsmith.abstraction.entities.MCEnderman;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.material.MaterialData;

public class BukkitMCEnderman extends BukkitMCLivingEntity implements MCEnderman {
	Enderman e;

	public BukkitMCEnderman(Entity ent) {
		super(ent);
		e = (Enderman) ent;
	}

	@Override
	public MCMaterialData getCarriedMaterial() {
		return new BukkitMCMaterialData(e.getCarriedMaterial());
	}

	@Override
	public void setCarriedMaterial(MCMaterialData held) {
		e.setCarriedMaterial((MaterialData) held.getHandle());
	}
}
