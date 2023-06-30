package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPiglin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Piglin;

public class BukkitMCPiglin extends BukkitMCAgeable implements MCPiglin {

	public BukkitMCPiglin(Entity ent) {
		super(ent);
	}

	@Override
	public Piglin getHandle() {
		return (Piglin) super.getHandle();
	}

	@Override
	public boolean isImmuneToZombification() {
		return getHandle().isImmuneToZombification();
	}

	@Override
	public void setImmuneToZombification(boolean immune) {
		getHandle().setImmuneToZombification(immune);
	}
}
