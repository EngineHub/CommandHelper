package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCAnimal;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;

import java.util.UUID;

public class BukkitMCAnimal extends BukkitMCAgeable implements MCAnimal {

	private final Animals animal;

	public BukkitMCAnimal(Entity e) {
		super(e);
		this.animal = (Animals) e;
	}

	@Override
	public int getLoveTicks() {
		return this.animal.getLoveModeTicks();
	}

	@Override
	public void setLoveTicks(int ticks) {
		this.animal.setLoveModeTicks(ticks);
	}

	@Override
	public UUID getBreedCause() {
		return this.animal.getBreedCause();
	}

	@Override
	public void setBreedCause(UUID cause) {
		this.animal.setBreedCause(cause);
	}
}
