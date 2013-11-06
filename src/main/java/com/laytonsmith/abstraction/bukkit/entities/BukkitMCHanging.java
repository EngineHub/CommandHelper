package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCHanging;
import com.laytonsmith.abstraction.enums.MCBlockFace;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Hanging;

public abstract class BukkitMCHanging extends BukkitMCEntity implements MCHanging {

	public BukkitMCHanging(Hanging hanging) {
		super(hanging);
	}

	@Override
	public Hanging getHandle() {
		return (Hanging) metadatable;
	}

	public MCBlockFace getFacing() {
		String dir = getHandle().getFacing().name();
		return MCBlockFace.valueOf(dir);
	}

	public void setFacingDirection(MCBlockFace direction) {
		BlockFace dir = BlockFace.valueOf(direction.name());
		getHandle().setFacingDirection(dir);
	}

	public boolean setFacingDirection(MCBlockFace direction, boolean force) {
		BlockFace dir = BlockFace.valueOf(direction.name());
		return getHandle().setFacingDirection(dir, force);
	}
}