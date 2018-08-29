package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.enums.MCTreeSpecies;

public interface MCBoat extends MCVehicle {
	double getMaxSpeed();
	void setMaxSpeed(double speed);
	MCTreeSpecies getWoodType();
	void setWoodType(MCTreeSpecies type);
}
