package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.blocks.MCBlockData;

public interface MCMinecart extends MCVehicle {

	void setDamage(double damage);

	double getDamage();

	double getMaxSpeed();

	void setMaxSpeed(double speed);

	boolean isSlowWhenEmpty();

	void setSlowWhenEmpty(boolean slow);

	void setDisplayBlock(MCBlockData data);

	MCBlockData getDisplayBlock();

	void setDisplayBlockOffset(int offset);

	int getDisplayBlockOffset();
}
