package com.laytonsmith.abstraction;

import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.enums.MCEntityType;

public interface MCCreatureSpawner extends MCBlockState {
	MCEntityType getSpawnedType();
	void setSpawnedType(MCEntityType type);
	int getDelay();
	void setDelay(int delay);
	int getMinDelay();
	void setMinDelay(int delay);
	int getMaxDelay();
	void setMaxDelay(int delay);
	int getSpawnCount();
	void setSpawnCount(int count);
	int getMaxNearbyEntities();
	void setMaxNearbyEntities(int max);
	int getPlayerRange();
	void setPlayerRange(int range);
	int getSpawnRange();
	void setSpawnRange(int range);
}
