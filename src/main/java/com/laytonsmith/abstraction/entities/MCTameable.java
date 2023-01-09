package com.laytonsmith.abstraction.entities;

import com.laytonsmith.abstraction.MCAnimalTamer;

public interface MCTameable extends MCAnimal {

	boolean isTamed();

	void setTamed(boolean bln);

	MCAnimalTamer getOwner();

	void setOwner(MCAnimalTamer at);
}
