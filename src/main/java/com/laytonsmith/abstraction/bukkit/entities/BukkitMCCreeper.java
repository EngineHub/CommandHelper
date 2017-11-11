
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
		this ((Creeper) ao.getHandle());
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
		try {
			return creeper.getMaxFuseTicks();
		} catch(NoSuchMethodError ex) {
			// Probably prior to 1.12.2
			return 30;
		}
	}

	@Override
	public void setMaxFuseTicks(int ticks) {
		try {
			creeper.setMaxFuseTicks(ticks);
		} catch(NoSuchMethodError ex){
			// Probably prior to 1.12.2
		}
	}

	@Override
	public int getExplosionRadius() {
		try {
			return creeper.getExplosionRadius();
		} catch(NoSuchMethodError ex) {
			// Probably prior to 1.12.2
			return 3;
		}
	}

	@Override
	public void setExplosionRadius(int radius) {
		try {
			creeper.setExplosionRadius(radius);
		} catch(NoSuchMethodError ex) {
			// Probably prior to 1.12.2
		}
	}
}
