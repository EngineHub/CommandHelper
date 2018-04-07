package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCShulkerBox;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import org.bukkit.block.ShulkerBox;

public class BukkitMCShulkerBox extends BukkitMCBlockState implements MCShulkerBox {

	ShulkerBox sb;

	public BukkitMCShulkerBox(ShulkerBox block) {
		super(block);
		this.sb = block;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.sb.getInventory());
	}
}
