package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCWarden;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Warden;

public class BukkitMCWarden extends BukkitMCLivingEntity implements MCWarden {

	Warden w;

	public BukkitMCWarden(Entity entity) {
		super(entity);
		this.w = (Warden) entity;
	}

	@Override
	public Warden getHandle() {
		return this.w;
	}
}
