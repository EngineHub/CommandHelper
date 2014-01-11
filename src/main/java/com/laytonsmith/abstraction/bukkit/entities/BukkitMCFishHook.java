package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCProjectile;
import com.laytonsmith.abstraction.entities.MCFishHook;
import org.bukkit.entity.Fish;

public class BukkitMCFishHook extends BukkitMCProjectile implements MCFishHook {

	Fish f;
	public BukkitMCFishHook(Fish e) {
		super(e);
		f = e;
	}

	@Override
	public double getBiteChance() {
		return f.getBiteChance();
	}

	@Override
	public void setBiteChance(double chance) {
		f.setBiteChance(chance);
	}

}
