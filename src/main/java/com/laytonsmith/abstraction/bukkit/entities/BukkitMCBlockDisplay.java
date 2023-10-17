package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import com.laytonsmith.abstraction.entities.MCBlockDisplay;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;

public class BukkitMCBlockDisplay extends BukkitMCDisplay implements MCBlockDisplay {

	BlockDisplay bd;

	public BukkitMCBlockDisplay(Entity e) {
		super(e);
		this.bd = (BlockDisplay) e;
	}

	@Override
	public MCBlockData getBlockData() {
		return new BukkitMCBlockData(this.bd.getBlock());
	}

	@Override
	public void setBlockData(MCBlockData data) {
		this.bd.setBlock((BlockData) data.getHandle());
	}
}
