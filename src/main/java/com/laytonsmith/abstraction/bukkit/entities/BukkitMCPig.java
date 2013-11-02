package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.Pig;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPig;

/**
 * 
 * @author jb_aero
 */
public class BukkitMCPig extends BukkitMCAgeable implements MCPig {

	public BukkitMCPig(Pig pig) {
		super(pig);
	}

	public BukkitMCPig(AbstractionObject ao) {
		this((Pig) ao.getHandle());
	}

	@Override
	public Pig getHandle() {
		return (Pig) metadatable;
	}

	public boolean isSaddled() {
		return getHandle().hasSaddle();
	}

	public void setSaddled(boolean saddled) {
		getHandle().setSaddle(saddled);
	}
}