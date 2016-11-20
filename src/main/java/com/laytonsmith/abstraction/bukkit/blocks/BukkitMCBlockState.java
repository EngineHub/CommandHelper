package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCLocation;
import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCBlock;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.BukkitMCLocation;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import org.bukkit.block.BlockState;

/**
 *
 * 
 */
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
    public MCMaterialData getData() {
        return new BukkitMCMaterialData(bs.getData());
    }

	@Override
    public int getTypeId() {
        return bs.getTypeId();
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