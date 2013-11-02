package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.WitherSkull;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCWitherSkull;

/**
 *
 * @author Hekta
 */
public class BukkitMCWitherSkull extends BukkitMCFireball implements MCWitherSkull {

	public BukkitMCWitherSkull(WitherSkull skull) {
		super(skull);
	}

	public BukkitMCWitherSkull(AbstractionObject ao) {
		this((WitherSkull) ao.getHandle());
	}

	@Override
	public WitherSkull getHandle() {
		return (WitherSkull) metadatable;
	}
}