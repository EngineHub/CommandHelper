package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCEnderSignal;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;

public class BukkitMCEnderSignal extends BukkitMCEntity implements
		MCEnderSignal {

	EnderSignal es;

	public BukkitMCEnderSignal(Entity e) {
		super(e);
		this.es = (EnderSignal) e;
	}

}
