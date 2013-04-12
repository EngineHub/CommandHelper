package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Fish;
import com.laytonsmith.abstraction.bukkit.BukkitMCProjectile;
import com.laytonsmith.abstraction.entities.MCFishHook;

public class BukkitMCFishHook extends BukkitMCProjectile implements MCFishHook {

	Fish f;
	public BukkitMCFishHook(Fish e) {
		super(e);
		f = e;
	}

	public double getBiteChance() {
		return f.getBiteChance();
	}

	public void setBiteChance(double chance) {
		f.setBiteChance(chance);
	}

}
