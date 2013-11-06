package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.MCMaterial;
import com.laytonsmith.abstraction.bukkit.BukkitMCMaterial;
import org.bukkit.entity.FallingSand;

/**
 * Will be deprecated.
 * @author Hekta
 */
public class BukkitMCFallingSand extends BukkitMCEntity implements MCFallingBlock {

	public BukkitMCFallingSand(FallingSand falling) {
		super(falling);
	}

	public BukkitMCFallingSand(AbstractionObject ao) {
		this((FallingSand) ao.getHandle());
	}

	@Override
	public FallingSand getHandle() {
		return (FallingSand) metadatable;
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