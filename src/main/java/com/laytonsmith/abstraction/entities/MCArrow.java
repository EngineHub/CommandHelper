package com.laytonsmith.abstraction.entities;

public interface MCArrow extends MCProjectile {

	int getKnockbackStrength();

	void setKnockbackStrength(int strength);

	boolean isCritical();

	void setCritical(boolean critical);
}
