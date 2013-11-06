package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Creeper;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCCreeper;

/**
 *
 * @author Hekta
 */
public class BukkitMCCreeper extends BukkitMCCreature implements MCCreeper {

	public BukkitMCCreeper(Creeper creeper) {
		super(creeper);
	}

	public BukkitMCCreeper(AbstractionObject ao) {
		this((Creeper) ao.getHandle());
	}

	@Override
	public Creeper getHandle() {
		return (Creeper) metadatable;
	}

	public boolean isPowered() {
		return getHandle().isPowered();
	}

	public void setPowered(boolean isPowered) {
		getHandle().setPowered(isPowered);
	}
}