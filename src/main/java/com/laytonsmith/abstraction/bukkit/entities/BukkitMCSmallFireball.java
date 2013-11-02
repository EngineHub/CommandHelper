package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.SmallFireball;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSmallFireball;

/**
 *
 * @author Hekta
 */
public class BukkitMCSmallFireball extends BukkitMCFireball implements MCSmallFireball {

	public BukkitMCSmallFireball(SmallFireball fireball) {
		super(fireball);
	}

	public BukkitMCSmallFireball(AbstractionObject ao) {
		this((SmallFireball) ao.getHandle());
	}

	@Override
	public SmallFireball getHandle() {
		return (SmallFireball) metadatable;
	}
}