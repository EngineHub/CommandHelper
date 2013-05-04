package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCProjectile;
import com.laytonsmith.abstraction.entities.MCFishHook;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.Fish;

public class BukkitMCFishHook extends BukkitMCProjectile implements MCFishHook {

	@WrappedItem Fish f;
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
