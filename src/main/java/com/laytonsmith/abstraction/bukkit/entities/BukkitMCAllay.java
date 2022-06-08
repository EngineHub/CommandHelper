package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCAllay;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Entity;

public class BukkitMCAllay extends BukkitMCLivingEntity implements MCAllay {

	Allay a;

	public BukkitMCAllay(Entity allay) {
		super(allay);
		this.a = (Allay) allay;
	}

	@Override
	public Allay getHandle() {
		return a;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}
}
