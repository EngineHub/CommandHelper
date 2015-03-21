
package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCFallingBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.entities.BukkitMCEntity;
import org.bukkit.entity.FallingBlock;

/**
 *
 * @author import
 */
public class BukkitMCFallingBlock extends BukkitMCEntity implements MCFallingBlock {
	FallingBlock f;
	
	public BukkitMCFallingBlock(FallingBlock f) {
		super(f);
		this.f = f;
	}
	
	@Override
	public byte getBlockData() {
		return f.getBlockData();
	}

	@Override
	public int getBlockId() {
		return f.getBlockId();
	}

	@Override
	public boolean getDropItem() {
		return f.getDropItem();
	}

	@Override
	public MCMaterial getMaterial() {
		return new BukkitMCMaterial(f.getMaterial());
	}

	@Override
	public void setDropItem(boolean drop) {
		f.setDropItem(drop);
	}

	@Override
	public FallingBlock getHandle() {
		return f;
	}
	
}
