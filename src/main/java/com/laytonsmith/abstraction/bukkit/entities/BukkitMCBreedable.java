package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCBreedable;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Entity;

public class BukkitMCBreedable extends BukkitMCAgeable implements MCBreedable {

	Breedable b;

	public BukkitMCBreedable(Entity be) {
		super(be);
		this.b = (Breedable) be;
	}

	@Override
	public boolean getCanBreed() {
		return b.canBreed();
	}

	@Override
	public void setCanBreed(boolean breed) {
		b.setBreed(breed);
	}

	@Override
	public boolean getAgeLock() {
		return b.getAgeLock();
	}

	@Override
	public void setAgeLock(boolean lock) {
		b.setAgeLock(lock);
	}

}
