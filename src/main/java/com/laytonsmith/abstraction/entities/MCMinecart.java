package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCMaterialData;

public interface MCMinecart extends MCVehicle {

	void setDamage(double damage);

	double getDamage();

	double getMaxSpeed();

	void setMaxSpeed(double speed);

	boolean isSlowWhenEmpty();

	void setSlowWhenEmpty(boolean slow);

	void setDisplayBlock(MCMaterialData material);

	MCMaterialData getDisplayBlock();

	void setDisplayBlockOffset(int offset);

	int getDisplayBlockOffset();
}
