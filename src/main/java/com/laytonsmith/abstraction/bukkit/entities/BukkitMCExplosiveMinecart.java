package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.minecart.ExplosiveMinecart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCExplosiveMinecart;

/**
 * 
 * @author Hekta
 */
public class BukkitMCExplosiveMinecart extends BukkitMCMinecart implements MCExplosiveMinecart {

	public BukkitMCExplosiveMinecart(ExplosiveMinecart minecart) {
		super(minecart);
	}

	public BukkitMCExplosiveMinecart(AbstractionObject ao) {
		this((ExplosiveMinecart) ao.getHandle());
	}

	@Override
	public ExplosiveMinecart getHandle() {
		return (ExplosiveMinecart) metadatable;
	}
}