package com.laytonsmith.abstraction.bukkit;

import com.laytonsmith.abstraction.MCHanging;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;

public class BukkitMCHanging extends BukkitMCEntity implements MCHanging {

	@WrappedItem Hanging h;
	
	public MCBlockFace getFacing() {
		String dir = h.getFacing().name();
		return MCBlockFace.valueOf(dir);
	}
	
	public void setFacingDirection(MCBlockFace direction) {
		BlockFace dir = BlockFace.valueOf(direction.name());
		h.setFacingDirection(dir);
	}
	
	public boolean setFacingDirection(MCBlockFace direction, boolean force) {
		BlockFace dir = BlockFace.valueOf(direction.name());
		return h.setFacingDirection(dir, force);
	}
}
