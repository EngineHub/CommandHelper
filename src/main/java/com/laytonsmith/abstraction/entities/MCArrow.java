package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCProjectile;

public interface MCArrow extends MCProjectile {

	int getKnockbackStrength();

	void setKnockbackStrength(int strength);

	boolean isCritical();

	void setCritical(boolean critical);
}
