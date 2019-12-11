package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.MCLocation;

public interface MCBee extends MCLivingEntity {
	MCLocation getHiveLocation();
	void setHiveLocation(MCLocation loc);
	MCLocation getFlowerLocation();
	void setFlowerLocation(MCLocation loc);
	boolean hasNectar();
	void setHasNectar(boolean nectar);
	boolean hasStung();
	void setHasStung(boolean stung);
	int getAnger();
	void setAnger(int ticks);
}
