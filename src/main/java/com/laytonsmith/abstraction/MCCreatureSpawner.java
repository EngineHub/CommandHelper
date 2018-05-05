package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.enums.MCEntityType;

public interface MCCreatureSpawner extends MCBlockState {
	MCEntityType getSpawnedType();
	void setSpawnedType(MCEntityType type);
	int getDelay();
	void setDelay(int delay);
}
