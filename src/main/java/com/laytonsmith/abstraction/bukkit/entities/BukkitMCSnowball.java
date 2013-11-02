package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Snowball;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSnowball;

/**
 *
 * @author Hekta
 */
public class BukkitMCSnowball extends BukkitMCProjectile implements MCSnowball {

	public BukkitMCSnowball(Snowball snowball) {
		super(snowball);
	}

	public BukkitMCSnowball(AbstractionObject ao) {
		this((Snowball) ao.getHandle());
	}

	@Override
	public Snowball getHandle() {
		return (Snowball) metadatable;
	}
}