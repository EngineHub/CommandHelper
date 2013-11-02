package com.laytonsmith.abstraction.bukkit.entities;

import org.bukkit.entity.EnderDragonPart;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderDragonPart;

/**
 *
 * @author Hekta
 */
public class BukkitMCEnderDragonPart extends BukkitMCComplexEntityPart implements MCEnderDragonPart {

	public BukkitMCEnderDragonPart(EnderDragonPart part) {
		super(part);
	}

	public BukkitMCEnderDragonPart(AbstractionObject ao) {
		this((EnderDragonPart) ao.getHandle());
	}

	@Override
	public EnderDragonPart getHandle() {
		return (EnderDragonPart) metadatable;
	}
}