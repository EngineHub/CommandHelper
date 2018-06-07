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
		try {
			((Snowman) getHandle()).setDerp(derp);
		} catch(NoSuchMethodError ex) {
			// probably prior to 1.9.4
		}
	}

	@Override
	public boolean isDerp() {
		try {
			return ((Snowman) getHandle()).isDerp();
		} catch(NoSuchMethodError ex) {
			// probably prior to 1.9.4
		}
		return false;
	}

}
