package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;

public interface MCPiglin extends MCLivingEntity {
	boolean isBaby();
	void setBaby(boolean baby);
}
