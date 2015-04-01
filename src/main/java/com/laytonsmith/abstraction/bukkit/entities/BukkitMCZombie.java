package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCZombie;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

/**
 *
 * @author Hekta
 */
public class BukkitMCZombie extends BukkitMCLivingEntity implements MCZombie {

	public BukkitMCZombie(Entity zombie) {
		super(zombie);
	}

	public BukkitMCZombie(AbstractionObject ao) {
		this((Zombie) ao.getHandle());
	}

	@Override
	public Zombie getHandle() {
		return (Zombie)super.asLivingEntity();
	}

	@Override
	public boolean isBaby() {
		return getHandle().isBaby();
	}

	@Override
	public void setBaby(boolean isBaby) {
		getHandle().setBaby(isBaby);
	}

	@Override
	public boolean isVillager() {
		return getHandle().isVillager();
	}

	@Override
	public void setVillager(boolean isVillager) {
		getHandle().setVillager(isVillager);
	}
}