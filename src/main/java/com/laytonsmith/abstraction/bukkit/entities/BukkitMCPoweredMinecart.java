package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCPoweredMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.PoweredMinecart;

public class BukkitMCPoweredMinecart extends BukkitMCMinecart implements MCPoweredMinecart {

	PoweredMinecart pm;

	public BukkitMCPoweredMinecart(Entity e) {
		super(e);
		this.pm = (PoweredMinecart) e;
	}
}
