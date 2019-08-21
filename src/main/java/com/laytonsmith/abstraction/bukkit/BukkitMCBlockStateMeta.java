package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.MCBlockStateMeta;
import com.laytonsmith.core.Static;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.logging.Level;

public class BukkitMCBlockStateMeta extends BukkitMCItemMeta implements MCBlockStateMeta {

	BlockStateMeta bsm;

	public BukkitMCBlockStateMeta(BlockStateMeta meta) {
		super(meta);
		this.bsm = meta;
	}

	@Override
	public MCBlockState getBlockState() {
		BlockState bs;
		try {
			bs = bsm.getBlockState();
		} catch (Exception ex) {
			// Broken server implementation.
			Static.getLogger().log(Level.WARNING, ex.getMessage() + " when"
					+ " trying to get the BlockState from " + bsm.toString());
			return null;
		}
		return BukkitConvertor.BukkitGetCorrectBlockState(bs);
	}

	@Override
	public void setBlockState(MCBlockState state) {
		bsm.setBlockState((BlockState) state.getHandle());
	}
}
