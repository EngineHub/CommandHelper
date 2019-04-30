package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLivingEntity;
import com.laytonsmith.abstraction.enums.MCMushroomCowType;

public interface MCMushroomCow extends MCLivingEntity {
	MCMushroomCowType getVariant();
	void setVariant(MCMushroomCowType type);
}
