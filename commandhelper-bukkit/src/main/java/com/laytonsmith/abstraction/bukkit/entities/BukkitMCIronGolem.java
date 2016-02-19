package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCIronGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;

/**
 *
 * @author Hekta
 */
public class BukkitMCIronGolem extends BukkitMCLivingEntity implements MCIronGolem {

	public BukkitMCIronGolem(Entity golem) {
		super(golem);
	}

	public BukkitMCIronGolem(AbstractionObject ao) {
		this((IronGolem) ao.getHandle());
	}

	@Override
	public IronGolem getHandle() {
		return (IronGolem)super.getHandle();
	}

	@Override
	public boolean isPlayerCreated() {
		return getHandle().isPlayerCreated();
	}

	@Override
	public void setPlayerCreated(boolean playerCreated) {
		getHandle().setPlayerCreated(playerCreated);
	}
}