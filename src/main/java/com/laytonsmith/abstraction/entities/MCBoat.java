package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCLeashable;
import com.laytonsmith.abstraction.enums.MCTreeSpecies;

public interface MCBoat extends MCVehicle, MCLeashable {
	MCTreeSpecies getWoodType();
	void setWoodType(MCTreeSpecies type);
}
