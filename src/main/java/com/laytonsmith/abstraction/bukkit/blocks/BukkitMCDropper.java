package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCDropper;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;

import org.bukkit.block.Dropper;

public class BukkitMCDropper extends BukkitMCBlockState implements MCDropper {

	private Dropper dropper;

	public BukkitMCDropper(Dropper block) {
		super(block);
		this.dropper = block;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.dropper.getInventory());
	}

	@Override
	public void drop() {
		this.dropper.drop();
	}
}
