package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.minecart.HopperMinecart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCHopperMinecart;

/**
 * 
 * @author Hekta
 */
public class BukkitMCHopperMinecart extends BukkitMCMinecart implements MCHopperMinecart {

	public BukkitMCHopperMinecart(HopperMinecart minecart) {
		super(minecart);
	}

	public BukkitMCHopperMinecart(AbstractionObject ao) {
		this((HopperMinecart) ao.getHandle());
	}

	@Override
	public HopperMinecart getHandle() {
		return (HopperMinecart) metadatable;
	}
}