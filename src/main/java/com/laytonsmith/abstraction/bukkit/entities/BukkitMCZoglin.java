package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCZoglin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zoglin;

public class BukkitMCZoglin extends BukkitMCAgeable implements MCZoglin {

	public BukkitMCZoglin(Entity zombie) {
		super(zombie);
	}

	@Override
	public Zoglin getHandle() {
		return (Zoglin) super.getHandle();
	}
}
