package com.laytonsmith.abstraction.entities;

import java.util.UUID;

public interface MCAnimal extends MCAgeable {
	int getLoveTicks();
	void setLoveTicks(int ticks);
	UUID getBreedCause();
	void setBreedCause(UUID cause);
}
