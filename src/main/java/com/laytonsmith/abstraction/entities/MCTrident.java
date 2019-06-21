package com.laytonsmith.abstraction.entities;

public interface MCTrident extends MCProjectile {
	int getKnockbackStrength();
	void setKnockbackStrength(int strength);
	boolean isCritical();
	void setCritical(boolean critical);
	double getDamage();
	void setDamage(double damage);
}
