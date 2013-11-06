package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.EnderPearl;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderPearl;

/**
 *
 * @author Hekta
 */
public class BukkitMCEnderPearl extends BukkitMCProjectile implements MCEnderPearl {

	public BukkitMCEnderPearl(EnderPearl pearl) {
		super(pearl);
	}

	public BukkitMCEnderPearl(AbstractionObject ao) {
		this((EnderPearl) ao.getHandle());
	}

	@Override
	public EnderPearl getHandle() {
		return (EnderPearl) metadatable;
	}
}