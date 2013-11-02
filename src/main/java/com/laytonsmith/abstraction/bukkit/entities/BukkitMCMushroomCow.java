package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.MushroomCow;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCMushroomCow;

/**
 *
 * @author Hekta
 */
public class BukkitMCMushroomCow extends BukkitMCCow implements MCMushroomCow {

	public BukkitMCMushroomCow(MushroomCow cow) {
		super(cow);
	}

	public BukkitMCMushroomCow(AbstractionObject ao) {
		this((MushroomCow) ao.getHandle());
	}

	@Override
	public MushroomCow getHandle() {
		return (MushroomCow) metadatable;
	}
}