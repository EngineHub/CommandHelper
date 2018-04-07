package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCChest;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;

import org.bukkit.block.Chest;

public class BukkitMCChest extends BukkitMCBlockState implements MCChest {
	
	private Chest chest;
	
	public BukkitMCChest(Chest block) {
		super(block);
		this.chest = block;
	}
	
	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.chest.getInventory());
	}
}
