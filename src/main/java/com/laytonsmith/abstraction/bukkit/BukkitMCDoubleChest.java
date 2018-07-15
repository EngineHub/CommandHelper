package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCDoubleChest;
import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.MCLocation;
import org.bukkit.block.DoubleChest;

public class BukkitMCDoubleChest implements MCDoubleChest {

	DoubleChest dc;

	public BukkitMCDoubleChest(DoubleChest chest) {
		this.dc = chest;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.dc.getInventory());
	}

	@Override
	public MCLocation getLocation() {
		return new BukkitMCLocation(this.dc.getLocation());
	}

	@Override
	public Object getHandle() {
		return this.dc;
	}
}
