package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCNautilus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Nautilus;

public class BukkitMCNautilus extends BukkitMCTameable implements MCNautilus {

	public BukkitMCNautilus(Entity nautilus) {
		super(nautilus);
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(((Nautilus) getHandle()).getInventory());
	}
}
