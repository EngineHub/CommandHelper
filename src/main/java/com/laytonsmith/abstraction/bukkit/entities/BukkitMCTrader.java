package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCTrader;
import org.bukkit.entity.Entity;

public abstract class BukkitMCTrader extends BukkitMCAgeable implements MCTrader {

	public BukkitMCTrader(Entity be) {
		super(be);
	}
}
