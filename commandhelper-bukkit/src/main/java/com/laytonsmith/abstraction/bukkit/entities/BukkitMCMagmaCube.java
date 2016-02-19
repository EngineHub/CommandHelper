package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCMagmaCube;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;

/**
 *
 * @author Hekta
 */
public class BukkitMCMagmaCube extends BukkitMCSlime implements MCMagmaCube {

	public BukkitMCMagmaCube(Entity cube) {
		super(cube);
	}

	public BukkitMCMagmaCube(AbstractionObject ao) {
		this((MagmaCube) ao.getHandle());
	}
}