package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCHanging;
import com.laytonsmith.abstraction.blocks.MCBlockFace;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;

public class BukkitMCHanging extends BukkitMCEntity implements MCHanging {

	Hanging h;
	public BukkitMCHanging(Entity e) {
		super(e);
		this.h = (Hanging) e;
	}
	
	@Override
	public MCBlockFace getFacing() {
		String dir = h.getFacing().name();
		return MCBlockFace.valueOf(dir);
	}
	
	@Override
	public void setFacingDirection(MCBlockFace direction) {
		BlockFace dir = BlockFace.valueOf(direction.name());
		h.setFacingDirection(dir);
	}
	
	@Override
	public boolean setFacingDirection(MCBlockFace direction, boolean force) {
		BlockFace dir = BlockFace.valueOf(direction.name());
		return h.setFacingDirection(dir, force);
	}
}
