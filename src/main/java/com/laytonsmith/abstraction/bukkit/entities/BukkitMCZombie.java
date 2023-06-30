package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCZombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

public class BukkitMCZombie extends BukkitMCAgeable implements MCZombie {

	public BukkitMCZombie(Entity zombie) {
		super(zombie);
	}

	@Override
	public Zombie getHandle() {
		return (Zombie) super.asLivingEntity();
	}

	@Override
	public boolean canBreakDoors() {
		return getHandle().canBreakDoors();
	}

	@Override
	public void setCanBreakDoors(boolean canBreakDoors) {
		try {
			getHandle().setCanBreakDoors(canBreakDoors);
		} catch(NoSuchMethodError ex) {
			// probably before 1.19
		}
	}
}
