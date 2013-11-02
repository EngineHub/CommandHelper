package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderCrystal;

import org.bukkit.entity.EnderCrystal;

/**
 *
 * @author Layton
 */
public class BukkitMCEnderCrystal extends BukkitMCEntity implements MCEnderCrystal {

	public BukkitMCEnderCrystal(EnderCrystal crystal) {
		super(crystal);
	}

	public BukkitMCEnderCrystal(AbstractionObject ao) {
		this((EnderCrystal) ao.getHandle());
	}

	@Override
	public EnderCrystal getHandle() {
		return (EnderCrystal) metadatable;
	}
}