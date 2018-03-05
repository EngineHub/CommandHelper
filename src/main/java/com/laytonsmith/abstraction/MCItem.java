package com.laytonsmith.abstraction;

public interface MCItem extends MCEntity {

	MCItemStack getItemStack();

	int getPickupDelay();

	void setItemStack(MCItemStack stack);

	void setPickupDelay(int delay);
}
