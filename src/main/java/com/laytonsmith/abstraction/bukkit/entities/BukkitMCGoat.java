package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCGoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Goat;

public class BukkitMCGoat extends BukkitMCAgeable implements MCGoat {

	Goat g;

	public BukkitMCGoat(Entity goat) {
		super(goat);
		this.g = (Goat) goat;
	}

	@Override
	public Goat getHandle() {
		return g;
	}

	@Override
	public boolean isScreaming() {
		return g.isScreaming();
	}

	@Override
	public void setScreaming(boolean screaming) {
		g.setScreaming(screaming);
	}
}
