package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPigZombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;

/**
 *
 * @author Hekta
 */
public class BukkitMCPigZombie extends BukkitMCZombie implements MCPigZombie {

	public BukkitMCPigZombie(Entity zombie) {
		super(zombie);
	}

	public BukkitMCPigZombie(AbstractionObject ao) {
		this((PigZombie) ao.getHandle());
	}

	@Override
	public PigZombie getHandle() {
		return (PigZombie)super.getHandle();
	}

	@Override
	public int getAnger() {
		return getHandle().getAnger();
	}

	@Override
	public void setAnger(int anger) {
		getHandle().setAnger(anger);
	}

	@Override
	public boolean isAngry() {
		return getHandle().isAngry();
	}

	@Override
	public void setAngry(boolean isAngry) {
		getHandle().setAngry(isAngry);
	}
}