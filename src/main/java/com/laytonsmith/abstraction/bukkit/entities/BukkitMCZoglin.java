package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCZoglin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zoglin;

public class BukkitMCZoglin extends BukkitMCLivingEntity implements MCZoglin {

	public BukkitMCZoglin(Entity zombie) {
		super(zombie);
	}

	public BukkitMCZoglin(AbstractionObject ao) {
		this((Zoglin) ao.getHandle());
	}

	@Override
	public Zoglin getHandle() {
		return (Zoglin) super.getHandle();
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
