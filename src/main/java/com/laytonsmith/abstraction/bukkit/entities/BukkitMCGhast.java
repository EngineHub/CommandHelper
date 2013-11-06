package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Ghast;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCGhast;

/**
 *
 * @author Hekta
 */
public class BukkitMCGhast extends BukkitMCLivingEntity implements MCGhast {

	public BukkitMCGhast(Ghast ghast) {
		super(ghast);
	}

	public BukkitMCGhast(AbstractionObject ao) {
		this((Ghast) ao.getHandle());
	}

	@Override
	public Ghast getHandle() {
		return (Ghast) metadatable;
	}
}