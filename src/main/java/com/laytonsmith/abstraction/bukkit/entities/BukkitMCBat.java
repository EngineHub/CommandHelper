package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Bat;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCBat;

/**
 *
 * @author Hekta
 */
public class BukkitMCBat extends BukkitMCLivingEntity implements MCBat {

	public BukkitMCBat(Bat bat) {
		super(bat);
	}

	public BukkitMCBat(AbstractionObject ao) {
		this((Bat) ao.getHandle());
	}

	@Override
	public Bat getHandle() {
		return (Bat) metadatable;
	}
}