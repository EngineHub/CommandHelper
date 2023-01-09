package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCBlockDataMeta;
import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCBlockData;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.meta.BlockDataMeta;

public class BukkitMCBlockDataMeta extends BukkitMCItemMeta implements MCBlockDataMeta {

	BlockDataMeta bdm;

	public BukkitMCBlockDataMeta(BlockDataMeta meta) {
		super(meta);
		this.bdm = meta;
	}


	@Override
	public MCBlockData getBlockData(MCMaterial material) {
		return new BukkitMCBlockData(this.bdm.getBlockData((Material) material.getHandle()));
	}

	@Override
	public boolean hasBlockData() {
		return this.bdm.hasBlockData();
	}

	@Override
	public void setBlockData(MCBlockData blockData) {
		this.bdm.setBlockData((BlockData) blockData.getHandle());
	}
}
