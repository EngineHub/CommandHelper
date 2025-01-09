package com.laytonsmith.abstraction.blocks;

import com.laytonsmith.abstraction.MCLocation;

public interface MCEndGateway extends MCBlockState {
	MCLocation getExitLocation();
	void setExitLocation(MCLocation location);
	boolean isExactTeleport();
	void setExactTeleport(boolean isExact);
	long getAge();
	void setAge(long age);
}
