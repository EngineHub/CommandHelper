package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCContainer;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;

import org.bukkit.block.Container;

public class BukkitMCContainer extends BukkitMCBlockState implements MCContainer {

	private Container cont;

	public BukkitMCContainer(Container block) {
		super(block);
		this.cont = block;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.cont.getInventory());
	}
}
