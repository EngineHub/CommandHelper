package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Blaze;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCBlaze;

/**
 *
 * @author Hekta
 */
public class BukkitMCBlaze extends BukkitMCCreature implements MCBlaze {

	public BukkitMCBlaze(Blaze blaze) {
		super(blaze);
	}

	public BukkitMCBlaze(AbstractionObject ao) {
		this((Blaze) ao.getHandle());
	}

	@Override
	public Blaze getHandle() {
		return (Blaze) metadatable;
	}
}