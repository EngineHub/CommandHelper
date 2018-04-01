package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCVehicle;

public interface MCBoat extends MCVehicle {

	double getMaxSpeed();

	void setMaxSpeed(double speed);
}
