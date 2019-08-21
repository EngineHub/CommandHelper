package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.entities.MCEnderman;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;

public class BukkitMCEnderman extends BukkitMCLivingEntity implements MCEnderman {

	Enderman e;

	public BukkitMCEnderman(Entity ent) {
		super(ent);
		e = (Enderman) ent;
	}

	@Override
	public MCBlockData getCarriedMaterial() {
		return new BukkitMCBlockData(e.getCarriedBlock());
	}

	@Override
	public void setCarriedMaterial(MCBlockData held) {
		e.setCarriedBlock((BlockData) held.getHandle());
	}
}
