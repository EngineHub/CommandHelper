package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.minecart.PoweredMinecart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCPoweredMinecart;

/**
 * 
 * @author Hekta
 */
public class BukkitMCPoweredMinecart extends BukkitMCMinecart implements MCPoweredMinecart {

	public BukkitMCPoweredMinecart(PoweredMinecart minecart) {
		super(minecart);
	}

	public BukkitMCPoweredMinecart(AbstractionObject ao) {
		this((PoweredMinecart) ao.getHandle());
	}

	@Override
	public PoweredMinecart getHandle() {
		return (PoweredMinecart) metadatable;
	}
}