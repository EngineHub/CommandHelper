package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.IronGolem;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCIronGolem;

/**
 *
 * @author Hekta
 */
public class BukkitMCIronGolem extends BukkitMCCreature implements MCIronGolem {

	public BukkitMCIronGolem(IronGolem golem) {
		super(golem);
	}

	public BukkitMCIronGolem(AbstractionObject ao) {
		this((IronGolem) ao.getHandle());
	}

	@Override
	public IronGolem getHandle() {
		return (IronGolem) metadatable;
	}

	public boolean isPlayerCreated() {
		return getHandle().isPlayerCreated();
	}

	public void setPlayerCreated(boolean playerCreated) {
		getHandle().setPlayerCreated(playerCreated);
	}
}