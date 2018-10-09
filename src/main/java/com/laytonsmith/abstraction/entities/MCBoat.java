package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCTreeSpecies;

public interface MCBoat extends MCVehicle {
	MCTreeSpecies getWoodType();
	void setWoodType(MCTreeSpecies type);
}
