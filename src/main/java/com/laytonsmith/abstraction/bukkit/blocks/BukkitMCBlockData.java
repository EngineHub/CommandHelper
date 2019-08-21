package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCBlockData;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import org.bukkit.block.data.BlockData;

public class BukkitMCBlockData implements MCBlockData {
	BlockData bd;

	protected BukkitMCBlockData() {}

	public BukkitMCBlockData(BlockData data) {
		this.bd = data;
	}

	@Override
	public BlockData getHandle() {
		return bd;
	}

	@Override
	public MCMaterial getMaterial() {
		return new BukkitMCMaterial(bd.getMaterial());
	}

	@Override
	public String getAsString() {
		return bd.getAsString();
	}

	@Override
	public String toString() {
		return bd.toString();
	}

	@Override
	public int hashCode() {
		return bd.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof BlockData && bd.equals(obj);
	}
}
