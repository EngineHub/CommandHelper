package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Slime;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSlime;

/**
 *
 * @author Hekta
 */
public class BukkitMCSlime extends BukkitMCLivingEntity implements MCSlime {

	public BukkitMCSlime(Slime slime) {
		super(slime);
	}

	public BukkitMCSlime(AbstractionObject ao) {
		this((Slime) ao.getHandle());
	}

	@Override
	public Slime getHandle() {
		return (Slime) metadatable;
	}

	public int getSize() {
		return getHandle().getSize();
	}

	public void setSize(int size) {
		getHandle().setSize(size);
	}
}