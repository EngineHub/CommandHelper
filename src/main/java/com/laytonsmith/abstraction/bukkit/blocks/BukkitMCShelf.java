package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCShelf;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import org.bukkit.block.Shelf;

public class BukkitMCShelf extends BukkitMCBlockState implements MCShelf {

	Shelf s;

	public BukkitMCShelf(Shelf s) {
		super(s);
		this.s = s;
	}

	@Override
	public Shelf getHandle() {
		return this.s;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.s.getInventory());
	}
}
