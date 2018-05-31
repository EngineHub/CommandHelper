package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCExplosiveMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.ExplosiveMinecart;

public class BukkitMCExplosiveMinecart extends BukkitMCMinecart implements MCExplosiveMinecart {

	ExplosiveMinecart em;

	public BukkitMCExplosiveMinecart(Entity e) {
		super(e);
		this.em = (ExplosiveMinecart) e;
	}

}
