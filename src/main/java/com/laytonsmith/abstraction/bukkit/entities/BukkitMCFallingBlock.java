package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCFallingBlock;
import com.laytonsmith.abstraction.blocks.MCMaterial;
import com.laytonsmith.abstraction.bukkit.blocks.BukkitMCMaterial;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

public class BukkitMCFallingBlock extends BukkitMCEntity implements MCFallingBlock {

	FallingBlock f;

	public BukkitMCFallingBlock(Entity e) {
		super(e);
		this.f = (FallingBlock) e;
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
	public boolean canHurtEntities() {
		return f.canHurtEntities();
	}

	@Override
	public void setHurtEntities(boolean hurt) {
		f.setHurtEntities(hurt);
	}

	@Override
	public FallingBlock getHandle() {
		return f;
	}

}
