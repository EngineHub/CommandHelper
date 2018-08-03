package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCCreeper;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;

public class BukkitMCCreeper extends BukkitMCLivingEntity implements MCCreeper {

	Creeper creeper;

	public BukkitMCCreeper(Entity c) {
		super(c);
		creeper = (Creeper) c;
	}

	public BukkitMCCreeper(AbstractionObject ao) {
		this((Creeper) ao.getHandle());
	}

	@Override
	public boolean isPowered() {
		return creeper.isPowered();
	}

	@Override
	public void setPowered(boolean powered) {
		creeper.setPowered(powered);
	}

	@Override
	public int getMaxFuseTicks() {
		return creeper.getMaxFuseTicks();
	}

	@Override
	public void setMaxFuseTicks(int ticks) {
		creeper.setMaxFuseTicks(ticks);
	}

	@Override
	public int getExplosionRadius() {
		return creeper.getExplosionRadius();
	}

	@Override
	public void setExplosionRadius(int radius) {
		creeper.setExplosionRadius(radius);
	}
}
