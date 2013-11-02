package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.minecart.RideableMinecart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCRideableMinecart;

/**
 * 
 * @author Hekta
 */
public class BukkitMCRideableMinecart extends BukkitMCMinecart implements MCRideableMinecart {

	public BukkitMCRideableMinecart(RideableMinecart minecart) {
		super(minecart);
	}

	public BukkitMCRideableMinecart(AbstractionObject ao) {
		this((RideableMinecart) ao.getHandle());
	}

	@Override
	public RideableMinecart getHandle() {
		return (RideableMinecart) metadatable;
	}
}