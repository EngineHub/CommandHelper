package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCVehicle;

public interface MCBoat extends MCVehicle {
	public double getMaxSpeed();
	public void setMaxSpeed(double speed);
	public double getOccupiedDeclaration();
	public void setOccupiedDeclaration(double rate);
	public double getUnoccupiedDeclaration();
	public void setUnoccupiedDeclaration(double rate);
	public boolean getWorkOnLand();
	public void setWorkOnLand(boolean workOnLand);
}
