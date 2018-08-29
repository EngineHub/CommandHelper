package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;

public interface MCItem extends MCEntity {

	MCItemStack getItemStack();

	int getPickupDelay();

	void setItemStack(MCItemStack stack);

	void setPickupDelay(int delay);
}
