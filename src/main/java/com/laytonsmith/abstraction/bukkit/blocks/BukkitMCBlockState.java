

package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.MCMaterialData;
import com.laytonsmith.abstraction.blocks.MCBlockState;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterialData;
import com.laytonsmith.abstraction.bukkit.BukkitMCMetadatable;
import org.bukkit.block.BlockState;

/**
 *
 * @author layton
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
    
}
