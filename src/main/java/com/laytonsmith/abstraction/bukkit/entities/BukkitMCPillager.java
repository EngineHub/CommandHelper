package com.laytonsmith.abstraction.bukkit.entities;

import com.laytonsmith.abstraction.MCInventory;
import com.laytonsmith.abstraction.bukkit.BukkitMCInventory;
import com.laytonsmith.abstraction.entities.MCPillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pillager;

public class BukkitMCPillager extends BukkitMCLivingEntity implements MCPillager {

	Pillager p;

	public BukkitMCPillager(Entity pillager) {
		super(pillager);
		this.p = (Pillager) pillager;
	}

	@Override
	public Pillager getHandle() {
		return p;
	}

	@Override
	public MCInventory getInventory() {
		return new BukkitMCInventory(getHandle().getInventory());
	}
}
