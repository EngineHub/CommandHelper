package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCLocation;

public interface MCEnderSignal extends MCEntity {
	int getDespawnTicks();
	void setDespawnTicks(int ticks);
	boolean getDropItem();
	void setDropItem(boolean drop);
	MCLocation getTargetLocation();
	void setTargetLocation(MCLocation loc);
}
