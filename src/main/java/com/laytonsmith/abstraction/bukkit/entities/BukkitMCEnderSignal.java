package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.entities.MCEnderSignal;
import org.bukkit.entity.EnderSignal;

public class BukkitMCEnderSignal extends BukkitMCEntity implements
		MCEnderSignal {

	EnderSignal es;
	public BukkitMCEnderSignal(EnderSignal e) {
		super(e);
		this.es = e;
	}

}
