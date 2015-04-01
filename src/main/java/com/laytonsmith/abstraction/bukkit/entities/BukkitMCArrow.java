package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;

/**
 *
 * @author Veyyn
 */
public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	private final Arrow _arrow;

	public BukkitMCArrow(Entity arrow) {
		super((Projectile) arrow);
		_arrow = (Arrow) arrow;
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