package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.MCVehicle;

public interface MCPig extends MCAgeable, MCVehicle {

	boolean isSaddled();

	void setSaddled(boolean saddled);
}
