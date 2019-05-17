package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;

public class BukkitMCArrow extends BukkitMCProjectile implements MCArrow {

	private final Arrow arrow;

	public BukkitMCArrow(Entity arrow) {
		super(arrow);
		this.arrow = (Arrow) arrow;
	}

	@Override
	public int getKnockbackStrength() {
		return this.arrow.getKnockbackStrength();
	}

	@Override
	public void setKnockbackStrength(int strength) {
		this.arrow.setKnockbackStrength(strength);
	}

	@Override
	public boolean isCritical() {
		return this.arrow.isCritical();
	}

	@Override
	public void setCritical(boolean critical) {
		this.arrow.setCritical(critical);
	}

	@Override
	public double getDamage() {
		return this.arrow.getDamage();
	}

	@Override
	public void setDamage(double damage) {
		this.arrow.setDamage(damage);
	}
}
