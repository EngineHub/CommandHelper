package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCLocation;

public interface MCBeehive extends MCBlockState {
	MCLocation getFlowerLocation();
	void setFlowerLocation(MCLocation loc);
	void addBees(int count);
	int getEntityCount();
}
