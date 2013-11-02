package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Cow;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCCow;

/**
 *
 * @author Hekta
 */
public class BukkitMCCow extends BukkitMCAgeable implements MCCow {

	public BukkitMCCow(Cow cow) {
		super(cow);
	}

	public BukkitMCCow(AbstractionObject ao) {
		this((Cow) ao.getHandle());
	}

	@Override
	public Cow getHandle() {
		return (Cow) metadatable;
	}
}