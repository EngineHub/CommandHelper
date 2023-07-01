package com.laytonsmith.abstraction.entities;

public interface MCBreedable extends MCAgeable {

	boolean getCanBreed();

	void setCanBreed(boolean breed);

	boolean getAgeLock();

	void setAgeLock(boolean lock);
}
