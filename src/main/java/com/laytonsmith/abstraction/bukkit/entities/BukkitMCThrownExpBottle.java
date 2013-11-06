package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.ThrownExpBottle;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCThrownExpBottle;

/**
 *
 * @author Hekta
 */
public class BukkitMCThrownExpBottle extends BukkitMCProjectile implements MCThrownExpBottle {

	public BukkitMCThrownExpBottle(ThrownExpBottle bottle) {
		super(bottle);
	}

	public BukkitMCThrownExpBottle(AbstractionObject ao) {
		this((ThrownExpBottle) ao.getHandle());
	}

	@Override
	public ThrownExpBottle getHandle() {
		return (ThrownExpBottle) metadatable;
	}
}