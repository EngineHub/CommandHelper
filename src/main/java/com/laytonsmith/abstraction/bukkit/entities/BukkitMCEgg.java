package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Egg;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEgg;

/**
 *
 * @author Hekta
 */
public class BukkitMCEgg extends BukkitMCProjectile implements MCEgg {

	public BukkitMCEgg(Egg egg) {
		super(egg);
	}

	public BukkitMCEgg(AbstractionObject ao) {
		this((Egg) ao.getHandle());
	}

	@Override
	public Egg getHandle() {
		return (Egg) metadatable;
	}
}