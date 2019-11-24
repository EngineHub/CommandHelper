package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCVex;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vex;

public class BukkitMCVex extends BukkitMCLivingEntity implements MCVex {

	public BukkitMCVex(Entity ent) {
		super(ent);
	}

	@Override
	public boolean isCharging() {
		return ((Vex) getHandle()).isCharging();
	}

	@Override
	public void setCharging(boolean charging) {
		((Vex) getHandle()).setCharging(charging);
	}
}
