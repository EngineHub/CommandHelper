package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.MagmaCube;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCMagmaCube;

/**
 *
 * @author Hekta
 */
public class BukkitMCMagmaCube extends BukkitMCSlime implements MCMagmaCube {

	public BukkitMCMagmaCube(MagmaCube cube) {
		super(cube);
	}

	public BukkitMCMagmaCube(AbstractionObject ao) {
		this((MagmaCube) ao.getHandle());
	}

	@Override
	public MagmaCube getHandle() {
		return (MagmaCube) metadatable;
	}
}