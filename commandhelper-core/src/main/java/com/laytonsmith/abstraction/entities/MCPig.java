package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAgeable;
import com.laytonsmith.abstraction.MCVehicle;

public interface MCPig extends MCAgeable, MCVehicle {
	public boolean isSaddled();
	public void setSaddled(boolean saddled);
}
