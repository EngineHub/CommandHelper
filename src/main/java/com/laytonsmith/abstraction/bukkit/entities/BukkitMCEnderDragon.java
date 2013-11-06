package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.EnderDragon;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderDragon;

/**
 *
 * @author Hekta
 */
public class BukkitMCEnderDragon extends BukkitMCComplexLivingEntity implements MCEnderDragon {

	public BukkitMCEnderDragon(EnderDragon dragon) {
		super(dragon);
	}

	public BukkitMCEnderDragon(AbstractionObject ao) {
		this((EnderDragon) ao.getHandle());
	}

	@Override
	public EnderDragon getHandle() {
		return (EnderDragon) metadatable;
	}
}