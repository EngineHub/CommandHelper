package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Zombie;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCZombie;

/**
 *
 * @author Hekta
 */
public class BukkitMCZombie extends BukkitMCCreature implements MCZombie {

	public BukkitMCZombie(Zombie zombie) {
		super(zombie);
	}

	public BukkitMCZombie(AbstractionObject ao) {
		this((Zombie) ao.getHandle());
	}

	@Override
	public Zombie getHandle() {
		return (Zombie) metadatable;
	}

	public boolean isBaby() {
		return getHandle().isBaby();
	}

	public void setBaby(boolean isBaby) {
		getHandle().setBaby(isBaby);
	}

	public boolean isVillager() {
		return getHandle().isVillager();
	}

	public void setVillager(boolean isVillager) {
		getHandle().setVillager(isVillager);
	}
}