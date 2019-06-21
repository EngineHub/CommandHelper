package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCSpectralArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.SpectralArrow;

public class BukkitMCSpectralArrow extends BukkitMCProjectile implements MCSpectralArrow {

	private final SpectralArrow spectral;

	public BukkitMCSpectralArrow(Entity arrow) {
		super(arrow);
		this.spectral = (SpectralArrow) arrow;
	}

	@Override
	public int getKnockbackStrength() {
		return this.spectral.getKnockbackStrength();
	}

	@Override
	public void setKnockbackStrength(int strength) {
		this.spectral.setKnockbackStrength(strength);
	}

	@Override
	public boolean isCritical() {
		return this.spectral.isCritical();
	}

	@Override
	public void setCritical(boolean critical) {
		this.spectral.setCritical(critical);
	}

	@Override
	public double getDamage() {
		return this.spectral.getDamage();
	}

	@Override
	public void setDamage(double damage) {
		this.spectral.setDamage(damage);
	}

	@Override
	public int getGlowingTicks() {
		return spectral.getGlowingTicks();
	}

	@Override
	public void setGlowingTicks(int ticks) {
		spectral.setGlowingTicks(ticks);
	}
}
