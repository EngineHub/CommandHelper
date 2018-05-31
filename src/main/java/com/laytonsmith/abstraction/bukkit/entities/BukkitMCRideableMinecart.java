package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.entities.MCRideableMinecart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.RideableMinecart;

public class BukkitMCRideableMinecart extends BukkitMCMinecart implements MCRideableMinecart {

	RideableMinecart rm;

	public BukkitMCRideableMinecart(Entity e) {
		super(e);
		this.rm = (RideableMinecart) e;
	}

}
