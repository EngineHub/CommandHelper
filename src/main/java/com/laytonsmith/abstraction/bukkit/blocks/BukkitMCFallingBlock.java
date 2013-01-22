
package com.laytonsmith.abstraction.bukkit.blocks;

import com.laytonsmith.abstraction.blocks.MCFallingBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
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
	
	public byte getBlockData() {
		return f.getBlockData();
	}

	public int getBlockId() {
		return f.getBlockId();
	}

	public boolean getDropItem() {
		return f.getDropItem();
	}

	public MCMaterial getMaterial() {
		return new BukkitMCMaterial(f.getMaterial());
	}

	public void setDropItem(boolean drop) {
		f.setDropItem(drop);
	}

	public Object getHandle() {
		return f;
	}
	
}
