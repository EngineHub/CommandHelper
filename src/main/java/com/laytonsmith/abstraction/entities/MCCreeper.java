package com.laytonsmith.abstraction.entities;

public interface MCCreeper {

	boolean isPowered();

	void setPowered(boolean powered);

	int getMaxFuseTicks();

	void setMaxFuseTicks(int ticks);

	int getExplosionRadius();

	void setExplosionRadius(int radius);
}
