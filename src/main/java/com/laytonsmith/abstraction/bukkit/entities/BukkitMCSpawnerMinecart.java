package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCSpawnerMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.SpawnerMinecart;

public class BukkitMCSpawnerMinecart extends BukkitMCMinecart implements MCSpawnerMinecart {

	SpawnerMinecart sm;

	public BukkitMCSpawnerMinecart(Entity e) {
		super(e);
		this.sm = (SpawnerMinecart) e;
	}

}
