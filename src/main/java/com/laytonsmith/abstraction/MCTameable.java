package com.laytonsmith.abstraction;

public interface MCTameable extends MCAgeable {

	boolean isTamed();

	void setTamed(boolean bln);

	MCAnimalTamer getOwner();

	void setOwner(MCAnimalTamer at);
}
