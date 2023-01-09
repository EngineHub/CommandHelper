package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCMushroomCowType;

public interface MCMushroomCow extends MCAnimal {
	MCMushroomCowType getVariant();
	void setVariant(MCMushroomCowType type);
}
