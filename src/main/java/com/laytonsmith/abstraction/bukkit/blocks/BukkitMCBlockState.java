package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import org.bukkit.block.BlockState;

public class BukkitMCBlockState extends BukkitMCMetadatable implements MCBlockState {

	BlockState bs;

	public BukkitMCBlockState(BlockState state) {
		super(state);
		this.bs = state;
	}

	@Override
	public BlockState getHandle() {
		return bs;
	}

	@Override
	public MCMaterial getType() {
		return new BukkitMCMaterial(bs.getType());
	}

	@Override
	public MCBlock getBlock() {
		return new BukkitMCBlock(bs.getBlock());
	}

	@Override
	public MCLocation getLocation() {
		return new BukkitMCLocation(bs.getLocation());
	}

	@Override
	public void update() {
		bs.update();
	}
}
