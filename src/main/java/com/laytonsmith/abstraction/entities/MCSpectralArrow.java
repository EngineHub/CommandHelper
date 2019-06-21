package com.laytonsmith.abstraction.entities;

public interface MCSpectralArrow extends MCProjectile {
	int getKnockbackStrength();
	void setKnockbackStrength(int strength);
	boolean isCritical();
	void setCritical(boolean critical);
	double getDamage();
	void setDamage(double damage);
	int getGlowingTicks();
	void setGlowingTicks(int ticks);
}
