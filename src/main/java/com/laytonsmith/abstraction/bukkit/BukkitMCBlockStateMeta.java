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
		return getBlockState(false);
	}

	@Override
	public MCBlockState getBlockState(boolean copy) {
		BlockStateMeta meta = bsm;
		if(copy) {
			// Getting a BlockState currently writes to the block entity tags for some block types.
			// Since the tags are no longer equal when compared later, unexpected behavior can occur.
			// For example, when getting a shulker box's BlockState on BlockPlaceEvent, it can duplicate the item.
			// Copying the meta before getting the block state ensures the original tags are unaffected.
			meta = ((BlockStateMeta) meta.clone());
		}
		BlockState bs;
		try {
			bs = meta.getBlockState();
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
