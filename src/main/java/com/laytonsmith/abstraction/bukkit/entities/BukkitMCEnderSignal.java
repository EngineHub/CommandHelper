package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.bukkit.BukkitMCEntity;
import com.laytonsmith.abstraction.entities.MCEnderSignal;
import com.laytonsmith.annotations.WrappedItem;
import org.bukkit.entity.EnderSignal;

public class BukkitMCEnderSignal extends BukkitMCEntity implements
		MCEnderSignal {

	@WrappedItem EnderSignal es;
	public BukkitMCEnderSignal(EnderSignal e) {
		super(e);
		this.es = e;
	}

}
