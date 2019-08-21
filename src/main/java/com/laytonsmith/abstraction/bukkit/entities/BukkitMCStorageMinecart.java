package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCStorageMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;

public class BukkitMCStorageMinecart extends BukkitMCMinecart implements MCStorageMinecart {

	StorageMinecart sm;

	public BukkitMCStorageMinecart(Entity e) {
		super(e);
		this.sm = (StorageMinecart) e;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(sm.getInventory());
	}
}
