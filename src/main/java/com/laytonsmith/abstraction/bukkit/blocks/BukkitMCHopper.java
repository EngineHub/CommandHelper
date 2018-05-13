package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCHopper;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;

import org.bukkit.block.Hopper;

public class BukkitMCHopper extends BukkitMCBlockState implements MCHopper {

	private Hopper hopper;

	public BukkitMCHopper(Hopper block) {
		super(block);
		this.hopper = block;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.hopper.getInventory());
	}
}
