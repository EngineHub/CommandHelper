package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCChestBoat;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;

public class BukkitMCChestBoat extends BukkitMCBoat implements MCChestBoat {

	ChestBoat cb;

	public BukkitMCChestBoat(Entity cb) {
		super(cb);
		this.cb = (ChestBoat) cb;
	}

	@Override
	public ChestBoat getHandle() {
		return this.cb;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.cb.getInventory());
	}
}
