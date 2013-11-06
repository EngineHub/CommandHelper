package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.AbstractionObject;
import com.laytonsmith.abstraction.entities.MCEnderSignal;

import org.bukkit.entity.EnderSignal;

public class BukkitMCEnderSignal extends BukkitMCEntity implements MCEnderSignal {

	public BukkitMCEnderSignal(EnderSignal signal) {
		super(signal);
	}

	public BukkitMCEnderSignal(AbstractionObject ao) {
		this((EnderSignal) ao.getHandle());
	}

	@Override
	public EnderSignal getHandle() {
		return (EnderSignal) metadatable;
	}
}
