package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterial;
import org.bukkit.entity.FallingBlock;

/**
 *
 * @author import
 */
public class BukkitMCFallingBlock extends BukkitMCEntity implements MCFallingBlock {

	public BukkitMCFallingBlock(FallingBlock falling) {
		super(falling);
	}

	public BukkitMCFallingBlock(AbstractionObject ao) {
		this((FallingBlock) ao.getHandle());
	}

	@Override
	public FallingBlock getHandle() {
		return (FallingBlock) metadatable;
	}

	public MCMaterial getMaterial() {
		return new BukkitMCMaterial(getHandle().getMaterial());
	}

	public boolean getDropItem() {
		return getHandle().getDropItem();
	}

	public void setDropItem(boolean drop) {
		getHandle().setDropItem(drop);
	}
}