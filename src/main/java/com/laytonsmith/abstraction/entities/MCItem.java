package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCEntity;
import com.laytonsmith.abstraction.MCItemStack;

import java.util.UUID;

public interface MCItem extends MCEntity {

	MCItemStack getItemStack();

	int getPickupDelay();

	void setItemStack(MCItemStack stack);

	void setPickupDelay(int delay);

	UUID getOwner();

	void setOwner(UUID owner);

	UUID getThrower();

	void setThrower(UUID thrower);

	boolean willDespawn();

	void setWillDespawn(boolean despawn);
}
