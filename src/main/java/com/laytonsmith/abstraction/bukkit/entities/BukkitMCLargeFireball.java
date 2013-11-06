package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.LargeFireball;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCLargeFireball;

/**
 *
 * @author Hekta
 */
public class BukkitMCLargeFireball extends BukkitMCFireball implements MCLargeFireball {

	public BukkitMCLargeFireball(LargeFireball fireball) {
		super(fireball);
	}

	public BukkitMCLargeFireball(AbstractionObject ao) {
		this((LargeFireball) ao.getHandle());
	}

	@Override
	public LargeFireball getHandle() {
		return (LargeFireball) metadatable;
	}
}