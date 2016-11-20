package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;

public class BukkitMCBlockStateMeta extends BukkitMCItemMeta implements MCBlockStateMeta {

	BlockStateMeta bsm;

	public BukkitMCBlockStateMeta(BlockStateMeta meta) {
		super(meta);
		this.bsm = meta;
	}

	@Override
	public MCBlockState getBlockState() {
		return BukkitConvertor.BukkitGetCorrectBlockState(bsm.getBlockState());
	}

	@Override
	public void setBlockState(MCBlockState state) {
		bsm.setBlockState((BlockState) state.getHandle());
	}
}