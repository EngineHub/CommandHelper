package com.laytonsmith.abstraction.entities;

public interface MCStrider extends MCAnimal, MCVehicle {
	boolean isSaddled();
	void setSaddled(boolean saddled);
}
