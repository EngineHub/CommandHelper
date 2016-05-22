package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowman;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCSnowman;

public class BukkitMCSnowman extends BukkitMCLivingEntity implements MCSnowman {

	public BukkitMCSnowman(Entity snowman) {
		super(snowman);
	}
	
	public BukkitMCSnowman(AbstractionObject ao) {
		this((Snowman) ao.getHandle());
	}

	@Override
	public void setDerp(boolean derp) {
		((Snowman)getHandle()).setDerp(derp);
	}

	@Override
	public boolean isDerp() {
		return ((Snowman)getHandle()).isDerp();
	}

}
