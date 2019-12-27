package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import org.bukkit.block.BlockState;
import org.bukkit.block.Lockable;

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

	@Override
	public boolean isLockable() {
		return bs instanceof Lockable;
	}

	@Override
	public boolean isLocked() {
		if(bs instanceof Lockable) {
			return ((Lockable) bs).isLocked();
		} else {
			throw new ClassCastException("Block is not a Lockable.");
		}
	}

	@Override
	public String getLock() {
		if(bs instanceof Lockable) {
			return ((Lockable) bs).getLock();
		} else {
			throw new ClassCastException("Block is not a Lockable.");
		}
	}

	@Override
	public void setLock(String key) {
		if(bs instanceof Lockable) {
			((Lockable) bs).setLock(key);
			bs.update();
		} else {
			throw new ClassCastException("Block is not a Lockable.");
		}
	}
}
