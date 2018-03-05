package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCShulkerBox;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import org.bukkit.block.ShulkerBox;

public class BukkitMCShulkerBox extends BukkitMCBlockState implements MCShulkerBox {

	ShulkerBox sb;

	public BukkitMCShulkerBox(ShulkerBox block) {
		super(block);
		sb = block;
	}

	@Override
	public ShulkerBox getHandle() {
		return sb;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(sb.getInventory());
	}
}
