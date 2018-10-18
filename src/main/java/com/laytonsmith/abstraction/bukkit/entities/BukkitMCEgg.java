package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCEgg;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;

public class BukkitMCEgg extends BukkitMCProjectile implements MCEgg {

	private final Egg egg;

	public BukkitMCEgg(Entity e) {
		super(e);
		this.egg = (Egg) e;
	}

}
