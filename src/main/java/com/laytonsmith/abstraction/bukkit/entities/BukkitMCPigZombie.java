package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.PigZombie;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPigZombie;

/**
 *
 * @author Hekta
 */
public class BukkitMCPigZombie extends BukkitMCZombie implements MCPigZombie {

	public BukkitMCPigZombie(PigZombie zombie) {
		super(zombie);
	}

	public BukkitMCPigZombie(AbstractionObject ao) {
		this((PigZombie) ao.getHandle());
	}

	@Override
	public PigZombie getHandle() {
		return (PigZombie) metadatable;
	}

	public int getAnger() {
		return getHandle().getAnger();
	}

	public void setAnger(int anger) {
		getHandle().setAnger(anger);
	}

	public boolean isAngry() {
		return getHandle().isAngry();
	}

	public void setAngry(boolean isAngry) {
		getHandle().setAngry(isAngry);
	}
}