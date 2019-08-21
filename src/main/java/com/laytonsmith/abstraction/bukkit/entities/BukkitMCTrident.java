package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCTrident;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Trident;

public class BukkitMCTrident extends BukkitMCProjectile implements MCTrident {

	private final Trident trident;

	public BukkitMCTrident(Entity trident) {
		super(trident);
		this.trident = (Trident) trident;
	}

	@Override
	public int getKnockbackStrength() {
		return this.trident.getKnockbackStrength();
	}

	@Override
	public void setKnockbackStrength(int strength) {
		this.trident.setKnockbackStrength(strength);
	}

	@Override
	public boolean isCritical() {
		return this.trident.isCritical();
	}

	@Override
	public void setCritical(boolean critical) {
		this.trident.setCritical(critical);
	}

	@Override
	public double getDamage() {
		return this.trident.getDamage();
	}

	@Override
	public void setDamage(double damage) {
		this.trident.setDamage(damage);
	}
}
