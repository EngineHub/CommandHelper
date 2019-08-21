package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.blocks.MCLectern;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import org.bukkit.block.Lectern;

public class BukkitMCLectern extends BukkitMCBlockState implements MCLectern {

	private Lectern l;

	public BukkitMCLectern(Lectern block) {
		super(block);
		this.l = block;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(this.l.getInventory());
	}

	@Override
	public int getPage() {
		return l.getPage();
	}

	@Override
	public void setPage(int page) {
		l.setPage(page);
	}
}
