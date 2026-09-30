package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCChiseledBookshelf;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import org.bukkit.block.ChiseledBookshelf;

public class BukkitMCChiseledBookshelf extends BukkitMCBlockState implements MCChiseledBookshelf {

	ChiseledBookshelf cbs;

	public BukkitMCChiseledBookshelf(ChiseledBookshelf cbs) {
		super(cbs);
		this.cbs = cbs;
	}

	@Override
	public ChiseledBookshelf getHandle() {
		return this.cbs;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.cbs.getInventory());
	}
}
