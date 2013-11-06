package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Witch;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCWitch;

/**
 *
 * @author Hekta
 */
public class BukkitMCWitch extends BukkitMCCreature implements MCWitch {

	public BukkitMCWitch(Witch witch) {
		super(witch);
	}

	public BukkitMCWitch(AbstractionObject ao) {
		this((Witch) ao.getHandle());
	}

	@Override
	public Witch getHandle() {
		return (Witch) metadatable;
	}
}