package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCHopperMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.HopperMinecart;

public class BukkitMCHopperMinecart extends BukkitMCMinecart implements MCHopperMinecart {

	HopperMinecart hm;

	public BukkitMCHopperMinecart(Entity e) {
		super(e);
		this.hm = (HopperMinecart) e;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(hm.getInventory());
	}
}
