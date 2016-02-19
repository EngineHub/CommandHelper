package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCProjectile;

public interface MCArrow extends MCProjectile {

	public int getKnockbackStrength();
	public void setKnockbackStrength(int strength);

	public boolean isCritical();
	public void setCritical(boolean critical);
}