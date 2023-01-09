package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCTadpole;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tadpole;

public class BukkitMCTadpole extends BukkitMCLivingEntity implements MCTadpole {

	Tadpole t;

	public BukkitMCTadpole(Entity entity) {
		super(entity);
		this.t = (Tadpole) entity;
	}

	@Override
	public Tadpole getHandle() {
		return t;
	}
}
