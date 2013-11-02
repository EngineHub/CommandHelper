package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Explosive;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCExplosive;

/**
 *
 * @author Hekta
 */
public class BukkitMCExplosive extends BukkitMCEntity implements MCExplosive {

	public BukkitMCExplosive(Explosive explosive) {
		super(explosive);
	}

	public BukkitMCExplosive(AbstractionObject ao) {
		this((Explosive) ao.getHandle());
	}

	@Override
	public Explosive getHandle() {
		return (Explosive) metadatable;
	}

	public float getYield() {
		return getHandle().getYield();
	}

	public void setYield(float yield) {
		getHandle().setYield(yield);
	}

	public boolean isIncendiary() {
		return getHandle().isIncendiary();
	}

	public void setIsIncendiary(boolean isIncendiary) {
		getHandle().setIsIncendiary(isIncendiary);
	}
}