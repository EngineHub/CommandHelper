package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Arrow;

import com.laytonsmith.abstraction.entities.MCArrow;
import com.laytonsmith.abstraction.bukkit.BukkitMCProjectile;

/**
 *
 * @author Veyyn
 */
public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	private final Arrow _arrow;

	public BukkitMCArrow(Arrow arrow) {
		super(arrow);
		_arrow = arrow;
	}

	@Override
	public int getKnockbackStrength() {
		return _arrow.getKnockbackStrength();
	}

	@Override
	public void setKnockbackStrength(int strength) {
		_arrow.setKnockbackStrength(strength);
	}

	@Override
	public boolean isCritical() {
		return _arrow.isCritical();
	}

	@Override
	public void setCritical(boolean critical) {
		_arrow.setCritical(critical);
	}
}