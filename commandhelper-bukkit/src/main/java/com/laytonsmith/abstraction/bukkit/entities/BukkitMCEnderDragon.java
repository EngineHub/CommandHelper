package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;

/**
 *
 * @author Hekta
 */
public class BukkitMCEnderDragon extends BukkitMCComplexLivingEntity implements MCEnderDragon {

	public BukkitMCEnderDragon(Entity dragon) {
		super(dragon);
	}

	public BukkitMCEnderDragon(AbstractionObject ao) {
		this((EnderDragon) ao.getHandle());
	}

	@Override
	public EnderDragon getHandle() {
		return (EnderDragon)super.getHandle();
	}
}