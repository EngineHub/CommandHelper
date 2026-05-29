package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCZombieNautilus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ZombieNautilus;

public class BukkitMCZombieNautilus extends BukkitMCTameable implements MCZombieNautilus {

	public BukkitMCZombieNautilus(Entity zombieNautilus) {
		super(zombieNautilus);
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(((ZombieNautilus) getHandle()).getInventory());
	}
}
