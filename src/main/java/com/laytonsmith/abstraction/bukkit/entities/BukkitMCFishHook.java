package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCFishHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;

public class BukkitMCFishHook extends BukkitMCProjectile implements MCFishHook {

	FishHook f;

	public BukkitMCFishHook(Entity e) {
		super(e);
		f = (FishHook) e;
	}

	@Override
	public double getBiteChance() {
		// only works pre-1.9
		return f.getBiteChance();
	}

	@Override
	public void setBiteChance(double chance) {
		// only works pre-1.9
		f.setBiteChance(chance);
	}

}
