package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCZombie;
import com.laytonsmith.core.CHLog;
import com.laytonsmith.core.LogLevel;
import com.laytonsmith.core.constructs.Target;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;

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
		try {
			getHandle().setVillager(isVillager);
		} catch(UnsupportedOperationException ex){
			// 1.11 or later
			CHLog.GetLogger().Log(CHLog.Tags.DEPRECATION, LogLevel.ERROR,
					"Cannot change a Zombie to ZombieVillager in Minecraft 1.11+", Target.UNKNOWN);
		}
	}
}