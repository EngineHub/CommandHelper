package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Arrow;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCArrow;

/**
 *
 * @author Hekta
 */
public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	public BukkitMCArrow(Arrow arrow) {
		super(arrow);
	}

	public BukkitMCArrow(AbstractionObject ao) {
		this((Arrow) ao.getHandle());
	}

	@Override
	public Arrow getHandle() {
		return (Arrow) metadatable;
	}
}