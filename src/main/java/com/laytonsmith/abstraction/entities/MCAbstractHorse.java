package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCInventoryHolder;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCAbstractHorse extends MCTameable, MCVehicle, MCInventoryHolder {

	double getJumpStrength();

	void setJumpStrength(double strength);

	int getDomestication();

	int getMaxDomestication();

	void setDomestication(int level);

	void setMaxDomestication(int level);

	void setSaddle(MCItemStack stack);

	MCItemStack getSaddle();

}
