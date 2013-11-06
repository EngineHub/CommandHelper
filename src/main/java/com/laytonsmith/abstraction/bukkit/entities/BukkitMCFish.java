package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import org.bukkit.entity.Fish;

import com.laytonsmith.abstraction.entities.MCFish;

public class BukkitMCFish extends BukkitMCProjectile implements MCFish {

	public BukkitMCFish(Fish fish) {
		super(fish);
	}

	public BukkitMCFish(AbstractionObject ao) {
		this((Fish) ao.getHandle());
	}

	@Override
	public Fish getHandle() {
		return (Fish) metadatable;
	}

	public double getBiteChance() {
		return getHandle().getBiteChance();
	}

	public void setBiteChance(double chance) {
		getHandle().setBiteChance(chance);
	}
}