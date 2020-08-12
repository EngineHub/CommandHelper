package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPiglin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Piglin;

public class BukkitMCPiglin extends BukkitMCLivingEntity implements MCPiglin {

	public BukkitMCPiglin(Entity ent) {
		super(ent);
	}

	public BukkitMCPiglin(AbstractionObject ao) {
		this((Piglin) ao.getHandle());
	}

	@Override
	public Piglin getHandle() {
		return (Piglin) super.getHandle();
	}

	@Override
	public boolean isBaby() {
		return getHandle().isBaby();
	}

	@Override
	public void setBaby(boolean baby) {
		getHandle().setBaby(baby);
	}
}
